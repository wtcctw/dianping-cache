package com.dianping.cache.remote.jms;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.dianping.cat.Cat;
import com.dianping.remote.cache.dto.CacheConfigurationDTO;
import com.dianping.remote.cache.dto.CacheKeyConfigurationDTO;
import com.dianping.remote.cache.dto.CacheKeyTypeVersionUpdateDTO;
import com.dianping.remote.cache.dto.SingleCacheRemoveDTO;
import com.dianping.remote.cache.util.SedesUtils;
import com.dianping.squirrel.common.config.ConfigManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;
import com.dianping.squirrel.common.util.BoundedLinkedList;
import com.dianping.squirrel.common.util.JsonUtils;
import com.dianping.squirrel.common.util.ZKUtils;
import com.geekhua.filequeue.Config;
import com.geekhua.filequeue.FileQueue;
import com.geekhua.filequeue.FileQueueImpl;

public class CacheMessageNotifier implements Serializable, InitializingBean, MQSender {

    private static final long serialVersionUID = 1L;

    private static final String CAT_EVENT_TYPE = "Cache.notifications";
    
    private static final String KEY_ZOOKEEPER_ENABLED = "avatar-cache.zookeeper.enabled";
    private static final String KEY_BATCH_REMOVE_INTERVAL = "avatar-cache.batch.remove.interval";
    private static final String KEY_MAX_KEYS_PER_CATEGORY = "avatar-cache.max.keys.per.category";
    private static final String KEY_SINGLE_REMOVE_ENABLE = "avatar-cache.single.remove.enable";

    private static final boolean DEFAULT_ZOOKEEPER_ENABLED = true;
    private static final long DEFAULT_BATCH_REMOVE_INTERVAL = 1500;
    private static final int DEFAULT_MAX_KEYS_PER_CATEGORY = 5000;
    private static final boolean DEFAULT_SINGLE_REMOVE_ENABLE = false;
    
    private Logger logger = LoggerFactory.getLogger(CacheMessageNotifier.class);

    private ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    
    private boolean isZookeeperEnabled;
    private String zkAddress;
    private CuratorFramework curatorClient;
    
    private boolean enableSingleRemove;
    private long keyRemoveInterval;
    private Map<String, List<SingleCacheRemoveDTO>> keyRemoveBuffer;
    private int maxKeysPerCategory;
    private long lastKeyRemoveTime = System.currentTimeMillis();
    
    private FileQueue<SingleCacheRemoveDTO> cacheKeyQueue;
    
    public CacheMessageNotifier() {
        isZookeeperEnabled = configManager.getBooleanValue(KEY_ZOOKEEPER_ENABLED, DEFAULT_ZOOKEEPER_ENABLED);
        keyRemoveInterval = configManager.getLongValue(KEY_BATCH_REMOVE_INTERVAL, DEFAULT_BATCH_REMOVE_INTERVAL);
        keyRemoveBuffer = new HashMap<String, List<SingleCacheRemoveDTO>>();
        maxKeysPerCategory = configManager.getIntValue(KEY_MAX_KEYS_PER_CATEGORY, DEFAULT_MAX_KEYS_PER_CATEGORY);
        enableSingleRemove = configManager.getBooleanValue(KEY_SINGLE_REMOVE_ENABLE, DEFAULT_SINGLE_REMOVE_ENABLE);
        Thread t = new Thread("cache-batch-key-remove-thread") {
            public void run() {
                doBatchKeyRemove();
            }
        };
        t.start();
        logger.info("single key remove is " + (enableSingleRemove ? "enabled" : "disabled"));
        if(enableSingleRemove) {
            Config config = new Config();
            config.setName("cache-key-queue");
            cacheKeyQueue = new FileQueueImpl<SingleCacheRemoveDTO>(config);
            Thread t2 = new Thread("cache-key-remove-thread") {
                public void run() {
                    doKeyRemove();
                }
            };
            t2.start();
        }
    }
    
    @Override
    public void sendMessageToTopic(Object msg) {
        if(isZookeeperEnabled) {
            long start = System.currentTimeMillis();
            if(msg instanceof CacheKeyTypeVersionUpdateDTO) {
                notifyVersionChange((CacheKeyTypeVersionUpdateDTO)msg);
            } else if(msg instanceof SingleCacheRemoveDTO) {
                addToKeyRemoveBuffer((SingleCacheRemoveDTO)msg);
                if(enableSingleRemove) {
                    try {
                        cacheKeyQueue.add((SingleCacheRemoveDTO)msg);
                    } catch (Exception e) {
                        logger.error("failed to add to file queue", e);
                    }
                }
            } else if(msg instanceof CacheConfigurationDTO) {
                ((CacheConfigurationDTO) msg).setKey(null);
                ((CacheConfigurationDTO) msg).setDetail(null);
                notifyServiceConfigChange((CacheConfigurationDTO)msg);
            } else if(msg instanceof CacheKeyConfigurationDTO) {
                notifyCategoryConfigChange((CacheKeyConfigurationDTO)msg);
            } else {
                logger.warn("unknown message");
            }
            long span = System.currentTimeMillis() - start;
            if(span > 100) {
                logger.warn("sendMessageToZK took " + span);
            }
        }
    }

