package com.dianping.squirrel.service;

import com.dianping.squirrel.cluster.DataNode;
import com.dianping.squirrel.cluster.redis.Info;
import com.dianping.squirrel.entity.HulkClusterConfig;
import com.dianping.squirrel.vo.ScaleParams;

import java.util.List;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/4/18.
 */
public interface ClusterService {
//    ClusterNode createClusterNode();
//    boolean clusterAddNode(String clusterName,int nodeCount);
//    boolean clusterNodeAddSlave(ClusterNode master,ClusterNode slave);
//    boolean clusterNodeRemoveSlave(ClusterNode master, ClusterNode slave);
//    ClusterConfig clusterConfig(String clusterName);
//    void freeClusterNode(ClusterNode clusterNode);
    Info getDataNodeInfo(DataNode dataNode);
    HulkClusterConfig getHulkClusterConfig(String clusterName);
    List<HulkClusterConfig> getAllHulkClusterConfig();
    int createCluter(ScaleParams scaleParams);
}
