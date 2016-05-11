package com.dianping.squirrel.dao;

import com.dianping.squirrel.entity.HulkClusterConfig;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/10.
 */
@Repository
public interface HulkClusterConfigDao {
    void insert(HulkClusterConfig config);
    void update(HulkClusterConfig config);
    void delete(HulkClusterConfig config);
    HulkClusterConfig find(String clusterName);
    List<HulkClusterConfig> findAll();
}