    public void notifyServiceConfigChange(CacheConfigurationDTO serviceConfig) {
        String path = ZKUtils.getServicePath(serviceConfig.getCacheKey());
        try {
            String content = JsonUtils.toStr(serviceConfig);
            updateNode(path, content);
            Cat.logEvent(CAT_EVENT_TYPE, "service.change:" + serviceConfig.getCacheKey(),
                    "0", serviceConfig.toString());
        } catch (Exception e) {
            Cat.logEvent(CAT_EVENT_TYPE, "service.change:" + serviceConfig.getCacheKey(),
                    "-1", e.getMessage());
            logger.error("failed to notify service config change: " + serviceConfig, e);
        }
    }
    
    public void notifyCategoryConfigChange(CacheKeyConfigurationDTO categoryConfig) {
        String path = ZKUtils.getCategoryPath(categoryConfig.getCategory());
        String versionPath = ZKUtils.getVersionPath(categoryConfig.getCategory());
        String extPath = ZKUtils.getExtensionPath(categoryConfig.getCategory());
        try {
            String content = JsonUtils.toStr(categoryConfig);
            String versionContent = getVersionContent(categoryConfig);
            String extContent = categoryConfig.getExtension();
            if(curatorClient.checkExists().forPath(path) == null) {
                curatorClient.create().creatingParentsIfNeeded().forPath(path, content.getBytes("UTF-8"));
                curatorClient.create().forPath(versionPath, versionContent.getBytes("UTF-8"));
                curatorClient.create().forPath(extPath, extContent == null ? new byte[0] : extContent.getBytes("UTF-8")); 
            } else {
                // update extension
                try {
                    curatorClient.setData().forPath(extPath, extContent == null ? new byte[0] : extContent.getBytes("UTF-8"));
                } catch(NoNodeException e) {
                    logger.warn("extension path {} dose not exist", extPath);
                    curatorClient.create().forPath(extPath, extContent == null ? new byte[0] : extContent.getBytes("UTF-8"));
                }
                // update category
                curatorClient.setData().forPath(path, content.getBytes("UTF-8"));
                // update version if changed
                try {
                    String currentVersionContent = new String(curatorClient.getData().forPath(versionPath), "UTF-8");
                    if(StringUtils.isBlank(currentVersionContent)) {
                        curatorClient.create().forPath(versionPath, versionContent.getBytes("UTF-8"));
                    } else {
                        CacheKeyTypeVersionUpdateDTO versionChange = JsonUtils.fromStr(currentVersionContent, CacheKeyTypeVersionUpdateDTO.class);
                        int version = Integer.valueOf(versionChange.getVersion());
                        if(version != categoryConfig.getVersion()) {
                            logger.warn("cache category config changed, and version is not equal");
                            curatorClient.setData().forPath(versionPath, versionContent.getBytes("UTF-8"));
                        }
                    }
                } catch(NoNodeException e) {
                    logger.warn("version path {} dose not exist", versionPath);
                    curatorClient.create().forPath(versionPath, versionContent.getBytes("UTF-8"));
                }
            }
            Cat.logEvent(CAT_EVENT_TYPE, "category.change:" + categoryConfig.getCategory(), "0", categoryConfig.toString());
        } catch (Exception e) {
            Cat.logEvent(CAT_EVENT_TYPE, "category.change:" + categoryConfig.getCategory(), "-1", e.getMessage());
            logger.error("failed to notify category config change: " + categoryConfig, e);
        }
    }

    private String getVersionContent(CacheKeyConfigurationDTO categoryConfig) throws Exception {
        CacheKeyTypeVersionUpdateDTO version = new CacheKeyTypeVersionUpdateDTO();
        version.setMsgValue(categoryConfig.getCategory());
        version.setVersion(""+categoryConfig.getVersion());
        version.setAddTime(categoryConfig.getAddTime());
        return JsonUtils.toStr(version);
    }

    public void notifyVersionChange(CacheKeyTypeVersionUpdateDTO message) {
        String path = ZKUtils.getVersionPath(message.getMsgValue());
        try {
            String content = JsonUtils.toStr(message);
            updateNode(path, content);
            Cat.logEvent(CAT_EVENT_TYPE, "clear.category:" + message.getMsgValue(), "0", message.getVersion());
        } catch (Exception e) {
            Cat.logEvent(CAT_EVENT_TYPE, "clear.category:" + message.getMsgValue(), "-1", e.getMessage());
            logger.error("failed to notify cache version change: " + message, e);
        }
    }

