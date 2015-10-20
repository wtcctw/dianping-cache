/**
 * Project: cache-server
 * 
 * File Created at 2010-10-19
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
package com.dianping.cache.remote.translator;

import com.dianping.avatar.cache.util.DTOUtils;
import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.entity.SupportedSpecification.SupportedCacheClients;
import com.dianping.remote.cache.dto.CacheConfigDetailDTO;
import com.dianping.remote.cache.dto.CacheConfigurationDTO;
import com.dianping.remote.cache.dto.EhcacheConfigDetailDTO;
import com.dianping.remote.cache.dto.MemcachedConfigDetailDTO;
//import com.dianping.remote.cache.dto.XMemcachedConfigDetailDTO;
//import com.dianping.remote.cache.dto.DCacheConfigDetailDTO;
 
/**
 * CacheConfiguration2DTO Translator
 * @author danson.liu
 *
 */
public class CacheConfiguration2DTOTranslator implements Translator<CacheConfiguration, CacheConfigurationDTO> {

	@Override
	public CacheConfigurationDTO translate(CacheConfiguration source) {
		assert source != null;
		CacheConfigurationDTO configuration = new CacheConfigurationDTO();
        DTOUtils.copyProperties(configuration, source);
        configuration.setKey(source.getCacheKey());
        configuration.setDetail(translate2detail(source));
        return configuration;
	}

	/**
	 * @param source
	 * @return
	 */
	private CacheConfigDetailDTO translate2detail(CacheConfiguration source) {
		String clientClazz = source.getClientClazz();
		if (SupportedCacheClients.MEMCACHED_CLIENT_CLAZZ.equals(clientClazz)) {
			return translateMemcachedConfigDetail(source);
		} else if (SupportedCacheClients.EHCACHE_CLIENT_CLAZZ.equals(clientClazz)) {
			return translateEhcacheConfigDetail(source);
		}
//		} else if(SupportedCacheClients.DCACHE_CLIENT_CLAZZ.equals(clientClazz)){
//			return translateDcacheConfigDetail(source);
//		} else if(SupportedCacheClients.XMEMCACHED_CLIENT_CLAZZ.equals(clientClazz)){
//			return translateXMemcachedConfigDetail(source);
//		}
		throw new UnsupportedOperationException("Configuration detail translation with client class[" 
				+ clientClazz + "] not supported now.");
	}

	/**
	 * @param source
	 * @return
	 */
	private CacheConfigDetailDTO translateEhcacheConfigDetail(CacheConfiguration source) {
		EhcacheConfigDetailDTO detail = new EhcacheConfigDetailDTO();
		return detail;
	}

	/**
	 * @param source
	 * @return
	 */
	private CacheConfigDetailDTO translateMemcachedConfigDetail(CacheConfiguration source) {
		MemcachedConfigDetailDTO detail = new MemcachedConfigDetailDTO();
		detail.setServerList(source.getServerList());
		detail.setTranscoderClazz(source.getTranscoderClazz());
		return detail;
	}
//	
//	private CacheConfigDetailDTO translateXMemcachedConfigDetail(CacheConfiguration source){
//		// TODO
//		XMemcachedConfigDetailDTO detail = new XMemcachedConfigDetailDTO();
//		detail.setServerList(source.getServerList());
//		detail.setTranscoderClazz(source.getTranscoderClazz());
//		return detail;
//		
//	}
//	private CacheConfigDetailDTO translateDcacheConfigDetail(
//			CacheConfiguration source) {
//		// TODO Auto-generated method stub
//		DCacheConfigDetailDTO detail = new DCacheConfigDetailDTO();
//		detail.setServerList(source.getServerList());
//		detail.setTranscoderClazz(source.getTranscoderClazz());
//		return detail;
//	}


}
