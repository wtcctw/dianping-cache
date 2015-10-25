package com.dianping.cache.util;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.utils.EnsurePath;
import org.apache.curator.utils.ZKPaths;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;

import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.entity.CacheKeyConfiguration;
import com.dianping.cache.service.CacheConfigurationService;
import com.dianping.cache.service.CacheKeyConfigurationService;
import com.dianping.remote.cache.dto.CacheKeyTypeVersionUpdateDTO;
import com.dianping.squirrel.common.util.JsonUtils;
import com.dianping.squirrel.common.util.ZKUtils;

public class Migrator implements InitializingBean{

    private static Logger logger = LoggerFactory.getLogger(Migrator.class);
    
    private CacheConfigurationService cacheConfigService;
    
    private CacheKeyConfigurationService cacheKeyConfigService;
    
    private String zkAddress;
    private CuratorFramework curatorClient;
    
    public void migrate() {
        logger.info("start to migrate cache configs from MySQL to ZK...");
        try {
            doMigrate();
            logger.info("done.");
        } catch (Exception e) {
            logger.error("failed.", e);
        }
    }
    
    private void doMigrate() throws Exception {
        migrateServiceConfig();
        logger.info("migrated cache service configs");
        migrateCategoryConfig();
        logger.info("migrated cache category configs");
    }
    
    private void migrateServiceConfig() throws Exception {
        ensurePath(ZKUtils.CACHE_SERVICE_PATH);
        List<CacheConfiguration> serviceConfigs = cacheConfigService.findAll();
        for(CacheConfiguration serviceConfig : serviceConfigs) {
            String json = JsonUtils.toStr(serviceConfig);
            String path = ZKUtils.getServicePath(serviceConfig.getCacheKey());
            updateNode(path, json);
        }
    }

    private void migrateCategoryConfig() throws Exception {
        ensurePath(ZKUtils.CACHE_CATEGORY_PATH);
        List<CacheKeyConfiguration> categoryConfigs = cacheKeyConfigService.findAll();
        for(CacheKeyConfiguration categoryConfig : categoryConfigs) {
            if(StringUtils.isEmpty(categoryConfig.getCategory()))
                continue;
            String path = ZKUtils.getCategoryPath(categoryConfig.getCategory());
            String json = JsonUtils.toStr(categoryConfig);
            updateNode(path, json);
            CacheKeyTypeVersionUpdateDTO versionDto = new CacheKeyTypeVersionUpdateDTO();
            versionDto.setMsgValue(categoryConfig.getCategory());
            versionDto.setVersion("" + categoryConfig.getVersion());
            versionDto.setAddTime(categoryConfig.getAddTime());
            updateNode(path + "/version", JsonUtils.toStr(versionDto));
            if("web".equals(categoryConfig.getCacheType())) {
                ensurePath(path + "/keys");
                ensurePath(path + "/key");
            }
        }
    }

    private void updateNode(String path, String json) throws Exception {
        try {
            curatorClient.setData().forPath(path, json.getBytes("UTF-8"));
        } catch(NoNodeException e) {
            curatorClient.create().creatingParentsIfNeeded().forPath(path, json.getBytes("UTF-8"));
        }
    }
    
    private void ensurePath(String path) throws Exception {
        ZKPaths.mkdirs(curatorClient.getZookeeperClient().getZooKeeper(), path);
//        EnsurePath ep = new EnsurePath(path);
//        ep.ensure(curatorClient.getZookeeperClient());
    }
    
    public void setCacheConfigService(CacheConfigurationService cacheConfigService) {
        this.cacheConfigService = cacheConfigService;
    }

    public void setCacheKeyConfigService(CacheKeyConfigurationService cacheKeyConfigService) {
        this.cacheKeyConfigService = cacheKeyConfigService;
    }

    public void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        if(StringUtils.isBlank(zkAddress))
            throw new NullPointerException("cache zookeeper address is empty");
        curatorClient = CuratorFrameworkFactory.newClient(zkAddress, 60*1000, 30*1000, 
                new RetryNTimes(Integer.MAX_VALUE, 1*1000));
        curatorClient.getConnectionStateListenable().addListener(new ConnectionStateListener() {

            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                logger.info("cache zookeeper {} state changed to {}", zkAddress, newState);
            }
            
        });
        curatorClient.start();
    }

    public static void main(String[] args) throws Exception {
        DOMConfigurator.configure(new DefaultResourceLoader().getResource("classpath:log/log4j.xml").getURL());
        ApplicationContext ac = new ClassPathXmlApplicationContext("classpath*:config/spring/appcontext-*.xml");
        Migrator migrator = (Migrator) ac.getBean("migrator");
        migrator.migrate();
    }
}
