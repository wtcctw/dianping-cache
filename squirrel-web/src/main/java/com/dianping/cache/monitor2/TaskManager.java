package com.dianping.cache.monitor2;

import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.monitor.Constants;
import com.dianping.cache.monitor.CuratorManager;
import com.dianping.cache.monitor.MemberMonitor;
import com.dianping.cache.monitor2.ServerState;
import com.dianping.squirrel.common.util.JsonUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import sun.misc.Cache;

import java.io.UnsupportedEncodingException;
import java.lang.management.BufferPoolMXBean;
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
                return ;
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

    private CacheConfiguration getServiceClusterCinfiguration(String cluster) throws Exception {
        // : 获得 线上 memcache 某个集群信息
        return getServersFromZkData(concatZkPath(Constants.SERVICE_PATH, cluster));
    }

    private Map<String, CacheConfiguration> getManagerClusterConfigurationMap() throws Exception {
        List<String> clusters = getManagerClusters();
        Map<String, CacheConfiguration> map = new HashMap<String, CacheConfiguration>();
        for(String cluster : clusters) {
            CacheConfiguration config = getManagerClusterCinfiguration(cluster);
            map.put(cluster, config);
        }
        return map;
    }

    private Map<String, CacheConfiguration> getServiceClusterConfigurationMap() throws Exception {
        List<String> clusters = getServiceClusters();
        Map<String, CacheConfiguration> map = new HashMap<String, CacheConfiguration>();
        for(String cluster : clusters) {
            CacheConfiguration config = getServiceClusterCinfiguration(cluster);
            map.put(cluster, config);
        }
        return map;
    }

    private void setServiceConfiguration(String cluster, CacheConfiguration config) throws Exception {
        // : 设置线上的机器集群状态
        String result = JsonUtils.toStr(config);
        curatorClient.setData().forPath(concatZkPath(Constants.SERVICE_PATH, cluster), result.getBytes("UTF-8"));
    }

    private void setManagerConfiguration(String cluster, CacheConfiguration config) throws Exception {
        // : 设置配置的机器集群状态
        String result = JsonUtils.toStr(config);
        curatorClient.setData().forPath(concatZkPath(Constants.MANAGER_PATH, cluster), result.getBytes("UTF-8"));
    }


    private List<String> getManagerClusters() throws Exception {
        // : 删掉 map 里面的多余项
        return curatorClient.getChildren().forPath(Constants.MANAGER_PATH);
    }
    private List<String> getServiceClusters() throws Exception {
        if(curatorClient.checkExists().forPath(Constants.SERVICE_PATH) == null)
            curatorClient.create().forPath(Constants.SERVICE_PATH);
        return curatorClient.getChildren().forPath(Constants.SERVICE_PATH);
    }
    private void addServiceCluster(CacheConfiguration cacheConfiguration) throws Exception {
        String path = concatZkPath(Constants.SERVICE_PATH, cacheConfiguration.getCacheKey());
        curatorClient.create().creatingParentsIfNeeded().forPath(path, JsonUtils.toStr(cacheConfiguration).getBytes("UTF-8"));
    }

    void filterLiveNodes() throws Exception {
        //  在 servicePath markDownPath markUpPath 处删掉所有的不在管理员列表中的节点
        Map<String, CacheConfiguration> managerConfig = getManagerClusterConfigurationMap();
        Map<String, CacheConfiguration> serviceConfig = getServiceClusterConfigurationMap();

        // 根据 manager 中的数据同步线上的数据
        for (Map.Entry<String, CacheConfiguration> entry : managerConfig.entrySet()) {
            String cluster = entry.getKey();

            CacheConfiguration managerClusterConfig = entry.getValue();
            CacheConfiguration serviceClusterConfig = serviceConfig.get(cluster);

            List<String> managerServers = managerClusterConfig.getServerList();

            // 管理员添加了 cluster
            // TODO: 管理员删除的 cluster
            if (serviceClusterConfig == null) {
                CacheConfiguration newCluster = managerClusterConfig;
                addServiceCluster(newCluster);
            } else {

                boolean change = false;
                // 管理员去掉了 cluster 中的某些机器
                // TODO : 边遍历边删除 去掉
                List<String> serviceServers = serviceClusterConfig.getServerList();
                List<String> forRemoveFromServiceServer = new ArrayList<String>();

                for (String server : serviceServers) {
                    if (!managerServers.contains(server)) {
                        forRemoveFromServiceServer.add(server);
                        change = true;
                    }
                }

                for(String s : forRemoveFromServiceServer) {
                    serviceServers.remove(s);
                }

                List<String> froAddFromServiceServer = new ArrayList<String>();
                // 管理员添加了 cluster 中的某些机器
                for (String server : managerServers) {
                    if (!serviceServers.contains(server)) {
                        froAddFromServiceServer.add(server);
                        change = true;
                    }
                }
                serviceServers.addAll(froAddFromServiceServer);
                // 使得修改生效
                if(change) {
                    serviceClusterConfig.setServerList(serviceServers);
                    setServiceConfiguration(cluster, serviceClusterConfig);
                }
            }
        }

        //清理 MARK DOWN 和 MARK UP 只需要将多余的节点( manager 没有配置过的 )删除
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

            if(curatorClient.checkExists().forPath(path) != null) {
                List<String> children = curatorClient.getChildren().forPath(path);
                // TODO : 这个地方是测试 所以直接打开
                //if(children.size() > memberMonitor.getMemberCount() * percent) {
                if (add && !serviceServers.contains(server)) {
                    serviceServers.add(server);
                    change = true;
                }
                if (!add && serviceServers.contains(server)) {
                    serviceServers.remove(server);
                    change = true;
                }
                //}
            }
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
            curatorClient.create().creatingParentsIfNeeded().forPath(Constants.MONITOR_JUDGE_LOCK);
            try {
                filterLiveNodes(); // 每个周期进行一次就可以了
                // TODO: 这个地方对不对？要先找到 manager 配置的 cluster 然后根据 cluster 找到每个 cluster 的机器
                List<String> managerClusters = getManagerClusters();
                for (String cluster : managerClusters) {
                    addLiveNode(cluster);
                    removeDeadNode(cluster);
                }
            } finally {
                curatorClient.delete().forPath(Constants.MONITOR_JUDGE_LOCK);
            }
        } catch (Exception e) {
            System.out.println(e.toString() + e.getLocalizedMessage());
        }
    }

    private void init() throws Exception {
        // 创建必要的四个节点 路径如下
        String[] paths = {Constants.SERVICE_PATH, Constants.MANAGER_PATH, Constants.MONITOR_MARKDOWN_PATH, Constants.MONITOR_MARKUP_PATH};
        for(String node : paths) {
            if(curatorClient.checkExists().forPath(node) == null)
                curatorClient.create().creatingParentsIfNeeded().forPath(node);
        }
    }

    public void start() throws Exception {
//        try {
//            curatorClient.create().creatingParentsIfNeeded().forPath(Constants.INIT_STAT_LOCK);
//            init();
//            // TODO 下面一段可以干掉 不合逻辑 init 留下 那个脚本第一次可以额外执行
//            List<String> clusters = getManagerClusters();
//            final List<String> clusterInUse = curatorClient.getChildren().forPath(Constants.SERVICE_PATH);
//            filterServers();
//            for (String cluster : clusters) {
//                if (!clusterInUse.contains(cluster)) {
//                    curatorClient.create().creatingParentsIfNeeded().forPath(Constants.SERVICE_PATH + "/" + cluster);
//                }
//                //setServers(concatZkPath(Constants.SERVICE_PATH, cluster), getManagerClusterServers(cluster));
//                CacheConfiguration configuration = getManagerClusterCinfiguration(cluster);
//                setServiceConfiguration(cluster, configuration);
//            }
//            curatorClient.delete().forPath(Constants.INIT_STAT_LOCK);
//        } catch (KeeperException e) {
//            e.printStackTrace();
//        }

        final Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    List<String> clusters = getManagerClusters();
                    for(String cluster : clusters) {
                        if(!cluster.startsWith("memcached"))
                            continue;
                        CacheConfiguration configuration = getManagerClusterCinfiguration(cluster);
                        List<String> servers = configuration.getServerList();
                        Map<String, Boolean> mp = new HashMap<String, Boolean>();
                        for(String server : servers) {
                            if(mp.get(server) != null) {
                                continue;
                            } else {
                                Boolean b = true;
                                mp.put(server, b);
                            }
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

    public void deleteLocks() {
        try {
            curatorClient.delete().forPath(Constants.INIT_STAT_LOCK);
            curatorClient.delete().forPath(Constants.MONITOR_JUDGE_LOCK);
        } catch (Exception e) {

        }
    }

    private void copyToAlpha(String betaPath, String alphaPath, CuratorFramework betaClient, CuratorFramework alphaClient) throws Exception {
        List<String> children = betaClient.getChildren().forPath(betaPath);

        if(children == null || children.size() == 0) {
            byte[] data = betaClient.getData().forPath(betaPath);
            alphaClient.create().creatingParentsIfNeeded().forPath(alphaPath, data);
            return ;
        }

        for(String c : children) {
            String betaNewPath = betaPath + "/" + c;
            String alphaNewPath = alphaPath + "/" + c;
            copyToAlpha(betaNewPath, alphaNewPath, betaClient, alphaClient);
        }

        byte[] data = betaClient.getData().forPath(betaPath);
        alphaClient.setData().forPath(alphaPath, data);
    }

    public void prepareBetaData() throws Exception{
        CuratorFramework betaClient = CuratorFrameworkFactory.newClient("qa.lion.dp:2181", 60 * 1000, 30 * 1000,
                new RetryNTimes(3, 1000));
        betaClient.start();
        String betaPath = Constants.SERVICE_PATH;
        String alphaPath = Constants.MANAGER_PATH;
        copyToAlpha(betaPath, alphaPath, betaClient, this.curatorClient);
    }

    public void testServerChange() {
        try {
            // 准备数据
            CacheConfiguration config = getManagerClusterCinfiguration("memcached-tuangou");
            List<String> servers = config.getServerList();
            String newServer = "192.168.8.45:11211";
            servers.add(newServer);
            config.setServerList(servers);
            setManagerConfiguration("memcached-tuangou", config);
            this.init();
            this.start();

            Thread.sleep(10000);

            CacheConfiguration config2= getServiceClusterCinfiguration("memcached-tuangou");
            List<String> serviceServer = config2.getServerList();
            boolean have = serviceServer.contains(newServer);

            CacheConfiguration config3 = getManagerClusterCinfiguration("memcached-tuangou");
            List<String> servers3 = config.getServerList();
            String newServer3 = "192.168.8.45:11211";
            servers3.remove(newServer3);
            config3.setServerList(servers3);
            setManagerConfiguration("memcached-tuangou", config3);

            Thread.sleep(10000);

            CacheConfiguration config4= getServiceClusterCinfiguration("memcached-tuangou");
            List<String> serviceServer4 = config4.getServerList();
            have = serviceServer4.contains(newServer);
            int a = 1;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void removeAllData() throws Exception {
        String[] paths = {Constants.INIT_STAT_LOCK, Constants.MONITOR_JUDGE_LOCK, Constants.SERVICE_PATH, Constants.MANAGER_PATH, Constants.MONITOR_MARKDOWN_PATH, Constants.MONITOR_MARKUP_PATH};
        for(String path : paths) {
            if(curatorClient.checkExists().forPath(path) != null) {
                zkDelete(path);
            }
        }
    }

    public void prepareManagerData() throws Exception {
        String path = Constants.MANAGER_PATH + "/" + "memcached-tuangou";
        String testClusterInfo = "{\"cacheKey\":\"memcached-tuangou\",\"clientClazz\":\"com.dianping.cache.memcached.MemcachedClientImpl\",\"servers\":\"10.66.11.117:11211\",\"transcoderClazz\":\"com.dianping.cache.memcached.HessianTranscoder\",\"addTime\":1452755302947,\"serverList\":[\"10.66.11.117:11211\"]}";
        curatorClient.create().creatingParentsIfNeeded().forPath(path, testClusterInfo.getBytes("UTF-8"));
    }

    public static void main(String[] args) {

        try {

            TaskManager taskManager = new TaskManager();
            taskManager.removeAllData();
            taskManager.prepareBetaData();
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
