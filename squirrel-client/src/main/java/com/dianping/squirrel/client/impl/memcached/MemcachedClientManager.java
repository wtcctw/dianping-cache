package com.dianping.squirrel.client.impl.memcached;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.HashSet;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.DefaultConnectionFactory;
import net.spy.memcached.MemcachedClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.squirrel.common.config.ConfigManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;
import com.dianping.squirrel.common.exception.StoreInitializeException;

public class MemcachedClientManager {

    private static Logger logger = LoggerFactory.getLogger(MemcachedClientManager.class);

    private static final ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    private static final long DEFAULT_OP_QUEUE_MAX_BLOCK_TIME = 10; // milliseconds

    private static final String PROP_OP_QUEUE_LEN = "avatar-cache.spymemcached.queuesize";

    private static final String PROP_READ_BUF_SIZE = "avatar-cache.spymemcached.readbufsize";

    private static final String PROP_OP_QUEUE_MAX_BLOCK_TIME = "avatar-cache.spymemcached.queueblocktime";

    private static int opQueueLen = configManager.getIntValue(PROP_OP_QUEUE_LEN,
            DefaultConnectionFactory.DEFAULT_OP_QUEUE_LEN);

    private static int readBufSize = configManager.getIntValue(PROP_READ_BUF_SIZE,
            DefaultConnectionFactory.DEFAULT_READ_BUFFER_SIZE);

    private static long opQueueMaxBlockTime = configManager.getLongValue(PROP_OP_QUEUE_MAX_BLOCK_TIME,
            DEFAULT_OP_QUEUE_MAX_BLOCK_TIME);

    private static final int POOLSIZE_READ = configManager.getIntValue("avatar-cache.spymemcached.poolsize.read", 3) > 0 ? configManager
            .getIntValue("avatar-cache.spymemcached.poolsize.read", 3) : 3;

    private static final int POOLSIZE_WRITE = configManager.getIntValue("avatar-cache.spymemcached.poolsize.write", 1) > 0 ? configManager
            .getIntValue("avatar-cache.spymemcached.poolsize.write", 1) : 1;

    private static final boolean USE_SHARED_POOL = configManager.getBooleanValue(
            "avatar-cache.spymemcached.pool.shared", false);

    private String storeType;

    private MemcachedClientConfig config;

    private MemcachedClient readClient;

    private MemcachedClient writeClient;

    private MemcachedClient[] readClients;

    private MemcachedClient[] writeClients;

    public MemcachedClientManager(String storeType, MemcachedClientConfig config) {
        checkNotNull(storeType, "store type is null");
        checkNotNull(config, "memcached client config is null");
        checkNotNull(config.getServers(), "memcached server address is null");
        this.storeType = storeType;
        this.config = config;
    }

    public MemcachedClient getReadClient() {
        if (this.readClient != null)
            return this.readClient;
        try {
            int idx = (int) (Math.random() * POOLSIZE_READ);
            return readClients[idx];
        } catch (RuntimeException e) {
        }
        return readClients[0];
    }

    public MemcachedClient getWriteClient() {
        if (this.writeClient != null)
            return this.writeClient;
        try {
            int idx = (int) (Math.random() * POOLSIZE_WRITE);
            return writeClients[idx];
        } catch (RuntimeException e) {
        }
        return writeClients[0];
    }

    public void start() {
        try {
            ExtendedConnectionFactory connectionFactory = new ExtendedKetamaConnectionFactory(opQueueLen, readBufSize,
                    opQueueMaxBlockTime);
            if (config.getTranscoder() != null) {
                if (config.getTranscoder() instanceof MemcachedTranscoder) {
                    ((MemcachedTranscoder) config.getTranscoder()).setCacheType(storeType);
                }
                connectionFactory.setTranscoder(config.getTranscoder());
            } else {
                connectionFactory.setTranscoder(new MemcachedTranscoder(storeType));
            }

            String[] serverSplits = config.getServers().split("\\|");
            String mainServer = serverSplits[0].trim();
            if (USE_SHARED_POOL) {
                MemcachedClient client = new MemcachedClient(connectionFactory, AddrUtil.getAddresses(mainServer));
                this.readClient = client;
                this.writeClient = client;
            } else {
                readClients = new MemcachedClient[POOLSIZE_READ];
                for (int i = 0; i < POOLSIZE_READ; i++) {
                    MemcachedClient client = new MemcachedClient(connectionFactory, AddrUtil.getAddresses(mainServer));
                    readClients[i] = client;
                }
                writeClients = new MemcachedClient[POOLSIZE_WRITE];
                for (int i = 0; i < POOLSIZE_WRITE; i++) {
                    MemcachedClient client = new MemcachedClient(connectionFactory, AddrUtil.getAddresses(mainServer));
                    writeClients[i] = client;
                }
            }

            logger.info("memcached store client initialized: " + mainServer);
        } catch (Exception e) {
            throw new StoreInitializeException(e);
        }
    }

    public void stop() {
        if (readClient != null) {
            readClient.shutdown();
        }
        if (writeClient != null) {
            writeClient.shutdown();
        }
        if (readClients != null) {
            for (MemcachedClient client : readClients) {
                if (client != null) {
                    client.shutdown();
                }
            }
        }
        if (writeClients != null) {
            for (MemcachedClient client : writeClients) {
                if (client != null) {
                    client.shutdown();
                }
            }
        }
    }

    public Collection<MemcachedClient> getMemcachedClients() {
        Collection<MemcachedClient> memcachedClients = new HashSet<MemcachedClient>();
        if (readClient != null) {
            memcachedClients.add(readClient);
        }
        if (writeClient != null) {
            memcachedClients.add(writeClient);
        }
        if (readClients != null) {
            for (MemcachedClient client : readClients) {
                if (client != null) {
                    memcachedClients.add(client);
                }
            }
        }
        if (writeClients != null) {
            for (MemcachedClient client : writeClients) {
                if (client != null) {
                    memcachedClients.add(client);
                }
            }
        }
        return memcachedClients;
    }

}
