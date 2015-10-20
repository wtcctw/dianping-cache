/**
 * Project: cache-core
 * 
 * File Created at 2010-8-23
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
package com.dianping.cache.memcached;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import net.spy.memcached.CachedData;
import net.spy.memcached.transcoders.BaseSerializingTranscoder;
import net.spy.memcached.transcoders.Transcoder;

import com.dianping.cache.config.ConfigManagerLoader;
import com.dianping.cache.core.CacheValue;
import com.dianping.cache.monitor.SizeMonitor;
import com.dianping.cache.monitor.TimeMonitor;

public class ProtostuffTranscoder extends BaseSerializingTranscoder implements Transcoder<Object> {

	static final int COMPRESSED = 2;

	// flag for specify proto serialized type
	static final int PROTOSERIALIZED = 5;

	static final int SPECIAL_STRING = 8;

	static final int SPECIAL_INT = 10 << 8;

	static final int SPECIAL_LONG = 11 << 8;

	private int compressionThreshold = DEFAULT_COMPRESSION_THRESHOLD;

	private ProtostuffSerializer serializer = new ProtostuffSerializer();

	private String EVENT_NAME_REQUEST_SIZE = "Cache.memcached.writeSize";

	private String EVENT_NAME_RESPONSE_SIZE = "Cache.memcached.readSize";

	private static final String EVENT_NAME_ENCODE_TIME = "encode";

	private static final String EVENT_NAME_DECODE_TIME = "decode";

	private String CACHE_TYPE = "memcached";

	private static final long timeMin = ConfigManagerLoader.getConfigManager().getIntValue(
			"avatar-cache.memcached.monitor.transcoder.time.min", 50) * 1000000;

	public ProtostuffTranscoder() {
		this("memcached", CachedData.MAX_SIZE);
	}

	public ProtostuffTranscoder(String cacheType) {
		this(cacheType, CachedData.MAX_SIZE);
	}

	public void setCacheType(String cacheType) {
		CACHE_TYPE = cacheType;
		EVENT_NAME_RESPONSE_SIZE = "Cache." + cacheType + ".readSize";
		EVENT_NAME_REQUEST_SIZE = "Cache." + cacheType + ".writeSize";
	}

	public ProtostuffTranscoder(String cacheType, int max) {
		super(max);
		setCacheType(cacheType);
	}

	@Override
	public CachedData encode(Object o) {
		Object value = new CacheValue(o, 0);
		long start = System.nanoTime();
		CachedData rv = null;
		Object[] result = fixIncrAndDecrIssue(value);
		int flags = (Integer) result[1];
		byte[] b = null;
		if (result[0] instanceof String) {
			b = encodeString((String) result[0]);
		} else {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			serializer.serialize(baos, result[0]);
			b = baos.toByteArray();
		}
		if (b != null) {
			if (b.length > compressionThreshold) {
				byte[] compressed = compress(b);
				if (compressed.length < b.length) {
					// getLogger().info("Compressed %s from %d to %d",
					// o.getClass().getName(), b.length, compressed.length);
					b = compressed;
					flags |= COMPRESSED;
				} else {
					getLogger().info("Compression increased the size of %s from %d to %d", value.getClass().getName(),
							b.length, compressed.length);
				}
			}
			rv = new CachedData(flags, b, getMaxSize());
		}
		long end = System.nanoTime();
		/**
		 * Cat cache size. After compressed.
		 */
		SizeMonitor.getInstance().logRequestSize(EVENT_NAME_REQUEST_SIZE, b.length);
		TimeMonitor.getInstance().logTime(CACHE_TYPE, null, EVENT_NAME_ENCODE_TIME, end - start, timeMin);

		return rv;
	}

	@Override
	public Object decode(CachedData d) {
		long start = System.nanoTime();
		byte[] data = d.getData();
		Object rv = null;
		if ((d.getFlags() & COMPRESSED) != 0) {
			data = decompress(d.getData());
		}
		if (data == null) {
			return null;
		}
		/**
		 * Cat cache size. After compressed.
		 */
		SizeMonitor.getInstance().logResponseSize(EVENT_NAME_RESPONSE_SIZE, data.length);

		if ((d.getFlags() & PROTOSERIALIZED) != 0) {
			ByteArrayInputStream is = new ByteArrayInputStream(data);
			CacheValue value = (CacheValue) serializer.deserialize(is, CacheValue.class);
			rv = value.getV();
		} else if ((d.getFlags() & SPECIAL_STRING) != 0) {
			rv = decodeString(data);
			if (d.getFlags() == (SPECIAL_INT | SPECIAL_STRING)) {
				rv = Integer.valueOf((String) rv);
			} else if (d.getFlags() == (SPECIAL_LONG | SPECIAL_STRING)) {
				rv = Long.valueOf((String) rv);
			}
		} else {
			throw new IllegalArgumentException("CachedData's flag[" + d.getFlags()
					+ "] not supported by ProtostuffTranscoder.");
		}
		long end = System.nanoTime();
		TimeMonitor.getInstance().logTime(CACHE_TYPE, null, EVENT_NAME_DECODE_TIME, end - start, timeMin);

		return rv;
	}

	/**
	 * @param o
	 * @return [fixed_obj, flag]
	 */
	private Object[] fixIncrAndDecrIssue(Object o) {
		if (o instanceof Integer) {
			return new Object[] { o.toString(), SPECIAL_INT | SPECIAL_STRING };
		}
		if (o instanceof Long) {
			return new Object[] { o.toString(), SPECIAL_LONG | SPECIAL_STRING };
		}
		if (o instanceof String) {
			return new Object[] { o, SPECIAL_STRING };
		}
		return new Object[] { o, PROTOSERIALIZED };
	}

	@Override
	public boolean asyncDecode(CachedData d) {
		if ((d.getFlags() & COMPRESSED) != 0) {
			return true;
		}
		return super.asyncDecode(d);
	}

}
