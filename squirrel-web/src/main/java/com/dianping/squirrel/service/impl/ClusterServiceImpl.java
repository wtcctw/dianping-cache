package com.dianping.squirrel.service.impl;

import com.dianping.cache.dao.OperationLogDao;
import com.dianping.squirrel.cluster.DataNode;
import com.dianping.squirrel.cluster.redis.Info;
import com.dianping.squirrel.dao.HulkClusterConfigDao;
import com.dianping.squirrel.entity.HulkClusterConfig;
import com.dianping.squirrel.service.HulkApiService;
import com.dianping.squirrel.service.ClusterService;
import com.dianping.squirrel.vo.ScaleParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/3.
 */
@Service
public class ClusterServiceImpl implements ClusterService {

    private static Logger logger = LoggerFactory.getLogger(ClusterServiceImpl.class);

    @Autowired
    private HulkClusterConfigDao hulkClusterConfigDao;

    @Autowired
    private HulkApiService cloudApiService;

    @Autowired
    private OperationLogDao operationLogDao;

    @Override
    public List<HulkClusterConfig> getAllHulkClusterConfig() {
        return hulkClusterConfigDao.findAll();
    }

    @Override
    public void createCluter(ScaleParams scaleParams) {
        //check cluster is exist?

        // if master slave in one zone
            // get master slave number,zone -> scale;
            // int rescode = cloudApiService.scaleOut(scaleParams);
            // Result = cloudApiService.scaleOut(scaleParams,rescode);
            // createInOneZone(Result)
        // else
            // get master number , zone  ->   scale;
            // get slave number , zone -> scale;
            // create()

    }

    @Override
    public Info getDataNodeInfo(DataNode dataNode) {
        Info info = null;
        Jedis connection = null;
        try {
            connection = getConnection(dataNode);
            String infoStr = connection.info();
            info = new Info(infoStr);
        } catch (Exception e) {
            logger.error("getDataNodeInfo error.",e);
        } finally {
            if(connection != null){
                connection.close();
            }
        }
        return info;
    }

    @Override
    public HulkClusterConfig getHulkClusterConfig(String clusterName) {
        return hulkClusterConfigDao.find(clusterName);
    }

    private Jedis getConnection(DataNode dataNode){
        Jedis jedis = new Jedis(dataNode.getIp(),dataNode.getPort());
        return jedis;
    }
}
