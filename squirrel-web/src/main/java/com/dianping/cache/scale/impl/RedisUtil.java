package com.dianping.cache.scale.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisClusterException;

import com.dianping.cache.autoscale.AppId;
import com.dianping.cache.autoscale.AutoScale;
import com.dianping.cache.autoscale.Instance;
import com.dianping.cache.autoscale.Result;
import com.dianping.cache.autoscale.dockerscale.DockerScale;
import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.entity.Server;
import com.dianping.cache.scale.ScaleException;
import com.dianping.cache.service.CacheConfigurationService;
import com.dianping.cache.service.ServerService;
import com.dianping.cache.support.spring.SpringLocator;
import com.dianping.cache.util.ParseServersUtil;

public class RedisUtil {
	
    public static final String SLOT_IN_TRANSITION_IDENTIFIER = "[";
    public static final String SLOT_IMPORTING_IDENTIFIER = "--<--";
    public static final String SLOT_MIGRATING_IDENTIFIER = "-->--";
    public static final long CLUSTER_SLEEP_INTERVAL = 100;
    public static final int CLUSTER_DEFAULT_TIMEOUT = 300;
    public static final int CLUSTER_MIGRATE_NUM = 100;
    public static final int CLUSTER_DEFAULT_DB = 0;
    public static final String UNIX_LINE_SEPARATOR = "\r\n";
    public static final String WINDOWS_LINE_SEPARATOR = "\r\n";
    public static final String COLON_SEPARATOR = ":";
	
	private static Logger logger = LoggerFactory.getLogger(RedisUtil.class);
	
	private static AutoScale autoScale = new DockerScale();
	
	private static CacheConfigurationService cacheConfigurationService  = SpringLocator.getBean("cacheConfigurationService");
	
	private static volatile int operateId = 0;
	
	private static Map<Integer,Integer> scaleStatus = new HashMap<Integer,Integer>();
	
	

	public static int applyNodes(String appId,int number){
		return autoScale.scaleUp(AppId.valueOf(appId), number);
	}
	
	public static Result getResult(int operateId){
		return autoScale.getValue(operateId);
	}
	
	public static int scaleNode(String cluster,int nodeNum,String appId) throws ScaleException{
		int totalNum = nodeNum * 2; // totalNum = master + slave
		int scaleOperationId = operateId++;
		scaleStatus.put(scaleOperationId, 100);
		autoScale(scaleOperationId,appId,cluster,totalNum);
		return scaleOperationId;
	}
	
	
	public static int scaleSlave(String masterAddress){
		int scaleOperationId = operateId++;
		scaleStatus.put(scaleOperationId, 100);
		//TODO
		autoScaleSlave(scaleOperationId, masterAddress);
		return scaleOperationId;
	}

	public static int deleteMaster(String cluster,String address){
		int scaleOperationId = operateId++;
		scaleStatus.put(scaleOperationId, 100);
		//TODO
		return scaleOperationId;
	}
	
	public static int deleteSlave(String cluster,String address){
		int scaleOperationId = operateId++;
		scaleStatus.put(scaleOperationId, 100);
		//TODO
		return scaleOperationId;
	}
	
	public static int getOperateStatus(int scaleOperationId){
		return scaleStatus.get(scaleOperationId);
	}
	
	
	private static void autoScale(final int scaleOperationId,final String appId,final String cluster,final int totalNum){
		
		Runnable run = new Runnable() {
			@Override
			public void run() {
//				int operateid = .scaleUp(appId, totalNum);
//				Result result = autoScale.getValue(operateid);
//				while(result.getStatus() == 100){
//					try {
//						Thread.sleep(100);
//						result = autoScale.getValue(operateid);
//					} catch(InterruptedException e){
//					}
//				}
				Result result = new Result();
				result.setStatus(200);
				scaleStatus.put(scaleOperationId, result.getStatus());
				if(result.getStatus() == 200){
					//autoScale.destroy(result);
					joinCluster(scaleOperationId,cluster,result);
				}
			}
		};
		Thread t = new Thread(run);
		t.start();
		
	}
	
	
	private static void joinCluster(final int scaleOperationId, final String cluster, final Result result) {
		CacheConfiguration config = cacheConfigurationService.find(cluster);
		if (config == null  || config.getServers() == null) {
			logger.error("Get " + cluster + " config from DB error! config is not correct!");
			scaleStatus.put(scaleOperationId, 500);
			autoScale.destroy(result);
			return;
		}
		String url = config.getServers();
		List<String> servers = ParseServersUtil.parseRedisServers(url);
		List<String> joinServers;// = loadNode(result); // TODO 捕获 DuplicateHostInNodeException 异常
		joinServers = new ArrayList<String>();
		joinServers.add("127.0.0.1:7000");
		joinServers.add("127.0.0.1:7007");
		RedisScaler rs = new RedisScaler(servers, joinServers);
		try {
			rs.scaleUp();
		} catch (ScaleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			scaleStatus.put(scaleOperationId, 500);
		}
		scaleStatus.put(scaleOperationId, 600);
	}
	
