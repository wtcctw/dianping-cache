package com.dianping.cache.monitor2;

import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.monitor.Constants;
import com.dianping.cache.monitor.CuratorManager;
import com.dianping.cache.monitor.MemberMonitor;
import com.dianping.cache.monitor.ServerState;
import com.dianping.squirrel.common.util.JsonUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.KeeperException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by thunder on 16/1/29.
 */
public class TaskManager {

    private MemberMonitor memberMonitor;

    private CuratorFramework curatorClient;

    private final int CORES = Runtime.getRuntime().availableProcessors();

    private static Map<String, ServerState> serverStatMap = new HashMap<String, ServerState>();

    private ScheduledThreadPoolExecutor monitorThreadPool;

    private ExecutorService taskRunnerThreadPool;

    private double percent = 0.5;

    public TaskManager() {
        this.memberMonitor = new MemberMonitor();
        monitorThreadPool = new ScheduledThreadPoolExecutor(1);
        taskRunnerThreadPool = Executors.newCachedThreadPool();
        CuratorManager.getInstance().ensurePath(Constants.MANAGER_PATH);
        curatorClient = CuratorManager.getInstance().getCuratorClient();

    }

    public void doCheck() {

    }

    String concatZkPath(String s1, String s2) {
        return s1 + "/" + s2;
    }

