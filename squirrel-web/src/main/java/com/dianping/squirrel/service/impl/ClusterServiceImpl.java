package com.dianping.squirrel.service.impl;

import com.dianping.squirrel.cluster.DataNode;
import com.dianping.squirrel.cluster.redis.Info;
import com.dianping.squirrel.service.ClusterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/3.
 */
@Service
public class ClusterServiceImpl implements ClusterService {

    private static Logger logger = LoggerFactory.getLogger(ClusterServiceImpl.class);

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

    private Jedis getConnection(DataNode dataNode){
        Jedis jedis = new Jedis(dataNode.getIp(),dataNode.getPort());
        return jedis;
    }
}
