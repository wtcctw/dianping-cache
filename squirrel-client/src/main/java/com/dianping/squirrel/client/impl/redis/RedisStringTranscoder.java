package com.dianping.squirrel.client.impl.redis;

import com.dianping.squirrel.client.core.Transcoder;
import com.dianping.squirrel.client.monitor.SizeMonitor;
import com.dianping.squirrel.common.compress.Compressor.CompressType;
import com.dianping.squirrel.common.exception.StoreTranscodeException;
import com.dianping.squirrel.common.serialize.SerializeException;
import com.dianping.squirrel.common.serialize.Serializer;
import com.dianping.squirrel.common.serialize.Serializer.SerializeType;
import com.dianping.squirrel.common.serialize.SerializerFactory;

/**
 * Redis serialize format:
 * 
 * long: string representation of long value, used in increase/decrease
 * operations other: $@[compress_flag][serialize_flag] + compressed/serialized
 * content
 * 
 * @author enlight
 */
public class RedisStringTranscoder implements Transcoder<String> {

	public static final String KEY_SERIALIZE_TYPE = "squirrel.serialize.type";
	public static final String DEFAULT_SERIALIZE_TYPE = SerializeType.hessian.name();
	public static final String KEY_COMPRESS_ENABLE = "squirrel.compress.enable";
	public static final boolean DEFAULT_COMPRESS_ENABLE = false;
	public static final String KEY_COMPRESS_TYPE = "squirrel.compress.type";
	public static final String DEFAULT_COMPRESS_TYPE = CompressType.gzip.name();
	public static final String KEY_COMPRESS_THRESHOLD = "squirrel.compress.threshold";
	public static final int DEFAULT_COMPRESS_THRESHOLD = 16 * 1024;

	private static final String TRANSCODE_PREFIX = "$@";

	private static final byte SERIALIZE_INT = 1;
	private static final byte SERIALIZE_STRING = 1 << 1;
	private static final byte SERIALIZE_HESSIAN = 1 << 2;

	private static final byte COMPRESS_NONE = 0;
	// private static final byte COMPRESS_GZIP = 1;
	// private static final byte COMPRESS_SNAPPY = 2;

	private String readSizeEvent;
	private String writeSizeEvent;
	private Serializer serializer;

	public RedisStringTranscoder() {
		this("redis");
	}

	public RedisStringTranscoder(String storeType) {
		setStoreType(storeType);
		this.serializer = SerializerFactory.getSerializer(DEFAULT_SERIALIZE_TYPE);
	}

	private void setStoreType(String storeType) {
		if (storeType == null)
			storeType = "redis";
		readSizeEvent = "Squirrel." + storeType + ".readSize";
		writeSizeEvent = "Squirrel." + storeType + ".writeSize";
	}

	@Override
	public <T> String encode(T object) {
		String serialized = null;
		if (object instanceof Long) {
			serialized = object.toString();
		} else if (object instanceof Integer) {
			serialized = TRANSCODE_PREFIX + (char) COMPRESS_NONE + (char) SERIALIZE_INT + object.toString();
		} else if (object instanceof String) {
			serialized = TRANSCODE_PREFIX + (char) COMPRESS_NONE + (char) SERIALIZE_STRING + (String) object;
		} else {
			try {
				String value = serializer.toString(object);
				serialized = TRANSCODE_PREFIX + (char) COMPRESS_NONE + (char) SERIALIZE_HESSIAN + value;
			} catch (SerializeException e) {
				throw new StoreTranscodeException(e);
			}
		}
		SizeMonitor.getInstance().logRequestSize(writeSizeEvent, serialized.length());
		return serialized;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T decode(String data) {
		SizeMonitor.getInstance().logResponseSize(readSizeEvent, data.length());
		if (data.startsWith(TRANSCODE_PREFIX)) {
			byte compressType = (byte) data.charAt(2);
			if (compressType != 0) {
				throw new StoreTranscodeException("RedisStringTranscoder does not support compress");
			}
			byte serializeType = (byte) data.charAt(3);
			String value = data.substring(4);
			switch (serializeType) {
			case SERIALIZE_INT:
				return (T) Integer.valueOf(value);
			case SERIALIZE_STRING:
				return (T) value;
			case SERIALIZE_HESSIAN:
				try {
					return (T) serializer.fromString(value);
				} catch (SerializeException e) {
					throw new StoreTranscodeException(e);
				}
			default:
				throw new StoreTranscodeException(
						"RedisStringTranscoder does not support serialize type: " + serializeType);
			}
		} else {
			return (T) Long.valueOf(data);
		}
	}
}
