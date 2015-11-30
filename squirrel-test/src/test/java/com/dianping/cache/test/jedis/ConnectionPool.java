package com.dianping.cache.test.jedis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Test;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Created by dp on 15/11/28.
 */
public class ConnectionPool {

    @Test
    public void testConnection(){

        String LocalHost = "127.0.0.1";
        Set<HostAndPort> hps = new HashSet<HostAndPort>();
        hps.add(new HostAndPort(LocalHost, 7000));
        hps.add(new HostAndPort(LocalHost, 7001));
        hps.add(new HostAndPort(LocalHost, 7002));
        hps.add(new HostAndPort(LocalHost, 7003));

        GenericObjectPoolConfig conf = new GenericObjectPoolConfig();
        conf.setMaxWaitMillis(1000);
        conf.setMaxTotal(1);
        conf.setMaxIdle(1);
        conf.setTestOnBorrow(false);
        conf.setTestOnReturn(false);
        conf.setTestWhileIdle(true);
        conf.setMinEvictableIdleTimeMillis(60000);
        conf.setTimeBetweenEvictionRunsMillis(30000);
        conf.setNumTestsPerEvictionRun(-1);


        JedisCluster jc = new JedisCluster(hps,10,100,1,conf);

        for(int i = 0; i < 10000; i++){
            jc.set("key_"+i,i+"");
        }


        while(true){
            try{
                String value = jc.get("key_"+new Random().nextInt(10000));
                System.out.println(value);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

    }



}
