package com.dianping.squirrel.cluster.redis;

import com.dianping.squirrel.cluster.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/3.
 */
public class Info {
    private static Logger logger = LoggerFactory.getLogger(Info.class);

    private CPUInfo cpuInfo;
    private ClientsInfo clientsInfo;
    private KeyspaceInfo keyspaceInfo;
    private MemoryInfo memoryInfo;
    private PersistenceInfo persistenceInfo;
    private ReplicationInfo replicationInfo;
    private ServerInfo serverInfo;
    private StatsInfo statsInfo;


    public Info(String info) {
        try {
            Map<String,String> data = RedisUtil.parseStringToMap(info);
            cpuInfo = new CPUInfo(data);
            clientsInfo = new ClientsInfo(data);
            keyspaceInfo = new KeyspaceInfo(data);
            memoryInfo = new MemoryInfo(data);
            persistenceInfo = new PersistenceInfo(data);
            replicationInfo = new ReplicationInfo(data);
            statsInfo = new StatsInfo(data);
        } catch (Exception e) {
            logger.error("get Info error",e);
        }
    }

    public CPUInfo getCpuInfo() {
        return cpuInfo;
    }

    public void setCpuInfo(CPUInfo cpuInfo) {
        this.cpuInfo = cpuInfo;
    }

    public ClientsInfo getClientsInfo() {
        return clientsInfo;
    }

    public void setClientsInfo(ClientsInfo clientsInfo) {
        this.clientsInfo = clientsInfo;
    }

    public KeyspaceInfo getKeyspaceInfo() {
        return keyspaceInfo;
    }

    public void setKeyspaceInfo(KeyspaceInfo keyspaceInfo) {
        this.keyspaceInfo = keyspaceInfo;
    }

    public MemoryInfo getMemoryInfo() {
        return memoryInfo;
    }

    public void setMemoryInfo(MemoryInfo memoryInfo) {
        this.memoryInfo = memoryInfo;
    }

    public PersistenceInfo getPersistenceInfo() {
        return persistenceInfo;
    }

    public void setPersistenceInfo(PersistenceInfo persistenceInfo) {
        this.persistenceInfo = persistenceInfo;
    }

    public ReplicationInfo getReplicationInfo() {
        return replicationInfo;
    }

    public void setReplicationInfo(ReplicationInfo replicationInfo) {
        this.replicationInfo = replicationInfo;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public StatsInfo getStatsInfo() {
        return statsInfo;
    }

    public void setStatsInfo(StatsInfo statsInfo) {
        this.statsInfo = statsInfo;
    }

    @Override
    public String toString() {
        return "Info{" +
                "cpuInfo=" + cpuInfo +
                ", clientsInfo=" + clientsInfo +
                ", keyspaceInfo=" + keyspaceInfo +
                ", memoryInfo=" + memoryInfo +
                ", persistenceInfo=" + persistenceInfo +
                ", replicationInfo=" + replicationInfo +
                ", serverInfo=" + serverInfo +
                ", statsInfo=" + statsInfo +
                '}';
    }

    public static void main(String[] args) {
        Jedis jedis = new Jedis("127.0.0.1",6379);
        String s = jedis.info();
        Info info = new Info(s);
        System.out.println(info);
    }
}
