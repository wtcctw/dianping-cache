package com.dianping.cache.util;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.xml.DOMConfigurator;
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
import com.dianping.squirrel.common.domain.CacheKeyTypeVersionUpdateDTO;
import com.dianping.squirrel.common.util.JsonUtils;
import com.dianping.squirrel.common.util.PathUtils;
import com.dianping.squirrel.common.zookeeper.ZookeeperClient;

public class Migrator implements InitializingBean{

    private static Logger logger = LoggerFactory.getLogger(Migrator.class);
    
    private CacheConfigurationService cacheConfigService;
    
    private CacheKeyConfigurationService cacheKeyConfigService;
    
    private String zkAddress;
    private ZookeeperClient zkClient;
    
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
        zkClient.ensurePath(PathUtils.CACHE_SERVICE_PATH);
        List<CacheConfiguration> serviceConfigs = cacheConfigService.findAll();
        for(CacheConfiguration serviceConfig : serviceConfigs) {
            String json = JsonUtils.toStr(serviceConfig);
            String path = PathUtils.getServicePath(serviceConfig.getCacheKey());
            zkClient.set(path, json);
        }
    }

    private void migrateCategoryConfig() throws Exception {
        zkClient.ensurePath(PathUtils.CACHE_CATEGORY_PATH);
        List<CacheKeyConfiguration> categoryConfigs = cacheKeyConfigService.findAll();
        for(CacheKeyConfiguration categoryConfig : categoryConfigs) {
            if(StringUtils.isEmpty(categoryConfig.getCategory()))
                continue;
            String path = PathUtils.getCategoryPath(categoryConfig.getCategory());
            String json = JsonUtils.toStr(categoryConfig);
            zkClient.set(path, json);
            CacheKeyTypeVersionUpdateDTO versionDto = new CacheKeyTypeVersionUpdateDTO();
            versionDto.setMsgValue(categoryConfig.getCategory());
            versionDto.setVersion("" + categoryConfig.getVersion());
            versionDto.setAddTime(categoryConfig.getAddTime());
            zkClient.set(path + "/version", JsonUtils.toStr(versionDto));
            if("web".equals(categoryConfig.getCacheType())) {
                zkClient.ensurePath(path + "/keys");
                zkClient.ensurePath(path + "/key");
            }
        }
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
        zkClient = new ZookeeperClient(zkAddress);
        zkClient.start();
    }

    public static void main(String[] args) throws Exception {
        DOMConfigurator.configure(new DefaultResourceLoader().getResource("classpath:log/log4j.xml").getURL());
        ApplicationContext ac = new ClassPathXmlApplicationContext("classpath*:config/spring/appcontext-*.xml");
        Migrator migrator = (Migrator) ac.getBean("migrator");
        migrator.migrate();
    }
}
