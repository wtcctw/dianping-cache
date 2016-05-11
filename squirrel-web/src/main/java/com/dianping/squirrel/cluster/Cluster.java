package com.dianping.squirrel.cluster;

import redis.clients.jedis.HostAndPort;

import java.util.ArrayList;
import java.util.List;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/3.
 */
public class Cluster {
    private String clusterName;
    private String password;
    private List<ClusterNode> clusterNodes;
    private List<HostAndPort> hostAndPorts;

    public Cluster(String clusterName, String password, List<ClusterNode> clusterNodes, List<HostAndPort> hostAndPorts) {
        this.clusterName = clusterName;
        this.password = password;
        this.clusterNodes = clusterNodes;
        this.hostAndPorts = hostAndPorts;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public List<ClusterNode> getNodes() {
        return clusterNodes;
    }

    public void setNodes(List<ClusterNode> clusterNodes) {
        this.clusterNodes = clusterNodes;
    }

    public List<HostAndPort> getHostAndPorts() {
        return hostAndPorts;
    }

    public void setHostAndPorts(List<HostAndPort> hostAndPorts) {
        this.hostAndPorts = hostAndPorts;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<DataNode> getDataNodes(){
        List<DataNode> dataNodes = new ArrayList<DataNode>();
        for(ClusterNode clusterNode : clusterNodes){
            dataNodes.add(clusterNode.getMaster());
            if(clusterNode.getSlaves() != null)
                dataNodes.addAll(clusterNode.getSlaves());
        }
        return dataNodes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cluster)) return false;

        Cluster cluster = (Cluster) o;

        return clusterNodes.equals(cluster.clusterNodes);

    }

    @Override
    public int hashCode() {
        return clusterNodes.hashCode();
    }
}
