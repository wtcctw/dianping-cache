package com.dianping.squirrel.client.impl.danga;

import java.util.List;

import org.apache.commons.lang.ClassUtils;

import com.dianping.remote.cache.dto.CacheConfigurationDTO;
import com.dianping.squirrel.client.config.StoreClientConfig;
import com.dianping.squirrel.client.config.StoreClientConfigParser;
import com.dianping.squirrel.client.config.zookeeper.CacheMessageManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;

public class DangaClientConfigParser implements StoreClientConfigParser{

	@Override
	public StoreClientConfig parse(CacheConfigurationDTO detail) {
		// TODO Auto-generated method stub
		DangaClientConfig config = new DangaClientConfig();
		config.setCacheConfigurationListener(CacheMessageManager.getInstance());
		String transcoderClass = ConfigManagerLoader.getConfigManager().getStringValue(
		        "squirrel.memcached.transcoder.class", "com.dianping.squirrel.client.impl.danga.DangaTranscoder");
		if (transcoderClass != null && !transcoderClass.trim().isEmpty()) {
			try {
				Class<?> cz = ClassUtils.getClass(transcoderClass.trim());
				config.setTranscoderClass(cz);
			} catch (Exception ex) {
				throw new RuntimeException("Failed to set memcached's transcoder.", ex);
			}
		}
		List<String> serverList = detail.getServerList();
		if (serverList == null || serverList.size() == 0) {
			throw new RuntimeException("Memcached config's server list must not be empty.");
		}
		config.setServerList(serverList);
		config.setPoolName(detail.getCacheKey());
		//config.setClientClazz(detail.getClientClazz());

		return config;
	}

}
