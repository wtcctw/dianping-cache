package com.dianping.squirrel.cluster;

import redis.clients.jedis.HostAndPort;

import java.util.Comparator;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/5.
 */
public class RawDataNode implements Comparator<RawDataNode>{
    private String ip;//实例的ip
    private int port;
    private String hostIp;//宿主机ip

    public RawDataNode(String ip, int port, String hostIp) {
        this.ip = ip;
        this.port = port;
        this.hostIp = hostIp;
    }

    public RawDataNode(String ip, String hostIp){
        this(ip,6379,hostIp);
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

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public HostAndPort getHostAndPort(){
        return new HostAndPort(ip,port);
    }

    @Override
    public String toString() {
        return "RawDataNode{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", hostIp='" + hostIp + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RawDataNode)) return false;

        RawDataNode that = (RawDataNode) o;

        if (getPort() != that.getPort()) return false;
        if (!getIp().equals(that.getIp())) return false;
        return getHostIp().equals(that.getHostIp());

    }

    @Override
    public int hashCode() {
        int result = getIp().hashCode();
        result = 31 * result + getPort();
        result = 31 * result + getHostIp().hashCode();
        return result;
    }

    @Override
    public int compare(RawDataNode o1, RawDataNode o2) {
        return o1.getHostIp().compareToIgnoreCase(o2.getHostIp());
    }
}
