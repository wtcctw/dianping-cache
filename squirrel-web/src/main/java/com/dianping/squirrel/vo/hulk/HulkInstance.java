package com.dianping.squirrel.vo.hulk;

import com.dianping.squirrel.vo.Instance;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/10.
 */
public class HulkInstance implements Instance{
    private String name;
    private String ip;
    private String hostIp;
    private String host;
    private String router;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public String getRouter() {
        return router;
    }

    public void setRouter(String router) {
        this.router = router;
    }
}
