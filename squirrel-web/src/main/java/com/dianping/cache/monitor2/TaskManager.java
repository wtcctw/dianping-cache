package com.dianping.cache.monitor2;

import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.monitor.Constants;
import com.dianping.cache.monitor.CuratorManager;
import com.dianping.cache.monitor.MemberMonitor;
import com.dianping.cache.monitor.NotifyManager;
import com.dianping.squirrel.common.util.JsonUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by thunder on 16/1/29.
 */
public class TaskManager {

    private MemberMonitor memberMonitor;

    private CuratorFramework curatorClient;

    private final int CORES = Runtime.getRuntime().availableProcessors();

    private static Map<String, TaskRunner> serverStatMap = new HashMap<String, TaskRunner>();

    private ScheduledThreadPoolExecutor monitorThreadPool;

    private ExecutorService taskRunnerThreadPool;

    private static final Logger logger = LoggerFactory.getLogger(TaskRunner.class);

    private double percent = 0.5;

    public TaskManager() {
        this.memberMonitor = new MemberMonitor();
        monitorThreadPool = new ScheduledThreadPoolExecutor(1);
        taskRunnerThreadPool = Executors.newCachedThreadPool();
        CuratorManager.getInstance().ensurePath(Constants.MANAGER_PATH);
        curatorClient = CuratorManager.getInstance().getCuratorClient();
    }

    String concatZkPath(String s1, String s2) {
        return s1 + "/" + s2;
    }

    private void zkDelete(String path) throws Exception {
        List<String> children = curatorClient.getChildren().forPath(path);
        if (children == null || children.size() == 0) {
            logger.info("remove zookeeper path " + path);
            curatorClient.delete().forPath(path);
            return ;
        }
        for (String child : children) {
            zkDelete(concatZkPath(path, child));
        }
        curatorClient.delete().forPath(path);
    }

    private String getMarkDownPath(String memcached) {
        StringBuilder buf = new StringBuilder(128);
        buf.append(Constants.MARKDOWN_PATH).append('/').append(memcached);
        return buf.toString();
    }

    private boolean isAlive(String server) {
        String path = getMarkDownPath(server);
        try {
            List<String> status = curatorClient.getChildren().watched().forPath(path);
            int memberCount = memberMonitor.getMemberCount();

            if(status.size() >= memberCount * percent) {
                return false;
            } else {
                return true;
            }
        } catch (KeeperException.NoNodeException e) {
            return true;
        } catch (Exception e) {
            logger.error("unknown server in monitor " + server);
            return true;
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
            if(!cluster.startsWith("memcached"))
                continue;
            CacheConfiguration config = getManagerClusterCinfiguration(cluster);
            if(config == null)
                continue;
            map.put(cluster, config);
        }
        return map;
    }

    private Map<String, CacheConfiguration> getServiceClusterConfigurationMap() throws Exception {
        List<String> clusters = getServiceClusters();
        Map<String, CacheConfiguration> map = new HashMap<String, CacheConfiguration>();
        for(String cluster : clusters) {
            if(!cluster.startsWith("memcached"))
                continue;
            CacheConfiguration config = getServiceClusterCinfiguration(cluster);
            map.put(cluster, config);
        }
        return map;
    }

    private void setServiceConfiguration(String cluster, CacheConfiguration config) throws Exception {
        // : 设置线上的机器集群状态
        if(!cluster.startsWith("memcached"))
            return ;

        String result = JsonUtils.toStr(config);

        String path = concatZkPath(Constants.SERVICE_PATH, cluster);
        if(curatorClient.checkExists().forPath(concatZkPath(Constants.SERVICE_PATH, cluster)) == null) {
            curatorClient.create().creatingParentsIfNeeded().forPath(path, result.getBytes("UTF-8"));
        } else {
            curatorClient.setData().forPath(concatZkPath(Constants.SERVICE_PATH, cluster), result.getBytes("UTF-8"));
        }
    }

