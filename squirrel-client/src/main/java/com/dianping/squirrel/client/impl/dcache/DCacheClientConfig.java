/**
 * Project: avatar-cache
 * 
 * File Created at 2010-7-12
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
package com.dianping.squirrel.client.impl.dcache;

import net.spy.memcached.transcoders.Transcoder;

import com.dianping.squirrel.client.core.CacheClientConfiguration;
import com.dianping.squirrel.common.exception.StoreInitializeException;

public class DCacheClientConfig implements CacheClientConfiguration {

	/**
	 * All servers
	 */
	private String locator;

	private String module;

	private String proxy;

	private boolean persistent = true;

	private String clientClazz;

	private Transcoder<Object> transcoder;

	public Transcoder<Object> getTranscoder() {
		return transcoder;
	}

	public boolean isPersistent() {
		return persistent;
	}

	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getProxy() {
		return proxy;
	}

	public void setProxy(String proxy) {
		this.proxy = proxy;
	}

	/**
	 * @param transcoder
	 *            the transcoder to set
	 */
	public void setTranscoder(Transcoder<Object> transcoder) {
		this.transcoder = transcoder;
	}

	public void setTranscoderClass(Class<?> transcoderClass) throws Exception {
		Transcoder<Object> transcoder = (Transcoder<Object>) transcoderClass.newInstance();
		setTranscoder(transcoder);
	}

	public String getLocator() {
		return locator;
	}

	public void setLocator(String locator) {
		this.locator = locator;
	}

	public String getClientClazz() {
		return this.clientClazz;
	}

	public void setClientClazz(String clientClazz) {
		this.clientClazz = clientClazz;
	}

	@Override
	public void init() throws StoreInitializeException {

	}
}