    private void zkDelete(String path) {
        try {
            List<String> children = curatorClient.getChildren().forPath(path);
            if (children == null || children.size() == 0) {
                curatorClient.delete().forPath(path);
            }
            for (String child : children) {
                zkDelete(concatZkPath(path, child));
            }
            curatorClient.delete().forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void filterServers() throws Exception {
        List<String> clusters = curatorClient.getChildren().forPath(Constants.MANAGER_PATH);
        List<String> clusterInUse = curatorClient.getChildren().forPath(Constants.SERVICE_PATH);
        for(String inUse : clusterInUse) {
            if(!clusters.contains(inUse)) {
                zkDelete(concatZkPath(Constants.SERVICE_PATH, inUse));
            }
        }

    }
    private CacheConfiguration getServersFromZkData(String path) throws Exception {
        byte[] data = curatorClient.getData().watched().forPath(path);
        if(data == null || data.length==0) {
            return null;
        }
        String value = new String(data, "UTF-8");
        CacheConfiguration config = JsonUtils.fromStr(value, CacheConfiguration.class);
        return config;
    }
    private CacheConfiguration getManagerClusterCinfiguration(String cluster) throws Exception {
        // : 从 manager path 那个地方获得本 cluster 的所有机器
        return getServersFromZkData(concatZkPath(Constants.MANAGER_PATH, cluster));
    }

    private void setServiceConfiguration(String cluster, CacheConfiguration config) throws Exception {
        // : 设置线上的机器集群状态
        String result = JsonUtils.toStr(config);
        curatorClient.setData().forPath(concatZkPath(Constants.SERVICE_PATH, cluster), result.getBytes("UTF-8"));
    }

    private CacheConfiguration getServiceClusterCinfiguration(String cluster) throws Exception {
        // : 获得 线上 memcache 某个集群信息
        return getServersFromZkData(concatZkPath(Constants.SERVICE_PATH, cluster));
    }

    private List<String> getManagerClusters() throws Exception {
        // : 删掉 map 里面的多余项
        return curatorClient.getChildren().forPath(Constants.MANAGER_PATH);
    }
    private void addServiceCluster(CacheConfiguration cacheConfiguration) throws Exception {
        String path = concatZkPath(Constants.SERVICE_PATH, cacheConfiguration.getCacheKey());
        curatorClient.create().creatingParentsIfNeeded().forPath(path, JsonUtils.toStr(cacheConfiguration).getBytes("UTF-8"));
    }

    void filterLiveNodes() throws Exception {
        //  在 servicePath markDownPath markUpPath 处删掉所有的不在管理员列表中的节点
        Map<String, CacheConfiguration> managerConfig = new HashMap<String, CacheConfiguration>();
        Map<String, CacheConfiguration> serviceConfig = new HashMap<String, CacheConfiguration>();

        // 根据 manager 中的数据同步线上的数据
        for (Map.Entry<String, CacheConfiguration> entry : managerConfig.entrySet()) {
            String cluster = entry.getKey();

            CacheConfiguration managerClusterConfig = entry.getValue();
            CacheConfiguration serviceClusterConfig = serviceConfig.get(cluster);

            List<String> managerServers = managerClusterConfig.getServerList();
            List<String> serviceServers = serviceClusterConfig.getServerList();

            // 管理员添加了 cluster
            if (serviceClusterConfig == null) {
                CacheConfiguration newCluster = managerClusterConfig;
                addServiceCluster(newCluster);
            } else {
                // 管理员去掉了 cluster 中的某些机器
                for (String server : serviceServers) {
                    if (!managerServers.contains(server))
                        serviceServers.remove(server);
                }

                // 管理员添加了 cluster 中的某些机器
                for (String server : managerServers) {
                    if (!serviceServers.contains(server)) {
                        serviceServers.add(server);
                    }
                }
                // 使得修改生效
                serviceClusterConfig.setServerList(serviceServers);
                setServiceConfiguration(cluster, serviceClusterConfig);
            }
        }

        //清理 MARK DOWN 和 MARK UP 只需要将多余的节点删除
        List<String> allManagerServers = new ArrayList<String>();
        for (CacheConfiguration cacheConfiguration : managerConfig.values()) {
            allManagerServers.addAll(cacheConfiguration.getServerList());
        }

        List<String> markDownChildren = curatorClient.getChildren().forPath(Constants.MONITOR_MARKDOWN_PATH);
        List<String> markUpChildren = curatorClient.getChildren().forPath(Constants.MONITOR_MARKUP_PATH);

        for (String hostPort : markDownChildren) {
            if (!allManagerServers.contains(hostPort))
                zkDelete(concatZkPath(Constants.MONITOR_MARKDOWN_PATH, hostPort));
        }

        for (String hostPort : markUpChildren) {
            if (!allManagerServers.contains(hostPort))
                zkDelete(concatZkPath(Constants.MONITOR_MARKUP_PATH, hostPort));
        }

    }

    private void changeNodeMode(boolean add, String cluster) throws Exception {
        CacheConfiguration managerConfig = getManagerClusterCinfiguration(cluster);
        CacheConfiguration serviceConfig = getServiceClusterCinfiguration(cluster);
        List<String> serviceServers = serviceConfig.getServerList();
        List<String> managerServers = managerConfig.getServerList();
        boolean change = false;
        for(String server : managerServers) {
            String path;
            if(add) {
                path = concatZkPath(Constants.MONITOR_MARKUP_PATH, server);
            } else {
                path = concatZkPath(Constants.MONITOR_MARKDOWN_PATH, server);
            }
            List<String> children = curatorClient.getChildren().forPath(path);
            // TODO : 这个地方是测试 所以直接打开
            //if(children.size() > memberMonitor.getMemberCount() * percent) {
                if( add && !serviceServers.contains(server)) {
                    serviceServers.add(server);
                    change = true;
                }
                if( !add && serviceServers.contains(server)) {
                    serviceServers.remove(server);
                    change = true;
                }
            //}
        }
        if(change) {
            serviceConfig.setServerList(serviceServers);
            setServiceConfiguration(cluster, serviceConfig);
        }

    }

    void addLiveNode(String cluster) throws Exception {
        changeNodeMode(true, cluster);
    }

    void removeDeadNode(String cluster) throws Exception {
        changeNodeMode(false, cluster);
    }

    private void judgeMachineState() throws Exception {
        try {
            if (curatorClient.create().creatingParentsIfNeeded().forPath(Constants.MONITOR_JUDGE_LOCK) != null) {
                filterLiveNodes(); // 每个周期进行一次就可以了
                // TODO: 这个地方对不对？要先找到 manager 配置的 cluster 然后根据 cluster 找到每个 cluster 的机器
                List<String> managerClusters = getManagerClusters();
                for (String cluster : managerClusters) {
                    addLiveNode(cluster);
                    removeDeadNode(cluster);
                }
            }
            curatorClient.delete().forPath(Constants.MONITOR_JUDGE_LOCK);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public void start() throws Exception {
        List<String> clusters = getManagerClusters();
        final List<String> clusterInUse = curatorClient.getChildren().forPath(Constants.SERVICE_PATH);
        filterServers();
        try {
            if (curatorClient.create().creatingParentsIfNeeded().forPath(Constants.INIT_STAT) != null) {
                for (String cluster : clusters) {
                    if (!clusterInUse.contains(cluster)) {
                        curatorClient.create().creatingParentsIfNeeded().forPath(Constants.SERVICE_PATH + "/" + cluster);
                    }
                    //setServers(concatZkPath(Constants.SERVICE_PATH, cluster), getManagerClusterServers(cluster));
                    CacheConfiguration configuration = getManagerClusterCinfiguration(cluster);
                    setServiceConfiguration(cluster, configuration);
                }
                curatorClient.delete().forPath(Constants.INIT_STAT);
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        }

        final Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    List<String> clusters = getManagerClusters();
                    for(String cluster : clusters) {
                        CacheConfiguration configuration = getManagerClusterCinfiguration(cluster);
                        List<String> servers = configuration.getServerList();
                        for(String server : servers) {
                            if(serverStatMap.get(server) == null)
                                serverStatMap.put(server, new ServerState(server));
                            TaskRunner taskRunner = new TaskRunner(serverStatMap.get(server));
                            taskRunnerThreadPool.submit(taskRunner);
                        }
                        judgeMachineState();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        // TODO: 这个地方是间隔
        int interval = 1000;
        monitorThreadPool.scheduleWithFixedDelay(task, interval, interval, TimeUnit.MILLISECONDS);
    }

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();
        try {
            taskManager.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(100000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
