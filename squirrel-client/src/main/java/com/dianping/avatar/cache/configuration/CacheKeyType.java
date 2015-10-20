/**
 * Project: avatar
 * 
 * File Created at 2010-7-15
 * $Id$
 * 
 * Copyright 2010 Dianping.com Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Dianping.com.
 */
package com.dianping.avatar.cache.configuration;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The metadata class for representing business-common configuration files.
 * 
 * @author danson.liu
 * 
 */
public class CacheKeyType {

    private static final Logger logger = LoggerFactory.getLogger(CacheKeyType.class);
    
	/**
	 * Default cache type
	 */
	public final static String DEFAULT_CACHE_TYPE = "memcached";

	/**
	 * Item category
	 */
	private String category;

	/**
	 * Duration(default hour)
	 * support time unit: hour, minute, second
	 * as: 
	 * 		3(h)	3 hours
	 * 		4m		4 minutes
	 *      5s      5 seconds
	 */
	private String duration;

	/**
	 * index template, such as c{0}st{1}rt{2}
	 */
	private String indexTemplate;

	/**
	 * Parameter descriptions
	 */
	private String indexDesc;

	/**
	 * Cache type
	 */
	private String cacheType;

	/**
	 * Version
	 */
	private int version;
	
	private boolean isHot;
	
	private int durationInSeconds = -1;
	
	/**
	 * Extended properties in format: key1=value1&key2=value2...
	 */
	private String extension;
	
	private Map<String, String> extendedProps;

	/**
	 * @param category
	 *            the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	/**
	 * @param indexTemplate
	 *            the indexTemplate to set
	 */
	public void setIndexTemplate(String indexTemplate) {
		this.indexTemplate = indexTemplate;
	}

	/**
	 * @param cacheType
	 *            the cacheType to set
	 */
	public void setCacheType(String cacheType) {
		this.cacheType = cacheType;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersion(int version) {
		this.version = version;
	}

	/**
	 * @return
	 */
	public String getCacheType() {
		return cacheType;
	}

	/**
	 * @return
	 */
	public String getCategory() {
		return category;
	}

	public String getDuration() {
		return duration;
	}

	public int getDurationSeconds() {
		if (durationInSeconds == -1) {
			synchronized (this) {
				if (durationInSeconds == -1) {
				    try {
    					if (duration.endsWith("m")) {
    						int minutes = Integer.parseInt(duration.substring(0, duration.length() - 1));
    						durationInSeconds = minutes * 60;
    					} else if(duration.endsWith("s")) {
    					    durationInSeconds = Integer.parseInt(duration.substring(0, duration.length() - 1));
    					} else {
    						String hourString = duration;
    						if (hourString.endsWith("h")) {
    							hourString = hourString.substring(0, hourString.length() - 1);
    						}
    						int hours = Integer.parseInt(hourString);
    						durationInSeconds = hours * 60 * 60;
    					}
				    } catch(RuntimeException e) {
					    logger.error("failed to parse duration seconds, use default duration 2h!", e);
					    durationInSeconds = 2 * 60 * 60;
					}
				}
			}
		}
		return durationInSeconds;
	}

	public String getIndexTemplate() {
		return indexTemplate;
	}

	/**
	 * @return the indexDesc
	 */
	public String getIndexDesc() {
		return indexDesc;
	}

	/**
	 * @param indexDesc
	 *            the indexDesc to set
	 */
	public void setIndexDesc(String indexDesc) {
		this.indexDesc = indexDesc;
	}

	public String[] getIndexParamDescs() {
		return indexDesc.split("\\|");
	}

	public int getVersion() {
		return version;
	}

	public boolean isHot() {
		return isHot;
	}

	public void setHot(boolean hot) {
		this.isHot = hot;
	}
	
	public String getExtension() {
	    return extension;
	}
	
	public void setExtension(String extension) {
	    this.extension = extension;
	    parseExtension();
	}
    
	private void parseExtension() {
	    if(extension != null) {
	        extendedProps = new HashMap<String, String>();
	        String[] pairs = extension.split("&");
	        for(String pair : pairs) {
	            String[] kv = pair.split("=");
	            if(kv.length == 2) {
	                String key = kv[0].trim();
	                String value = kv[1].trim();
	                if(key.length() > 0 && value.length() > 0) {
	                    extendedProps.put(key, value);
	                }
	            }
	        }
	    }
    }

    public Class getDataTypeClass() {
	    if(extendedProps != null) {
	        String dataType = extendedProps.get("dataType");
	        if(dataType == null) {
	            return Object.class;
	        }
	        if("string".equalsIgnoreCase(dataType)) {
	            return String.class;
	        } else if("integer".equalsIgnoreCase(dataType)) {
	            return Integer.class;
	        } else if("long".equalsIgnoreCase(dataType)) {
	            return Long.class;
	        } else {
	            return Object.class;
	        }
	    }
	    return Object.class;
	}
	
    @Deprecated
	public String getKey2(Object... params) {
		String accessKey = getCategory() + "." + getIndexTemplate() + "_" + getVersion();
		if (params == null) {
			params = new Object[] {null};
		}
		for (int i = 0; i < params.length; i++) {
			accessKey = accessKey.replace("{" + i + "}", params[i].toString());
		}
		return accessKey.replace(" ", "@+~");
	}
	
    /**
     * Return the string key for cache store. Key
     * Rule:{category}.{index}_{version}
     */
	public String getKey(Object... params) {
	    StringBuilder buf = new StringBuilder(128);
	    buf.append(category).append('.');
	    if(indexTemplate != null) {
    	    char[] chars = indexTemplate.toCharArray();
    	    byte state = 0;
    	    for(char c : chars) {
    	        switch(state) {
    	        case 0:
        	        if(c != '{') {
    	                buf.append(c);
        	        } else {
        	            state = 1;
        	        }
        	        break;
    	        case 1:
    	            int idx = c - '0';
    	            if(params.length > idx) {
    	                buf.append(params[idx]);
    	            } else {
    	                buf.append('{').append(c).append('}');
    	            }
    	            state = 2;
    	            break;
    	        case 2:
    	            state = 0;
    	        }
    	    }
	    } else {
	        buf.append("null");
	    }
	    buf.append('_').append(version);
	    String key = buf.toString();
	    return key.indexOf(' ') == -1 ? key : key.replace(" ", "@+~");
	}
	
}