    private void setManagerConfiguration(String cluster, CacheConfiguration config) throws Exception {
        // : 设置配置的机器集群状态
        String result = JsonUtils.toStr(config);
        curatorClient.setData().forPath(concatZkPath(Constants.MANAGER_PATH, cluster), result.getBytes("UTF-8"));
    }


    private List<String> getManagerClusters() throws Exception {
        // : 删掉 map 里面的多余项
        List<String> result = new ArrayList<String>();
        List<String> clusters = curatorClient.getChildren().forPath(Constants.MANAGER_PATH);
        for(String cluster : clusters) {
            if(!cluster.startsWith("memcached")) //memcached-mopay
                continue;
            //CacheConfiguration config = getManagerClusterCinfiguration(cluster);
            result.add(cluster);
        }
        return result;
    }

    private List<String> getServiceClusters() throws Exception {
        if(curatorClient.checkExists().forPath(Constants.SERVICE_PATH) == null)
            curatorClient.create().forPath(Constants.SERVICE_PATH);
        return curatorClient.getChildren().forPath(Constants.SERVICE_PATH);
    }

    private void removeServiceCluster(String cluster) throws Exception {
        if(!cluster.startsWith("memcached"))
            return;
        String path = concatZkPath(Constants.SERVICE_PATH, cluster);
        zkDelete(path);
    }

    void syncMachineState() throws Exception {
        Map<String, CacheConfiguration> managerConfig = getManagerClusterConfigurationMap();
        Map<String, CacheConfiguration> serviceConfig = getServiceClusterConfigurationMap();

        // 增加 管理员增加的 cluster
        for(String key : managerConfig.keySet()) {
            if(!serviceConfig.keySet().contains(key)) {
                setServiceConfiguration(key, managerConfig.get(key));
                logger.info("add new cluster " + key);
            }
        }

        // 删除 管理员删除的 cluster
        for(String key : serviceConfig.keySet()) {
            if(!managerConfig.keySet().contains(key)) {
                removeServiceCluster(key);
                logger.info("remove sluter " + key);
            }
        }

        // 给所有 manager 上面的机器 增加机器的监控
        for(String key : managerConfig.keySet()) {
            CacheConfiguration configuration = managerConfig.get(key);
            List<String> machines = configuration.getServerList();
            if(machines == null)
                continue;
            for(String server : machines) {
                if(serverStatMap.get(server) == null) {
                    TaskRunner taskRunner = new TaskRunner(server);
                    serverStatMap.put(server, taskRunner);
                }
            }
        }

        boolean change = false;

        // 根据监控的情况上线
        serviceConfig = getServiceClusterConfigurationMap();

        for(String managerClusterKey : managerConfig.keySet()) {
            CacheConfiguration managerConfiguration = managerConfig.get(managerClusterKey);
            CacheConfiguration serviceConfiguration = serviceConfig.get(managerClusterKey);
            List<String> managerServers = managerConfiguration.getServerList();
            List<String> serviceServers = serviceConfiguration.getServerList();

            if(serviceServers == null)
                serviceServers = new ArrayList<String>();

            List<String> newServiceServers = new ArrayList<String>();
            newServiceServers.addAll(serviceServers);

            if(managerServers == null)
                continue;
            for(String server : managerServers) {
                if(isAlive(server) && !serviceServers.contains(server)) {
                    newServiceServers.add(server);
                    logger.info("add new server in cluster " + managerClusterKey + " server " + server);
                    change = true;
                }
            }
            if(change) {
                serviceConfiguration.setServerList(newServiceServers);
                setServiceConfiguration(managerClusterKey, serviceConfiguration);
                change = false;
            }
        }

        change = false;
        // 根据监控情况下线
        for(String serviceCluserKey : serviceConfig.keySet()) {
            CacheConfiguration managerConfiguration = managerConfig.get(serviceCluserKey);
            CacheConfiguration serviceConfiguration = serviceConfig.get(serviceCluserKey);
            List<String> managerServers = managerConfiguration.getServerList();
            List<String> serviceServers = serviceConfiguration.getServerList();

            if(serviceServers == null)
                serviceServers = new ArrayList<String>();

            List<String> newServiceServers = new ArrayList<String>();
            newServiceServers.addAll(serviceServers);
            for(String server : serviceServers) {
                if(!isAlive(server) || !managerServers.contains(server)) { // 死了 或者 管理员下线了
                    newServiceServers.remove(server);
                    logger.info("remove new server in cluster " + serviceCluserKey + " server " + server);
                    change = true;
                }
            }
            if(change) {
                serviceConfiguration.setServerList(newServiceServers);
                setServiceConfiguration(serviceCluserKey, serviceConfiguration);
                change = false;
            }
        }


    }


