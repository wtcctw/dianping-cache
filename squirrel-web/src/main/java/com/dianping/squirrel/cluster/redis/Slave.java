package com.dianping.squirrel.cluster.redis;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/3.
 */
public class Slave {
    private String ip;
    private int port;
    private String state;
    private long offset;
    private int lag;

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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public int getLag() {
        return lag;
    }

    public void setLag(int lag) {
        this.lag = lag;
    }

    @Override
    public String toString() {
        return "Slave{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", state='" + state + '\'' +
                ", offset=" + offset +
                ", lag=" + lag +
                '}';
    }
}
