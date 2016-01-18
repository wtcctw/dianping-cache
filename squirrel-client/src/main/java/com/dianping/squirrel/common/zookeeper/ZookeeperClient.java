package com.dianping.squirrel.common.zookeeper;

import java.util.Collections;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.data.Stat;

import com.dianping.squirrel.common.lifecycle.Lifecycle;

public class ZookeeperClient implements Lifecycle {

    private String zkAddress;
    
    private ZookeeperListener zkListener;
    
    private CuratorFramework zkClient;

    public ZookeeperClient(String zkAddress) {
        this(zkAddress, null);
    }

    public ZookeeperClient(String zkAddress, ZookeeperListener zkListener) {
        this.zkAddress = zkAddress;
        this.zkListener = zkListener;
    }

    @Override
    public void start() {
        zkClient = CuratorFrameworkFactory.newClient(zkAddress, 60 * 1000, 30 * 1000, 
                new RetryNTimes(3, 1000));
        if(zkListener != null) {
            zkClient.getCuratorListenable().addListener(zkListener);
        }
        zkClient.start();
    }

    @Override
    public void stop() {
        if(zkClient != null) {
            zkClient.close();
            zkClient = null;
        }
    }
    
    public boolean exists(String path) throws Exception {
        Stat stat = zkClient.checkExists().forPath(path);
        return stat != null;
    }
    
    public void set(String path, String value) throws Exception {
        byte[] bytes = value.getBytes("UTF-8");
        try {
            zkClient.setData().forPath(path, bytes);
        } catch(NoNodeException e) {
            zkClient.create().creatingParentsIfNeeded().forPath(path, bytes);
        }
    }
    
    public String get(String path) throws Exception {
        try {
            byte[] data = zkClient.getData().forPath(path);
            return new String(data, "UTF-8");
        } catch (NoNodeException e) {
            return null;
        }
    }
    
    public List<String> getChildren(String path) throws Exception {
        try {
            List<String> children = zkClient.getChildren().forPath(path);
            return children;
        } catch(NoNodeException e) {
            return Collections.emptyList();
        }
    }
    
    public void ensurePath(String path) throws Exception {
        try {
            zkClient.create().creatingParentsIfNeeded().forPath(path);
        } catch(NodeExistsException e) {
        }
    }
    
    public void delete(String path) throws Exception {
        try {
            zkClient.delete().forPath(path);
        } catch(NoNodeException e) {
        }
    }
    
}
