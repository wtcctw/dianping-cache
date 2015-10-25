package com.dianping.squirrel.client.config.parser;

import org.apache.commons.lang.ClassUtils;
import org.codehaus.plexus.util.StringUtils;

import com.dianping.remote.cache.dto.CacheConfigurationDTO;
import com.dianping.squirrel.client.core.CacheClientConfiguration;
import com.dianping.squirrel.client.impl.dcache.DCacheClientConfig;

public class DCacheClientConfigurationParser implements CacheClientConfigurationParser {

	String trimValue(String value) {
		if (value.startsWith("\"") && value.endsWith("\"")) {
			return value.substring(1, value.length() - 1);
		}
		return value;
	}

	@Override
	public CacheClientConfiguration parse(CacheConfigurationDTO detail) {
		DCacheClientConfig config = new DCacheClientConfig();
		String servers = detail.getServers();
		if (StringUtils.isBlank(servers)) {
			throw new RuntimeException("Dcache config's servers must not be empty.");
		}
		String locator = null;
		String module = null;
		String proxy = null;
		boolean persistent = true;
		String[] items = servers.split(";");
		try {
			for (String item : items) {
				if (StringUtils.isNotBlank(item) && item.indexOf("=") != -1) {
					String[] itemArray = item.split("=");
					String key = itemArray[0];
					String value = itemArray[1];
					if (key.startsWith("module")) {
						module = trimValue(value);
					} else if (key.startsWith("proxy")) {
						proxy = trimValue(value);
					} else if (key.startsWith("locator")) {
						locator = trimValue(value);
					} else if (key.startsWith("persistent")) {
						persistent = Boolean.valueOf(trimValue(value));
					}
				}
			}
		} catch (RuntimeException ex) {
			throw new RuntimeException("Failed to parse dcached config.", ex);
		}
		if (StringUtils.isBlank(module)) {
			throw new RuntimeException("Dcache config's module must not be empty.");
		}
		if (StringUtils.isBlank(proxy)) {
			throw new RuntimeException("Dcache config's proxy must not be empty.");
		}
		if (StringUtils.isBlank(locator)) {
			throw new RuntimeException("Dcache config's locator must not be empty.");
		}
		config.setLocator(locator);
		config.setModule(module);
		config.setProxy(proxy);
		config.setPersistent(persistent);
		String transcoderClass = detail.getTranscoderClazz();
		if (transcoderClass != null && !transcoderClass.trim().isEmpty()) {
			try {
				Class<?> cz = ClassUtils.getClass(transcoderClass.trim());
				config.setTranscoderClass(cz);
			} catch (Exception ex) {
				throw new RuntimeException("Failed to set dcached's transcoder.", ex);
			}
		}
		config.setClientClazz(detail.getClientClazz());

		return config;
	}

}
