package com.dianping.squirrel.cluster;

import com.dianping.squirrel.cluster.redis.Configration;
import com.dianping.squirrel.cluster.redis.Info;
import com.dianping.squirrel.cluster.redis.Slot;

import java.util.HashSet;
import java.util.Set;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/3.
 */
public class DataNode {
    private ClusterNode clusterNode;
    private String id;
    private String masterId;
    private String ip;
    private int port;
    private String password;
    private Info info;
    private Configration configration;
    private Set<String> flags;
    private Slot slot;


    public DataNode(String ip, int port) {
        this(ip,port,null);
    }

    public DataNode(String ip, int port, String password) {
        this.ip = ip;
        this.port = port;
        this.password = password;
        this.flags = new HashSet<String>();
    }

    public DataNode(String address) {
        String[] ipAndPort = address.split(":");
        this.ip = ipAndPort[0];
        this.port = (ipAndPort.length > 1 ? Integer.parseInt(ipAndPort[1]) : 6379);
    }

    public ClusterNode getClusterNode() {
        return clusterNode;
    }

    public void setClusterNode(ClusterNode clusterNode) {
        this.clusterNode = clusterNode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Configration getConfigration() {
        return configration;
    }

    public void setConfigration(Configration configration) {
        this.configration = configration;
    }

    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }

    public void setFlags(String flagStr) {
        flags.clear();
        for (String flag : flagStr.split(",")) {
            flags.add(flag);
        }
    }

    public boolean isMyself() {
        return flags.contains("myself");
    }

    public boolean isMaster() {
        return flags.contains("master");
    }

    public boolean isSlave() {
        return flags.contains("slave");
    }

    public boolean isPartialFail() {
        return flags.contains("fail?");
    }

    public boolean isFail() {
        return flags.contains("fail");
    }

    public boolean isHandShake() {
        return flags.contains("handshake");
    }

    public boolean isNoAddr() {
        return flags.contains("noaddr");
    }

    public boolean isNoFlags() {
        return flags.contains("noflags");
    }

    public boolean isAlive() {
        return !isPartialFail() && !isFail() && !isHandShake() && !isNoAddr()
                && !isNoFlags();
    }




    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataNode)) return false;

        DataNode dataNode = (DataNode) o;

        if (getPort() != dataNode.getPort()) return false;
        return getIp().equals(dataNode.getIp());

    }

    @Override
    public int hashCode() {
        int result = getIp().hashCode();
        result = 31 * result + getPort();
        return result;
    }
}
