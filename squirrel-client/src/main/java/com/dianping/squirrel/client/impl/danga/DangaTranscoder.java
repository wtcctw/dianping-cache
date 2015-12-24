package com.dianping.squirrel.client.impl.danga;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import com.danga.MemCached.ContextObjectInputStream;
import com.dianping.squirrel.client.monitor.SizeMonitor;
import com.dianping.squirrel.client.monitor.TimeMonitor;
import com.dianping.squirrel.common.config.ConfigManagerLoader;
import com.dianping.squirrel.common.exception.StoreTranscodeException;
import com.dianping.squirrel.common.serialize.SerializeException;
import com.dianping.squirrel.common.serialize.Serializer;
import com.dianping.squirrel.common.serialize.SerializerFactory;
import com.schooner.MemCached.AbstractTransCoder;

public class DangaTranscoder extends AbstractTransCoder {
	static final int COMPRESSED = 2;

	// flag for specify hessian serialized type
	static final int HESSERIALIZED = 4;

	static final int SPECIAL_STRING = 8;

	static final int SPECIAL_INT = 10 << 8;

	static final int SPECIAL_LONG = 11 << 8;

	private int compressionThreshold = 16384;

	private Serializer hessianSerializer = SerializerFactory
			.getSerializer("hessian");

	private String EVENT_NAME_REQUEST_SIZE = "Squirrel.memcached.danga.writeSize";

	private String EVENT_NAME_RESPONSE_SIZE = "Squirrel.memcached.danga.readSize";

	private static final String EVENT_NAME_ENCODE_TIME = "encode";

	private static final String EVENT_NAME_DECODE_TIME = "decode";

	private String CACHE_TYPE = "memcached.danga";
	
	private static final long timeMin = ConfigManagerLoader.getConfigManager().getIntValue(
			"avatar-cache.memcached.monitor.transcoder.time.min", 50) * 1000000;

	private void setCacheType(String cacheType) {
		CACHE_TYPE = cacheType;
		if (CACHE_TYPE == null)
			CACHE_TYPE = "memcached.danga";
		EVENT_NAME_RESPONSE_SIZE = "Squirrel." + cacheType + ".readSize";
		EVENT_NAME_REQUEST_SIZE = "Squirrel." + cacheType + ".writeSize";
	}

	public Object decode(final InputStream input) throws IOException {
		long start = System.nanoTime();
		ByteArrayOutputStream swapStream = new ByteArrayOutputStream();  
        byte[] buff = new byte[100];  
        int rc = 0;  
        while ((rc = input.read(buff, 0, 100)) > 0) {  
            swapStream.write(buff, 0, rc);  
        }  
        
        byte[] data = swapStream.toByteArray(); 
        
        //decode byte[] to Object 
        
        Object rv = null;
        try {
            rv = hessianSerializer.fromBytes(data);
        } catch (SerializeException e) {
            throw new StoreTranscodeException(e);
        }
        
        long end = System.nanoTime();
		
		SizeMonitor.getInstance().logRequestSize(EVENT_NAME_REQUEST_SIZE, data.length);
		TimeMonitor.getInstance().logTime(CACHE_TYPE, null, EVENT_NAME_ENCODE_TIME, end - start, timeMin);
		swapStream.close();
		input.close();
		return rv;
	}

	public void encode(final OutputStream output, final Object object)
			throws IOException {
		// ObjectOutputStream oos = new ObjectOutputStream(output);

		long start = System.nanoTime();
		byte[] b = null;
		try {
			b = hessianSerializer.toBytes(object);
		} catch (SerializeException e) {
			throw new StoreTranscodeException(e);
		}

		if (b != null) {
			// TODO length > compressionThreshold --> compress
		}
		long end = System.nanoTime();
		/**
		 * Cat cache size. After compressed.
		 */
		SizeMonitor.getInstance().logRequestSize(EVENT_NAME_REQUEST_SIZE,
				b.length);
		TimeMonitor.getInstance().logTime(CACHE_TYPE, null,
				EVENT_NAME_ENCODE_TIME, end - start, timeMin);
		// oos.writeObject(object);
		// oos.close();
		output.write(b);
		output.close();
	}

	/**
	 * decode the object from the inputstream with your classloader
	 * 
	 * @param input
	 *            inputstream.
	 * @param classLoader
	 *            speicified classloader created by you.
	 * @return decoded java object.
	 * @throws IOException
	 *             error happened in decoding the input stream.
	 */
	public Object decode(InputStream input, ClassLoader classLoader)
			throws IOException {
		Object obj = null;
		ContextObjectInputStream ois = new ContextObjectInputStream(input,
				classLoader);
		try {
			obj = ois.readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException(e.getMessage());
		} finally{
			ois.close();
		}
		return obj;
	}

	protected byte[] encodeString(String in) {
		byte[] rv = null;
		try {
			rv = in.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return rv;
	}
}
