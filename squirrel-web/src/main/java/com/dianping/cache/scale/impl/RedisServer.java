package com.dianping.cache.scale.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisDataException;

import com.dianping.cache.scale.Server;

public class RedisServer extends Server {

    private String id;
    
    private Set<String> flags = new HashSet<String>();
    
    private String slotString;
    
    private List<Integer> slotList = new ArrayList<Integer>();

    private String masterId;
    
    private RedisInfo info = new RedisInfo();
    
    private Logger logger = LoggerFactory.getLogger(getClass());

    public RedisServer(String address) {
        super(address);
    }

    public RedisServer(String ip,int port){
        super(ip,port);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /*
     * Flags is a comma separated string
     * Known flag: myself, master, slave, fail?, fail, handshake, noaddr, noflags
     */
    public void setFlags(String flagStr) {
        flags.clear();
        for(String flag : flagStr.split(",")) {
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
        return flags.contains("fail") && !flags.contains("fail?");
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
        return !isPartialFail() && !isFail() && !isHandShake() && !isNoAddr() && !isNoFlags();
    }

    public void setSlots(String[] slots) {
        this.slotString = null;
        for(String segment : slots) {
            if(this.slotString == null) {
                this.slotString = segment;
            } else {
                this.slotString += ("," + segment);
            }
        }
        slotList = updateSlotList(slotString);
    }
    
    public String getSlotString() {
        return slotString;
    }
    
    public List<Integer> getSlotList() {
        return slotList;
    }
    
    public void setSlotList(List<Integer> slotList) {
        this.slotList = slotList;
        slotString = updateSlotString(slotList);
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }

    public String getMasterId() {
        return masterId;
    }
    
    List<Integer> updateSlotList(String slotString) {
    	if(slotString == null)
    		return null;
        String[] segments = slotString.split(",");
        List<Integer> slotList = new ArrayList<Integer>();
        for(String segment : segments) {
            segment = segment.trim();
            if(StringUtils.isEmpty(segment))
                continue;
            int idx = segment.indexOf('-');
            if(idx == -1) {
                slotList.add(Integer.parseInt(segment));
            } else if(segment.startsWith("[")){ //正在传输
            	//TODO
            } else{
            	int end = Integer.parseInt(segment.substring(idx + 1).trim());
            	int start = Integer.parseInt(segment.substring(0, idx).trim());
            	if(end < start) {
            		start = start ^ end; end = start ^ end; start = start ^ end;
            	}
            	for(int i=start; i<=end; i++) {
            		slotList.add(i);
            	}
            	
            }
        }
        // deduplicate
        Collections.sort(slotList);
        List<Integer> newList = new ArrayList<Integer>();
        if(slotList.size() > 0) {
            newList.add(slotList.get(0));
        }
        for(int i=1; i<slotList.size(); i++) {
            int n = slotList.get(i);
            if(n != slotList.get(i-1)) {
                newList.add(n);
            }
        }
        return newList;
    }
    
    String updateSlotString(List<Integer> slotList) {
        Collections.sort(slotList);
        StringBuilder ss = new StringBuilder();
        int i = 0;
        while(i < slotList.size()) {
            int j = i;
            for(; j<slotList.size()-1 && 
                    (slotList.get(j+1) == slotList.get(j) + 1 || 
                    slotList.get(j+1).equals(slotList.get(j)));
                    j++) {}
            if(j == i || slotList.get(i).equals(slotList.get(j))) {
                ss.append(slotList.get(i)).append(',');
            } else {
                ss.append(slotList.get(i)).append('-').append(slotList.get(j)).append(',');
            }
            i = j+1;
        }
        return ss.length() > 0 ? ss.substring(0, ss.length()-1) : "";
    }
    
    public void loadRedisInfo(){
		Jedis jedis = new Jedis(getIp(), getPort());
		try {
			List<String> redisConfig = jedis.configGet("*");
			String redisInfo = jedis.info();
			Map<String,String> redisConfigMap = parseServerConfig(redisConfig);
			Map<String,String> redisInfoMap = parseServerInfo(redisInfo);
			
			info.setMaxMemory(Long.parseLong(redisConfigMap.get("maxmemory"))/1024/1024);
			info.setUsedMemory(Long.parseLong(redisInfoMap.get("used_memory"))/1024/1024);
		} catch (NumberFormatException e) {
			logger.error(e.toString());
		} catch (Exception e1){
			logger.error("CONFIG alias error :" + e1);
		} finally{
			jedis.close();
		}
    }


    private Map<String,String> parseServerConfig(List<String> config){
    	String quota = "\"";
    	String quotaStr = "&quot;";
    	Map<String,String> result = new HashMap<String,String>();
    	if(config == null)
    		return result;
    	for(int i = 0; i < config.size(); i += 2 ){
    		config.set(i+1,config.get(i+1).replaceAll(quota, ""));
    		config.set(i+1,config.get(i+1).replaceAll(quotaStr,""));
    		result.put(config.get(i), config.get(i+1));
    	}
    	return result;
    }
    
	private Map<String,String> parseServerInfo(String infoString){
		Map<String,String> data = new HashMap<String,String>();
		String[] infoArray = infoString.split("\r\n");
		for(String info : infoArray){
			info.trim();
			String[] each = info.split(":");
			if(each.length > 1)
				data.put(each[0], each[1]);
		}
		return data;
	}

	public RedisInfo getInfo() {
		return info;
	}

	public void setInfo(RedisInfo info) {
		this.info = info;
	}
}