	private static void autoScaleSlave(final int scaleOperationId,final String masterAddress) {
		// TODO Auto-generated method stub
		ServerService serverService = SpringLocator.getBean("serverService");
		Server server = serverService.findByAddress(masterAddress);
		final AppId appId = AppId.valueOf(server.getAppId());
		
		Runnable run = new Runnable() {
			@Override
			public void run() {
				int operateid = autoScale.scaleUp(appId, 1);
				Result result = autoScale.getValue(operateid);
				while(result.getStatus() == 100){
					try {
						Thread.sleep(100);
						result = autoScale.getValue(operateid);
					} catch(InterruptedException e){
					}
				}
				
				scaleStatus.put(scaleOperationId, result.getStatus());
				if(result.getStatus() == 200){
					//autoScale.destroy(result);
					joinMaster(scaleOperationId,masterAddress,result);
				}
			}
		};
		Thread t = new Thread(run);
		t.start();
	}
	
	private static void joinMaster(int scaleOperationId, String masterAddress,Result result) {
		// TODO Auto-generated method stub
		
	}
	
	private static List<String> loadNode(Result result) {
		// TODO 分配主从节点 ：  分配节点应遵循 所有的主节点分散在不同的物理机上， 主从节点不能在同一台物理机上
		List<String> join = new ArrayList<String>();
		for(Instance ins : result.getInstances()){
			join.add(ins.getIp() + ":" + result.getAppid().getPort());
		}
		/**如果两个主从节点在同一台物理机上   则 throws   DuplicateHostInNodeException 
		 **/
		/**分配算法   利用一个桶  TreeMap 将宿主机ip相同的都放到同一个桶中，第一次选取master  依次将桶中的ip 放入返回结果中
		 * 第二次  从下标为 1 的map中依次选取 slave
		 * 分配完成后检测有没有 主从在同一物理机的   如果有  index＋1 重复第二步  直到index == TreeMap size
		 **/
		
		
		return join;
	}
	
    public static String getNodeId(final RedisServer node) {
    	Jedis jedis = RedisConnectionFactory.getConnection(node.getAddress());
        String[] clusterInfo = jedis.clusterNodes().split("\n");
        for (String lineInfo: clusterInfo) {
            if (lineInfo.contains("myself")) {
                return lineInfo.split(" ")[0];
            }
        }
        return null;
    }
    
    
    public static void waitForClusterReady(final List<RedisNode> clusterNodes) {
        boolean clusterOk = false;
        while (!clusterOk) {
            clusterOk = true;
            for (RedisNode rNode: clusterNodes) {
            	if(rNode.getMaster() != null){
            		Jedis master = RedisConnectionFactory.getConnection(rNode.getMaster().getAddress());
            		String clusterInfo = master.clusterInfo();
                    String firstLine = clusterInfo.split(UNIX_LINE_SEPARATOR)[0];
                    String[] firstLineArr = firstLine.trim().split(COLON_SEPARATOR);
                    if (firstLineArr[0].equalsIgnoreCase("cluster_state") &&
                            firstLineArr[1].equalsIgnoreCase("ok")) {
                        if(rNode.getSlave() != null){
                        	Jedis slave = RedisConnectionFactory.getConnection(rNode.getSlave().getAddress());
                        	clusterInfo = slave.clusterInfo();
                        	firstLine = clusterInfo.split(UNIX_LINE_SEPARATOR)[0];
                        	firstLineArr = firstLine.split(COLON_SEPARATOR);
                        	if (firstLineArr[0].equalsIgnoreCase("cluster_state") &&
                        			firstLineArr[1].equalsIgnoreCase("ok")) {
                        		continue;
                        	}
                        	 clusterOk = false;
                             break;
                        }
                        continue;
                    }
                    clusterOk = false;
                    break;
            	}
            	
            }

            if (clusterOk) {
                break;
            }

            try {
                TimeUnit.MILLISECONDS.sleep(CLUSTER_SLEEP_INTERVAL);
            } catch (InterruptedException e) {
                throw new JedisClusterException("waitForClusterReady", e);
            }
        }
    }

	public static void destroy(String address) {
		autoScale.scaleDown(address);
	}

	public static void des(String instanceId) {
		autoScale.destroyByInstanceId(instanceId);
	}
}
