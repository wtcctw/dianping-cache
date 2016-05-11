package com.dianping.squirrel.cluster;

import java.util.ArrayList;
import java.util.List;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/3.
 */
public class ClusterNode {
    private Cluster cluster;
    private DataNode master;
    private List<DataNode> slaves;

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public DataNode getMaster() {
        return master;
    }

    public void setMaster(DataNode master) {
        this.master = master;
    }

    public List<DataNode> getSlaves() {
        return slaves;
    }

    public void setSlaves(List<DataNode> slaves) {
        this.slaves = slaves;
    }

    public synchronized void addSlave(DataNode slave){
        if(slaves == null){
            slaves = new ArrayList<DataNode>();
        }
        slaves.add(slave);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClusterNode)) return false;

        ClusterNode that = (ClusterNode) o;

        if (!getMaster().equals(that.getMaster())) return false;
        return !(getSlaves() != null ? !getSlaves().equals(that.getSlaves()) : that.getSlaves() != null);

    }

    @Override
    public int hashCode() {
        int result = getMaster().hashCode();
        result = 31 * result + (getSlaves() != null ? getSlaves().hashCode() : 0);
        return result;
    }
}