    private void init() throws Exception {
        // 创建必要的四个节点 路径如下
        String[] paths = {Constants.SERVICE_PATH, Constants.MANAGER_PATH};
        for(String node : paths) {
            if(curatorClient.checkExists().forPath(node) == null)
                curatorClient.create().creatingParentsIfNeeded().forPath(node);
        }
    }

    public void start() throws Exception {
        final Runnable task = new Runnable() {
            @Override
            public void run() {
                for(TaskRunner t : serverStatMap.values())
                    taskRunnerThreadPool.submit(t);
                try {
                    logger.info("start sync machine state");
                    syncMachineState(); // 把管理员的更改同步到内部状态中
                } catch (Exception e) {
                    logger.error("syncMachineState error ");
                }
            }
        };
        // TODO: 这个地方是间隔
        int interval = 5000;
        monitorThreadPool.scheduleWithFixedDelay(task, interval, interval, TimeUnit.MILLISECONDS);
    }

    private void copyToAlpha(String betaPath, String alphaPath, CuratorFramework betaClient, CuratorFramework alphaClient) throws Exception {
        List<String> children = betaClient.getChildren().forPath(betaPath);

        if(children == null || children.size() == 0) {
            byte[] data = betaClient.getData().forPath(betaPath);
            try {
                alphaClient.create().creatingParentsIfNeeded().forPath(alphaPath, data);
            } catch (Exception e) {
                alphaClient.setData().forPath(alphaPath, data);
            }
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

    public void removeAllData() throws Exception {
        String[] paths = {Constants.MANAGER_PATH};
        for(String path : paths) {
            if(curatorClient.checkExists().forPath(path) != null) {
                zkDelete(path);
            }
        }
    }
    public void addNewServer() throws Exception {
        String s = "192.168.8.45:11212";
        String or = "10.66.11.117:11211";
        String cluster = "memcached-yy";
        CacheConfiguration cacheConfiguration = getManagerClusterCinfiguration(cluster);
        List<String> servers = cacheConfiguration.getServerList();
        servers.add(s);
        cacheConfiguration.setServerList(servers);
        setManagerConfiguration(cluster, cacheConfiguration);
    }
    public void prepareManagerData() throws Exception {
        String path = Constants.MANAGER_PATH + "/" + "memcached-tuangou";
        String testClusterInfo = "{\"cacheKey\":\"memcached-tuangou\",\"clientClazz\":\"com.dianping.cache.memcached.MemcachedClientImpl\",\"servers\":\"10.66.11.117:11211\",\"transcoderClazz\":\"com.dianping.cache.memcached.HessianTranscoder\",\"addTime\":1452755302947,\"serverList\":[\"10.66.11.117:11211\"]}";
        curatorClient.create().creatingParentsIfNeeded().forPath(path, testClusterInfo.getBytes("UTF-8"));
    }

    public static void main(String[] args) {
        try {
            TaskManager taskManager = new TaskManager();
//            taskManager.removeAllData();
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
