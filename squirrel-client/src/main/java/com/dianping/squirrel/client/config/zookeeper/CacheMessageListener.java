package com.dianping.squirrel.client.config.zookeeper;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorEventType;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.remote.cache.dto.CacheConfigurationDTO;
import com.dianping.remote.cache.dto.CacheKeyConfigurationDTO;
import com.dianping.remote.cache.dto.CacheKeyTypeVersionUpdateDTO;
import com.dianping.remote.cache.dto.SingleCacheRemoveDTO;
import com.dianping.remote.cache.util.SedesUtils;
import com.dianping.squirrel.client.config.listener.CacheConfigurationUpdateListener;
import com.dianping.squirrel.client.config.listener.CacheKeyConfigUpdateListener;
import com.dianping.squirrel.client.config.listener.CacheKeyTypeVersionUpdateListener;
import com.dianping.squirrel.client.config.listener.SingleCacheRemoveListener;
import com.dianping.squirrel.client.config.zookeeper.CacheEvent.CacheEventType;
import com.dianping.squirrel.common.util.JsonUtils;
import com.dianping.squirrel.common.util.PathUtils;

public class CacheMessageListener implements CuratorListener {

    private static Logger logger = LoggerFactory.getLogger(CacheMessageListener.class);

    public static final String CAT_EVENT_TYPE = "Squirrel.client";
    public static final String CACHE_SERVICE_PATH = "/dp/cache/service/";
    public static final String CACHE_CATEGORY_PATH = "/dp/cache/category/";
    public static final String VERSION_SUFFIX = "/version";
    public static final String EXTENSION_SUFFIX = "/extension";
    public static final String KEY_SUFFIX = "/key";
    public static final String BATCH_KEY = "/keys/";

    private CacheKeyTypeVersionUpdateListener versionChangeListener = new CacheKeyTypeVersionUpdateListener();
    private SingleCacheRemoveListener keyRemoveListener = new SingleCacheRemoveListener();
    private CacheConfigurationUpdateListener serviceChangeListener = new CacheConfigurationUpdateListener();
    private CacheKeyConfigUpdateListener categoryChangeListener = new CacheKeyConfigUpdateListener();
    
    private ConcurrentMap<String, List<String>> pathChildrenMap =  new ConcurrentHashMap<String, List<String>>();

    @Override
    public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
        if (event == null) {
            logger.warn("curator event is null");
            return;
        }