    public void notifyKeyRemove(SingleCacheRemoveDTO message) {
        String category = ZKUtils.getCategoryFromKey(message.getCacheKey());
        String path = ZKUtils.getKeyPath(category);
        try {
            String content = JsonUtils.toStr(message);
            long start = System.currentTimeMillis();
            updateNode(path, content);
            long time = System.currentTimeMillis() - start;
            if(time > 25) {
                logger.warn("notifyKeyRemove.update took " + time);
            }
            Cat.logEvent(CAT_EVENT_TYPE, "clear.key:" + ZKUtils.getCategoryFromKey(message.getCacheKey()), 
                    "0", message.getCacheKey());
        } catch (Exception e) {
            Cat.logEvent(CAT_EVENT_TYPE, "clear.key:" + ZKUtils.getCategoryFromKey(message.getCacheKey()), 
                    "-1", e.getMessage());
            logger.error("failed to notify cache key remove: " + message, e);
        }
    }

    public void addToKeyRemoveBuffer(SingleCacheRemoveDTO message) {
        String category = ZKUtils.getCategoryFromKey(message.getCacheKey());
        if(category != null) {
            synchronized(this) {
                List<SingleCacheRemoveDTO> list = keyRemoveBuffer.get(category);
                if(list == null) {
                    list = new BoundedLinkedList<SingleCacheRemoveDTO>(maxKeysPerCategory) {
                        protected void overflow(SingleCacheRemoveDTO keyRemove) {
                            logger.error("key remove event overflow! drop event: " + keyRemove);
                        }
                    };
                    keyRemoveBuffer.put(category, list);
                }
                list.add(message);
            }
        }
    }
    
    private void doKeyRemove() {
        int count = 0;
        while(!Thread.interrupted()) {
            try {
                SingleCacheRemoveDTO keyRemove = cacheKeyQueue.get();
                if(++count % 100 == 0)
                    Thread.sleep(1);
                notifyKeyRemove(keyRemove);
            } catch (InterruptedException e) {
                break;
            } catch (IOException e) {
                logger.error("failed to get from file queue", e);
            } catch (Exception e) {
                logger.error("failed to do key remove", e);
            }
        }
    }
    
    private void doBatchKeyRemove() {
        while(!Thread.interrupted()) {
            try {
                long now = System.currentTimeMillis();
                long elapsed = now - lastKeyRemoveTime;
                if(elapsed >= keyRemoveInterval) {
                    int count = consumeKeyRemoveBuffer();
                    lastKeyRemoveTime = now;
                    long span = System.currentTimeMillis() - now;
                    if(logger.isDebugEnabled()) {
                        logger.debug(String.format("doKeyRemove removed %s keys in %s ms", count, span));
                    }
                    if(count > 100 || span > 100) {
                        logger.warn(String.format("doKeyRemove removed %s keys in %s ms", count, span));
                    }
                } else {
                    Thread.sleep(keyRemoveInterval - elapsed);
                }
            } catch (InterruptedException e) {
                break;
            }
        }
    }
    
    private int consumeKeyRemoveBuffer() {
        Map<String, List<SingleCacheRemoveDTO>> processBuffer = null;
        synchronized(this) {
            if(keyRemoveBuffer != null & keyRemoveBuffer.size() > 0) {
                processBuffer = keyRemoveBuffer;
                keyRemoveBuffer = new HashMap<String, List<SingleCacheRemoveDTO>>();
            }
        }
        int count = 0;
        if(processBuffer != null) {
            for(Map.Entry<String, List<SingleCacheRemoveDTO>> entry : processBuffer.entrySet()) {
                List<SingleCacheRemoveDTO> removeKeyList = entry.getValue();
                if(removeKeyList == null || removeKeyList.size() == 0) {
                    logger.error("remove key list for " + entry.getKey() + " is empty");
                    continue;
                }
                count += removeKeyList.size();
                String path = ZKUtils.getBatchKeyPath(entry.getKey());
                try {
                    String content = SedesUtils.serialize(removeKeyList);
                    updateNode(path, content);
                    if(logger.isDebugEnabled()) {
                        logger.debug("updated key remove path " + path + 
                                ", count " + removeKeyList.size() +
                                ", size " + content.length());
                    }
                } catch (Exception e) {
                    logger.error("failed to update key remove path " + path, e);
                }
            }
        }
        return count;
    }

    private void updateNode(String path, String content) throws Exception {
        try {
            curatorClient.setData().forPath(path, content.getBytes("UTF-8"));
        } catch(NoNodeException e) {
            curatorClient.create().creatingParentsIfNeeded().forPath(path, content.getBytes("UTF-8"));
        }
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

}
