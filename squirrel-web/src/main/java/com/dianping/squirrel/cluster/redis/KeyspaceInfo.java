package com.dianping.squirrel.cluster.redis;

import java.util.HashMap;
import java.util.Map;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/3.
 */
public class KeyspaceInfo extends AbstractInfo {

    //private String db0;
    private long keys;
    private long expires;
    private long avg_ttl;

    public KeyspaceInfo() {
        this.infoSegmentName = "keyspace";
    }

    public KeyspaceInfo(Map<String,String> infoMap) {
        String db0 = infoMap.get("db0");
        if(db0 == null){
            return;
        }
        String[] infoArr = db0.split(",");
        Map<String, String> data = new HashMap<String, String>();
        for(String infoPair : infoArr){
            String[] pair = infoPair.split("=");
            if(pair.length > 1){
                data.put(pair[0],pair[1]);
            }
        }
        this.keys = Long.parseLong(data.get("keys"));
        this.expires = Long.parseLong(data.get("expires"));
        this.avg_ttl = Long.parseLong(data.get("avg_ttl"));
    }

    public long getKeys() {
        return keys;
    }

    public long getExpires() {
        return expires;
    }

    public long getAvg_ttl() {
        return avg_ttl;
    }

    @Override
    public String toString() {
        return "KeyspaceInfo{" +
                "keys=" + keys +
                ", expires=" + expires +
                ", avg_ttl=" + avg_ttl +
                '}';
    }
}
