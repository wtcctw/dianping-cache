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

import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.entity.SupportedSpecification.SupportedCacheClients;
import com.dianping.remote.cache.dto.*;
//import com.dianping.remote.cache.dto.XMemcachedConfigDetailDTO;
//import com.dianping.remote.cache.dto.DCacheConfigDetailDTO;
import com.dianping.squirrel.client.util.DTOUtils;
import com.dianping.squirrel.common.domain.CacheConfigDetailDTO;
import com.dianping.squirrel.common.domain.CacheConfigurationDTO;
import com.dianping.squirrel.common.domain.DangaConfigDetailDTO;
import com.dianping.squirrel.common.domain.DcacheConfigDetailDTO;
import com.dianping.squirrel.common.domain.EhcacheConfigDetailDTO;
import com.dianping.squirrel.common.domain.MemcachedConfigDetailDTO;
 
/**
 * CacheConfiguration2DTO Translator
 * @author danson.liu
 *
 */
public class CacheConfiguration2DTOTranslator implements Translator<CacheConfiguration, CacheConfigurationDTO> {

	@Override
	public CacheConfigurationDTO translate(CacheConfiguration source) {
		assert source != null;
		if(source != null){
			CacheConfigurationDTO configuration = new CacheConfigurationDTO();
			DTOUtils.copyProperties(configuration, source);
			configuration.setKey(source.getCacheKey());
			configuration.setDetail(translate2detail(source));
			return configuration;
		}
		return null;
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
		} else if (SupportedCacheClients.DCACHE_CLIENT_CLAZZ.equals(clientClazz)) {
			return translateDcacheConfigDetail(source);
		} else if (SupportedCacheClients.DANGA_CLIENT_CLAZZ.equals(clientClazz)) {
			return translateDangaConfigDetail(source);
		} else if (SupportedCacheClients.REDIS_CLIENT_CLAZZ.equals(clientClazz)) {
			return null;
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
		detail.setClientClazz(source.getClientClazz());
		detail.setTranscoderClazz(source.getTranscoderClazz());
		return detail;
	}

	private CacheConfigDetailDTO translateDangaConfigDetail(CacheConfiguration source){
		DangaConfigDetailDTO detail = new DangaConfigDetailDTO();
		detail.setClientClazz(source.getClientClazz());
		detail.setServerList(source.getServerList());
		detail.setTranscoderClazz(source.getTranscoderClazz());
		return detail;
	}

	private CacheConfigDetailDTO translateDcacheConfigDetail(CacheConfiguration source){
		DcacheConfigDetailDTO detail = new DcacheConfigDetailDTO();
		detail.setClientClazz(source.getClientClazz());
		detail.setTranscoderClazz(source.getTranscoderClazz());
		return detail;
	}


}
