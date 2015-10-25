package com.dianping.squirrel.client.impl.memcached;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.SerializationException;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

public class ProtostuffSerializer {

	private static ConcurrentHashMap<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<Class<?>, Schema<?>>();
	private static Objenesis objenesis = new ObjenesisStd(true);

	public ProtostuffSerializer() {
	}

	private static <T> Schema<T> getSchema(Class<T> cls) {
		Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
		if (schema == null) {
			schema = RuntimeSchema.createFrom(cls);
			if (schema != null) {
				cachedSchema.putIfAbsent(cls, schema);
			}
		}
		return schema;
	}

	public Object deserialize(InputStream is, Class<?> type) throws SerializationException {
		try {
			Object message = objenesis.newInstance(type);
			Schema schema = getSchema(type);
			ProtostuffIOUtil.mergeFrom(is, message, schema);
			return message;
		} catch (Throwable e) {
			throw new SerializationException(e.getMessage(), e);
		}
	}

	public void serialize(OutputStream os, Object obj) throws SerializationException {
		LinkedBuffer buffer = LinkedBuffer.allocate(1024);
		try {
			Schema schema = getSchema(obj.getClass());
			os.write(ProtostuffIOUtil.toByteArray(obj, schema, buffer));
		} catch (Throwable e) {
			throw new SerializationException(e.getMessage(), e);
		} finally {
			buffer.clear();
		}
	}

}