        if (CuratorEventType.WATCHED == event.getType()) {
            WatchedEvent we = event.getWatchedEvent();
            if (we == null) {
                logger.warn("zookeeper event is null");
                return;
            }

            if (we.getType() == EventType.None) {
                return;
            }
            
            String path = we.getPath();
            try {
                if(we.getType() == EventType.NodeDataChanged || we.getType() == EventType.NodeCreated) {
                    processDataChanged(client, path);
                } else if(we.getType() == EventType.NodeChildrenChanged) {
                    // cache server added, removed or ip address changed will trigger this event
                    processChildrenChanged(client, path);
                } else {
                    // we.getType() == EventType.NodeDeleted
                    watch(client, path);
                }
            } catch (Exception e) {
                logger.error("error in cache message listener, path: " + path, e);
            }
        }
    }

    public void watchChildren(CuratorFramework curatorClient, String parentPath) throws Exception {
        if(!pathChildrenMap.containsKey(parentPath)) {
            ZKPaths.mkdirs(curatorClient.getZookeeperClient().getZooKeeper(), parentPath);
            List<String> children = curatorClient.getChildren().watched().forPath(parentPath);
            if(children != null) {
                pathChildrenMap.put(parentPath, children);
            }
            
            if(children != null && children.size() > 0) {
                for(String child : children) {
                    watch(curatorClient, parentPath + "/" + child);
                }
            }
        }
    }
    
    private void processChildrenChanged(CuratorFramework client, String path) throws Exception {
        List<String> children = null;
        try {
            children = client.getChildren().watched().forPath(path);
        } catch(NoNodeException e) {
            ZKPaths.mkdirs(client.getZookeeperClient().getZooKeeper(), path);
            children = client.getChildren().watched().forPath(path);
        }
        
        if(children == null) {
            return;
        }
        
        List<String> oldChildren = pathChildrenMap.get(path);
        pathChildrenMap.put(path, children);
        
        if(oldChildren != null)
            children.removeAll(oldChildren);
        
        for(String child : children) {
            String childPath = path + "/" + child;
            processDataChanged(client, childPath);
        }
    }

    private void processDataChanged(CuratorFramework client, String path) throws Exception {
        String content = getData(client, path, true);
        logger.info(String.format("received store notification, path: %s, content: %s", path, content));
        if(content == null) {
            return;
        }
        CacheEvent ce = parseEvent(path, content);
        if (ce == null) {
            logger.error(String.format("failed to parse store event, path: %s, content: %s", path, content));
            return;
        }
        if(ce.getContent() instanceof CacheKeyConfigurationDTO) {
            // get extension
            CacheKeyConfigurationDTO categoryConfig = (CacheKeyConfigurationDTO)ce.getContent();
            String extension = getData(client, PathUtils.getExtensionPath(categoryConfig.getCategory()), false);
            categoryConfig.setExtension(extension);
        }
        dispatchCacheEvent(ce);
    }
    
    private CacheEvent parseEvent(String path, String content) throws Exception {
        CacheEvent ce = null;
        if (path.startsWith(CACHE_CATEGORY_PATH)) {
            ce = new CacheEvent();
            if (path.endsWith(VERSION_SUFFIX)) {
                ce.setType(CacheEventType.VersionChange);
                ce.setContent(JsonUtils.fromStr(content, CacheKeyTypeVersionUpdateDTO.class));
            } else if (path.endsWith(KEY_SUFFIX)) {
                ce.setType(CacheEventType.KeyRemove);
                ce.setContent(JsonUtils.fromStr(content, SingleCacheRemoveDTO.class));
            } else if (path.contains(BATCH_KEY)) {
                ce.setType(CacheEventType.BatchKeyRemove);
                ce.setContent(SedesUtils.deserialize(content));
            } else {
                ce.setType(CacheEventType.CategoryChange);
                ce.setContent(JsonUtils.fromStr(content, CacheKeyConfigurationDTO.class));
            }
        } else if (path.startsWith(CACHE_SERVICE_PATH)) {
            ce = new CacheEvent();
            ce.setType(CacheEventType.ServiceChange);
            ce.setContent(JsonUtils.fromStr(content, CacheConfigurationDTO.class));
            return ce;
        }
        return ce;
    }

    public boolean dispatchCacheEvent(CacheEvent ce) {
        switch (ce.getType()) {
        case VersionChange:
            CacheKeyTypeVersionUpdateDTO versionChange = (CacheKeyTypeVersionUpdateDTO) ce.getContent();
            if(CacheMessageManager.takeMessage(versionChange)) {
                Cat.logEvent(CAT_EVENT_TYPE, "clear.category:" + versionChange.getMsgValue(), "0", versionChange.getVersion());
                versionChangeListener.handleMessage(versionChange);
                return true;
            }
            return false;
        case KeyRemove:
            SingleCacheRemoveDTO keyRemove = (SingleCacheRemoveDTO) ce.getContent();
            Cat.logEvent(CAT_EVENT_TYPE, "clear.key:" + PathUtils.getCategoryFromKey(keyRemove.getCacheKey()));
            keyRemoveListener.handleMessage(keyRemove);
            return true;
        case BatchKeyRemove:
            List<SingleCacheRemoveDTO> keyRemoves = (List<SingleCacheRemoveDTO>) ce.getContent();
            if(keyRemoves == null || keyRemoves.size() == 0) {
                return false;
            }
            String category = PathUtils.getCategoryFromKey(keyRemoves.get(0).getCacheKey());
            for(SingleCacheRemoveDTO singleKeyRemove : keyRemoves) {
                Cat.logEvent(CAT_EVENT_TYPE, "clear.key:" + category);
                keyRemoveListener.handleMessage(singleKeyRemove);
            }
            return true;
        case CategoryChange:
            CacheKeyConfigurationDTO categoryChange = (CacheKeyConfigurationDTO) ce.getContent();
            if(CacheMessageManager.takeMessage(categoryChange)) {
                Cat.logEvent(CAT_EVENT_TYPE, "category.change:"+categoryChange.getCategory(), "0", "" + ce.getContent());
                categoryChangeListener.handleMessage(categoryChange);
                return true;
            }
            return false;
        case ServiceChange:
            CacheConfigurationDTO serviceChange = (CacheConfigurationDTO) ce.getContent();
            if(CacheMessageManager.takeMessage(serviceChange)) {
                Cat.logEvent(CAT_EVENT_TYPE, "service.change:" + serviceChange.getCacheKey(), "0", "" + ce.getContent());
                serviceChangeListener.handleMessage(serviceChange);
                return true;
            }
            return false;
        default:
            logger.error("invalid store event");
            return false;
        }
    }

    private String getData(CuratorFramework client, String path, boolean watch) throws Exception {
        try {
            byte[] data = null;
            if (watch) {
                data = client.getData().watched().forPath(path);
            } else {
                data = client.getData().forPath(path);
            }
            return new String(data, "UTF-8");
        } catch (NoNodeException e) {
            logger.info("path " + e.getPath() + " does not exist");
            if (watch) {
                client.checkExists().watched().forPath(path);
            }
            return null;
        }
    }

    private void watch(CuratorFramework client, String path) throws Exception {
        client.checkExists().watched().forPath(path);
    }
    
}
