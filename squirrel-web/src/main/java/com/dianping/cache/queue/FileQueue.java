package com.dianping.cache.queue;

import java.util.concurrent.TimeUnit;

/**
 * 文件队列
 * 
 * @author Leo Liang
 */
public interface FileQueue<T> {

	T get();

	T get(long timeout, TimeUnit timeUnit);

	void add(T m) throws FileQueueClosedException;

	void close();
}
