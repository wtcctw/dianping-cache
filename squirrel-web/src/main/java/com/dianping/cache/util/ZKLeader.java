package com.dianping.cache.util;

import com.dianping.squirrel.common.config.ConfigManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.retry.RetryNTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/1/20.
 */
public class ZKLeader {
    private static Logger logger = LoggerFactory.getLogger(ZKLeader.class);

    private static String KEY_ZOOKEEPER_ADDRESS = "avatar-cache.zookeeper.address";

    private static String KEY_LEADER_PATH = "/dp/cache/leader";

    private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    private static CuratorFramework curatorClient;

    private static LeaderLatch leaderLatch;

    static {
        try {
            String zkAddress = configManager.getStringValue(KEY_ZOOKEEPER_ADDRESS);
            if (StringUtils.isBlank(zkAddress))
                throw new NullPointerException("cache zookeeper address is empty");
            curatorClient = CuratorFrameworkFactory.newClient(zkAddress, 60 * 1000, 30 * 1000,
                    new RetryNTimes(3, 1000));
            leaderLatch = new LeaderLatch(curatorClient,KEY_LEADER_PATH);
            curatorClient.start();
            leaderLatch.start();
        } catch (Exception e) {
            logger.error("init leaderLatch client failed.",e);
        }
    }

    public static boolean isLeader(){
        boolean isleader;
        try {
            isleader = leaderLatch.hasLeadership();
        } catch (Throwable e) {
            logger.error("get leader failed,",e);
            isleader = false;
        }
        return isleader;
    }

}
