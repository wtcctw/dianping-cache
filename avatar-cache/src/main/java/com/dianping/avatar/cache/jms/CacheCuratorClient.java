package com.dianping.avatar.cache.jms;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.dianping.avatar.cache.client.RemoteCacheClientFactory;
import com.dianping.avatar.cache.configuration.RemoteCacheItemConfigManager;
import com.dianping.avatar.cache.jms.CacheEvent.CacheEventType;
import com.dianping.cache.config.ConfigChangeListener;
import com.dianping.cache.config.ConfigManager;
import com.dianping.cache.config.ConfigManagerLoader;
import com.dianping.cache.util.JsonUtils;
import com.dianping.cache.util.ZKUtils;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.pigeon.threadpool.NamedThreadFactory;
import com.dianping.remote.cache.dto.CacheConfigurationDTO;
import com.dianping.remote.cache.dto.CacheKeyConfigurationDTO;
import com.dianping.remote.cache.dto.CacheKeyTypeVersionUpdateDTO;

public class CacheCuratorClient {

    public static final String KEY_ZOOKEEPER_ADDRESS = "avatar-cache.zookeeper.address";
    public static final String KEY_ZOOKEEPER_RETRY_INTERVAL = "avatar-cache.zookeeper.retry.interval";
    public static final int DEFAULT_ZOOKEEPER_RETRY_INTERVAL = 1000;
    public static final String KEY_ZOOKEEPER_RETRY_LIMIT = "avatar-cache.zookeeper.retry.limit";
    public static final int DEFAULT_ZOOKEEPER_RETRY_LIMIT = 3;
	public static final String KEY_ZOOKEEPER_SYNC_INTERVAL = "avatar-cache.zookeeper.sync.interval";
	public static final long DEFAULT_ZOOKEEPER_SYNC_INTERVAL = 150 * 1000;
	public static final String KEY_ZOOKEEPER_FAIL_LIMIT = "avatar-cache.zookeeper.fail.limit";
    public static final int DEFAULT_ZOOKEEPER_FAIL_LIMIT = 300;
	public static final String KEY_ZOOKEEPER_PUSH_STEP = "avatar-cache.zookeeper.push.step";
	public static final long DEFAULT_ZOOKEEPER_PUSH_STEP = 2;
	public static final String WEB_CACHE = "web";

	private static Logger logger = LoggerFactory.getLogger(CacheCuratorClient.class);

	private String zkAddress;

	private volatile CuratorFramework curatorClient;

	private CacheMessageListener cacheMessageListener;

	private ExecutorService eventThreadPool;
	
	private ConfigManager configManager = ConfigManagerLoader.getConfigManager();

	private AtomicBoolean isSyncing = new AtomicBoolean(false);
	private volatile long lastSyncTime = System.currentTimeMillis();
	private volatile int retryInterval = configManager.getIntValue(KEY_ZOOKEEPER_RETRY_INTERVAL,
	        DEFAULT_ZOOKEEPER_RETRY_INTERVAL);
	private volatile int retryLimit = configManager.getIntValue(KEY_ZOOKEEPER_RETRY_LIMIT,
	        DEFAULT_ZOOKEEPER_RETRY_LIMIT);
	private volatile int failLimit = configManager.getIntValue(KEY_ZOOKEEPER_FAIL_LIMIT, 
	        DEFAULT_ZOOKEEPER_FAIL_LIMIT);
	private volatile long syncInterval = configManager.getLongValue(KEY_ZOOKEEPER_SYNC_INTERVAL,
			DEFAULT_ZOOKEEPER_SYNC_INTERVAL);
	private volatile long pushStep = configManager.getLongValue(KEY_ZOOKEEPER_PUSH_STEP,
			DEFAULT_ZOOKEEPER_PUSH_STEP);
	
	private int syncCount = 0;

	private int failCount = 0;

	private static CacheCuratorClient instance;
	
	private CacheCuratorClient() {
	    try {
            init();
        } catch (Exception e) {
            logger.error("failed to init cache curator client", e);
        }
	}
	
	public static CacheCuratorClient getInstance() {
	    if(instance == null) {
	        synchronized(CacheCuratorClient.class) {
	            if(instance == null) {
	                instance = new CacheCuratorClient();
	            }
	        }
	    }
	    return instance;
	}
	
