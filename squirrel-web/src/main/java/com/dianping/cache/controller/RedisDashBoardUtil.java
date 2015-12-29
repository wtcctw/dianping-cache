package com.dianping.cache.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import redis.clients.jedis.Jedis;

import com.dianping.cache.monitor.statsdata.RedisClusterData;

import static com.dianping.cache.scale.cluster.redis.RedisManager.getClusterCache;

public class RedisDashBoardUtil {

    public static List<RedisClusterData> getClusterData() {
        List<RedisClusterData> data = new ArrayList<RedisClusterData>();

        for (Map.Entry<String, com.dianping.cache.scale.cluster.redis.RedisCluster> cluster : getClusterCache().entrySet()) {
            RedisClusterData tmp = new RedisClusterData();
            tmp.setClusterName(cluster.getKey());
            com.dianping.cache.scale.cluster.redis.RedisCluster redisCluster = cluster.getValue();
            long maxmemory = 0, usedmemory = 0;
            int masterNum = 0,slaveNum = 0;
            int ops = 0;

            for (com.dianping.cache.scale.cluster.redis.RedisNode node : redisCluster.getNodes()) {
                maxmemory += node.getMaster().getInfo().getMaxMemory();
                usedmemory += node.getMaster().getInfo().getUsedMemory();
                node.getMaster().setSlotList(null);
                masterNum++;
                ops += node.getMaster().getInfo().getQps();
                if(node.getSlave() != null){
                    slaveNum++;
                    node.getSlave().setSlotList(null);
                }
                if(node.getMaster().getMigrating()){
                    tmp.setMigrate(true);
                }
                //tmp.setMigrate(true);
            }
            tmp.setQps(ops);
            tmp.setMasterNum(masterNum);
            tmp.setSlaveNum(slaveNum);
            tmp.setNodes(redisCluster.getNodes());
            tmp.setMaxMemory(maxmemory/1024);
            tmp.setUsedMemory(usedmemory/1024);
            tmp.setFailedServers(cluster.getValue().getFailedServers());
            float used = (float) usedmemory / maxmemory;
            used = convert(used);
            tmp.setUsed(used);
            tmp.check();

            data.add(tmp);
        }
        return data;
    }

    public static Map<String, Object> getRedisServerData(String address) {
        String[] server = address.split(":");
        Jedis jedis = new Jedis(server[0],6379);
        String info = jedis.info();
        jedis.close();
        return parseRedisInfo(info);
    }

    private static Map<String, Object> parseRedisInfo(String infoString) {
        Map<String, Object> data = new HashMap<String, Object>();
        String[] infoArray = infoString.split("\r\n");
        for (String info : infoArray) {
            info.trim();
            String[] each = info.split(":");
            if (each.length > 1)
                data.put(each[0], each[1]);
        }
        return data;
    }

    private static float convert(float value) {
        int tmp = Math.round(value * 10000);
        return (float) (tmp / 100.0);
    }

}