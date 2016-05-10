package com.dianping.squirrel.service.impl;

import com.dianping.cache.dao.OperationLogDao;
import com.dianping.cache.entity.OperationLog;
import com.dianping.cache.util.RequestUtil;
import com.dianping.squirrel.cluster.DataNode;
import com.dianping.squirrel.cluster.redis.Info;
import com.dianping.squirrel.dao.HulkClusterConfigDao;
import com.dianping.squirrel.entity.HulkClusterConfig;
import com.dianping.squirrel.service.CloudApiService;
import com.dianping.squirrel.service.ClusterService;
import com.dianping.squirrel.vo.ScaleParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.Date;
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
    private CloudApiService cloudApiService;

    @Autowired
    private OperationLogDao operationLogDao;

    @Override
    public List<HulkClusterConfig> getAllHulkClusterConfig() {
        return hulkClusterConfigDao.findAll();
    }

    @Override
    public int createCluter(ScaleParams scaleParams) {
        int rescode = cloudApiService.scaleOut(scaleParams);
        OperationLog operationLog = new OperationLog();
        operationLog.setContent("create cluster. " + scaleParams);
        operationLog.setOperateTime(new Date());
        operationLog.setOperator(RequestUtil.getUsername());
        operationLogDao.create(operationLog);

        return rescode;
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