	public CacheConfigurationDTO getServiceConfig(String service) throws Exception {
		if (ZKUtils.isZookeeperEnabled() && isZookeeperConnected()) {
			String path = ZKUtils.getServicePath(service);
			String content = getData(path, true);
			if (StringUtils.isBlank(content)) {
				logger.warn("cache service config [" + service + "] is empty");
				return null;
			}
			CacheConfigurationDTO serviceConfig = JsonUtils.fromStr(content, CacheConfigurationDTO.class);
			CacheMessageManager.takeMessage(serviceConfig);
			return serviceConfig;
		}
		return null;
	}

	private boolean isZookeeperConnected() {
        return curatorClient == null ? false : curatorClient.getZookeeperClient().isConnected();
    }

    public CacheKeyConfigurationDTO getCategoryConfig(String category) throws Exception {
		if (ZKUtils.isZookeeperEnabled() && isZookeeperConnected()) {
		    // get category
			CacheKeyConfigurationDTO categoryConfig = _getCategoryConfig(category);
			if (categoryConfig == null) {
				return null;
			}

			// get version
			CacheKeyTypeVersionUpdateDTO versionChange = _getVersionChange(category);
			if (versionChange == null) {
				return null;
			}
			if ("-1".equals(versionChange.getVersion())) {
				// verison -1 means cache category has been removed
				return null;
			}

			CacheMessageManager.takeMessage(versionChange);
			categoryConfig.setVersion(Integer.parseInt(versionChange.getVersion()));
			// get extension
			String extension = _getExtension(category);
			categoryConfig.setExtension(extension);
			CacheMessageManager.takeMessage(categoryConfig);

			
			if (WEB_CACHE.equals(categoryConfig.getCacheType())) {
			    String parentPath = ZKUtils.getBatchKeyParentPath(category);
			    cacheMessageListener.watchChildren(curatorClient, parentPath);
			}
			return categoryConfig;
		}
		return null;
	}

    private CacheKeyConfigurationDTO _getCategoryConfig(String category) throws Exception {
		String path = ZKUtils.getCategoryPath(category);
		String content = getData(path, true);
		if (StringUtils.isBlank(content)) {
			logger.warn("cache category config [" + category + "] is empty");
			return null;
		}
		CacheKeyConfigurationDTO config = JsonUtils.fromStr(content, CacheKeyConfigurationDTO.class);
		return config;
	}

	private CacheKeyTypeVersionUpdateDTO _getVersionChange(String category) throws Exception {
		String path = ZKUtils.getVersionPath(category);
		String content = getData(path, true);
		if (StringUtils.isBlank(content)) {
			logger.warn("cache category version [" + category + "] is empty");
			return null;
		}
		CacheKeyTypeVersionUpdateDTO versionChange = JsonUtils.fromStr(content, CacheKeyTypeVersionUpdateDTO.class);
		return versionChange;
	}
	
	private String _getExtension(String category) throws Exception {
	    String path = ZKUtils.getExtensionPath(category);
	    String content = getData(path, false);
	    return content;
	}

	public String getRuntimeServices(String appName) throws Exception {
	    if(isZookeeperConnected()) {
    		String path = ZKUtils.getRuntimeServicePath(appName);
    		return getData(path, false);
	    } else {
	        return null;
	    }
	}

	public String getRuntimeCategories(String appName) throws Exception {
	    if(isZookeeperConnected()) {
    		String path = ZKUtils.getRuntimeCategoryPath(appName);
    		return getData(path, false);
	    } else {
            return null;
        }
	}

	private String getData(String path, boolean watch) throws Exception {
		try {
			byte[] data = null;
			if (watch) {
				data = curatorClient.getData().watched().forPath(path);
			} else {
				data = curatorClient.getData().forPath(path);
			}
			return new String(data, "UTF-8");
		} catch (NoNodeException e) {
			logger.info("path " + e.getPath() + " does not exist");
			if (watch) {
				curatorClient.checkExists().watched().forPath(path);
			}
			return null;
		}
	}

	private void update(String path, String value) throws Exception {
		byte[] data = value.getBytes("UTF-8");
		if (curatorClient.checkExists().forPath(path) != null) {
			curatorClient.setData().forPath(path, data);
		} else {
			curatorClient.create().creatingParentsIfNeeded().forPath(path, data);
		}
	}

	private void syncAll(boolean force) {
		long now = System.currentTimeMillis();
		if (force || (now - lastSyncTime >= syncInterval)) {
			if (isSyncing.compareAndSet(false, true)) {
				try {
					_syncAll();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (Exception e) {
					logger.error("failed to sync cache events", e);
				} finally {
					lastSyncTime = System.currentTimeMillis();
					syncCount++;
					isSyncing.set(false);
				}
			}
		}
	}

	private void _syncAll() throws Exception {
		if (ZKUtils.isZookeeperEnabled() && isZookeeperConnected()) {
			Transaction t = Cat.getProducer().newTransaction("Cache.sync", "syncAll");
			try {
				syncAllServices();
				syncAllCategories();
				if (syncCount > 0 && syncCount % pushStep == 0) {
					logServices();
					logCategories();
				}
				t.setStatus(Message.SUCCESS);
			} catch (Exception e) {
				t.setStatus(e);
				throw e;
			} finally {
				t.complete();
			}
		}
	}

	private void syncAllServices() throws Exception {
		Set<String> cacheServices = RemoteCacheClientFactory.getInstance().getCacheClientKeys();
		Transaction t = Cat.getManager().getPeekTransaction();
		if (t != null) {
			t.addData("services", StringUtils.join(cacheServices, ','));
		}
		for (String cacheService : cacheServices) {
			CacheConfigurationDTO serviceConfig = getServiceConfig(cacheService);
			if (serviceConfig != null) {
				fireConfigChange(serviceConfig);
			}
		}
	}

	private void syncAllCategories() throws Exception {
		Set<String> cacheCategories = RemoteCacheItemConfigManager.getInstance().getCacheItemKeys();
		Transaction t = Cat.getManager().getPeekTransaction();
		if (t != null) {
			t.addData("categories", StringUtils.join(cacheCategories, ','));
		}
		for (String category : cacheCategories) {
			CacheKeyConfigurationDTO categoryConfig = _getCategoryConfig(category);
			if (categoryConfig == null) {
				continue;
			}

			CacheKeyTypeVersionUpdateDTO versionChange = _getVersionChange(category);
			if (versionChange == null) {
				continue;
			}

			fireVersionChange(versionChange);
			categoryConfig.setVersion(Integer.parseInt(versionChange.getVersion()));
			// get extension
            String extension = _getExtension(category);
            categoryConfig.setExtension(extension);
			fireConfigChange(categoryConfig);

			if (WEB_CACHE.equals(categoryConfig.getCacheType())) {
			    String parentPath = ZKUtils.getBatchKeyParentPath(category);
                cacheMessageListener.watchChildren(curatorClient, parentPath);
            }
		}
	}

	private void logServices() throws Exception {
		Set<String> cacheServices = RemoteCacheClientFactory.getInstance().getCacheClientKeys();
		if (!CollectionUtils.isEmpty(cacheServices)) {
			String appName = configManager.getAppName();
			if (StringUtils.isNotEmpty(appName)) {
				String path = ZKUtils.getRuntimeServicePath(appName);
				String value = StringUtils.join(cacheServices, ',');
				update(path, value);
				logger.info(String.format("logged %s's cache services %s to zookeeper", appName, value));
			}
		}
	}

	private void logCategories() throws Exception {
		Set<String> cacheCategories = RemoteCacheItemConfigManager.getInstance().getCacheItemKeys();
		if (!CollectionUtils.isEmpty(cacheCategories)) {
			String appName = configManager.getAppName();
			if (StringUtils.isNotEmpty(appName)) {
				String path = ZKUtils.getRuntimeCategoryPath(appName);
				String value = StringUtils.join(cacheCategories, ',');
				update(path, value);
				logger.info(String.format("logged %s's cache categories %s to zookeeper", appName, value));
			}
		}
	}

	private void fireConfigChange(CacheConfigurationDTO serviceConfig) {
		CacheEvent ce = new CacheEvent(CacheEventType.ServiceChange, serviceConfig);
		if (cacheMessageListener.dispatchCacheEvent(ce)) {
			Cat.logEvent(CacheMessageListener.CAT_EVENT_TYPE, "zookeeper:missed", Message.SUCCESS,
					serviceConfig.toString());
		}
	}

	private void fireConfigChange(CacheKeyConfigurationDTO categoryConfig) {
		CacheEvent ce = new CacheEvent(CacheEventType.CategoryChange, categoryConfig);
		if (cacheMessageListener.dispatchCacheEvent(ce)) {
			Cat.logEvent(CacheMessageListener.CAT_EVENT_TYPE, "zookeeper:missed", Message.SUCCESS,
					categoryConfig.toString());
		}
	}

	private void fireVersionChange(CacheKeyTypeVersionUpdateDTO versionChange) {
		CacheEvent ce = new CacheEvent(CacheEventType.VersionChange, versionChange);
		if (cacheMessageListener.dispatchCacheEvent(ce)) {
			Cat.logEvent(CacheMessageListener.CAT_EVENT_TYPE, "zookeeper:missed", Message.SUCCESS,
					versionChange.toString());
		}
	}

	public void init() throws Exception {
	    zkAddress = configManager.getStringValue(KEY_ZOOKEEPER_ADDRESS);
        if (StringUtils.isBlank(zkAddress))
            throw new NullPointerException("cache zookeeper address is empty");
		
		configManager.registerConfigChangeListener(new ConfigChangeListener() {

            @Override
            public void onChange(String key, String value) {
                if(KEY_ZOOKEEPER_FAIL_LIMIT.equals(key)) {
                    failLimit = Integer.parseInt(value);
                } else if(KEY_ZOOKEEPER_RETRY_LIMIT.equals(key)) {
                    retryLimit = Integer.parseInt(value);
                } else if(KEY_ZOOKEEPER_RETRY_INTERVAL.equals(key)) {
                    retryInterval = Integer.parseInt(value);
                } else if(KEY_ZOOKEEPER_SYNC_INTERVAL.equals(key)) {
                    syncInterval = Long.parseLong(value);
                } else if(KEY_ZOOKEEPER_PUSH_STEP.equals(key)) {
                    pushStep = Long.parseLong(value);
                }
            }
		    
		});
			
		eventThreadPool = new ThreadPoolExecutor(1, 4, 30L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000),
				new NamedThreadFactory("avatar-cache-event", true), new RejectedExecutionHandler() {
					@Override
					public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
						logger.error("avatar cache event overflow!!!");
					}
				});

		cacheMessageListener = new CacheMessageListener();
		
		curatorClient = newCuratorClient();

		startEventSyncer();
	}

	private void startEventSyncer() {
		Thread t = new Thread(new CacheEventSyncer(), "avatar-cache-event-sync");
		t.setDaemon(true);
		t.start();
	}

	class CacheEventSyncer implements Runnable {

		@Override
		public void run() {
			while (!Thread.interrupted()) {
				try {
					Thread.sleep(1000);
					checkZookeeper();
					syncAll(false);
				} catch (InterruptedException e) {
					logger.warn("cache event sync thread is interrupted");
					break;
				}
			}
		}

	}

    private void checkZookeeper() {
        boolean isConnected = false;
        try {
            isConnected = curatorClient.getZookeeperClient().getZooKeeper().getState().isConnected();
        } catch(Exception e) {
            logger.error("failed to check zookeeper status", e);
        }
        if(isConnected) {
            failCount = 0;
        } else {
            if(++failCount >= failLimit) {
                try {
                    renewCuratorClient();
                    failCount = 0;
                    logger.info("renewed curator client to " + zkAddress);
                    Cat.logEvent(CacheMessageListener.CAT_EVENT_TYPE, "zookeeper:renewSuccess", Message.SUCCESS, "" + failLimit);
                } catch (Exception e) {
                    failCount = failCount / 2;
                    logger.error("failed to renew curator client", e);
                    Cat.logEvent(CacheMessageListener.CAT_EVENT_TYPE, "zookeeper:renewFailure", Message.SUCCESS, e.getMessage());
                }
            }
        }
    }

    private void renewCuratorClient() throws Exception {
        CuratorFramework newCuratorClient = newCuratorClient();
        CuratorFramework oldCuratorClient = this.curatorClient;
        this.curatorClient = newCuratorClient;
        syncAll(true);
        if(oldCuratorClient != null) {
            try {
                oldCuratorClient.close();
            } catch(Exception e) {
                logger.error("failed to close curator client: " + e.getMessage());
            }
        }
    }

    private CuratorFramework newCuratorClient() throws Exception {
        CuratorFramework curatorClient = CuratorFrameworkFactory.newClient(zkAddress, 60 * 1000, 30 * 1000, 
                new RetryNTimes(retryLimit, retryInterval));
        curatorClient.getConnectionStateListenable().addListener(new ConnectionStateListener() {

            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                logger.info(String.format("cache zookeeper %s state changed to %s", zkAddress, newState));
                Cat.logEvent(CacheMessageListener.CAT_EVENT_TYPE, "zookeeper:" + newState);
                if (newState == ConnectionState.RECONNECTED) {
                    syncAll(true);
                }
            }

        }, eventThreadPool);
        curatorClient.getCuratorListenable().addListener(cacheMessageListener, eventThreadPool);
        curatorClient.start();

        if (!curatorClient.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
            // if failed to connect to zookeeper, throw the exception out
            try {
                curatorClient.getZookeeperClient().getZooKeeper();
            } catch(Exception e) {
                // close the client to release resource
                curatorClient.close();
                throw e;
            }
        }
        
        return curatorClient;
    }
}
