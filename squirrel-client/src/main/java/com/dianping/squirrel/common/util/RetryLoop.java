package com.dianping.squirrel.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;

import com.dianping.squirrel.client.impl.memcached.NodeMonitor;

import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.CheckedOperationTimeoutException;
import net.spy.memcached.internal.GetFuture;

public class RetryLoop {

	private static int retryLimit = 0;
	private static Integer[] timeoutMillis;
	private boolean done = false;
	private int retryCount = 0;

	public static class RetryResponse {
		private Object result;
		private int retries;

		public Object getResult() {
			return result;
		}

		public void setResult(Object result) {
			this.result = result;
		}

		public int getRetries() {
			return retries;
		}

		public void setRetries(int retries) {
			this.retries = retries;
		}

	}

	public static void initTimeoutConfig(String timeoutConfig) {
		try {
			String[] timeoutMillisArray = timeoutConfig.split(",");
			List<Integer> timeoutList = new ArrayList<Integer>();
			for (String timeout : timeoutMillisArray) {
				if (StringUtils.isNotBlank(timeout)) {
					timeoutList.add(Integer.valueOf(timeout));
				}
			}
			if (timeoutList.size() > 0) {
				timeoutMillis = timeoutList.toArray(new Integer[0]);
				retryLimit = timeoutMillis.length;
			} else {
				retryLimit = 1;
				timeoutMillis = new Integer[] { 50 };
			}
		} catch (RuntimeException e) {
			retryLimit = 1;
			timeoutMillis = new Integer[] { 50 };
		}
	}

	public RetryLoop() {
	}

	public void takeException(Exception e, Future<?> future) throws Exception {
		if (future != null) {
			future.cancel(true);
		}
		if (e instanceof CheckedOperationTimeoutException) {
		    e = new TimeoutException(e.getMessage());
		    if (++retryCount >= retryLimit)
                throw e;
		} else if (e instanceof IllegalStateException) {
		    e = new TimeoutException(e.getMessage());
		    if (++retryCount >= retryLimit)
		        throw e;
		} else if (e instanceof TimeoutException) {
			if (++retryCount >= retryLimit)
				throw e;
		} else {
			throw e;
		}
	}

	public void markComplete() {
		done = true;
	}

	public boolean shouldContinue() {
		return !done;
	}

	public static RetryResponse hedgedGet(MemcachedClient client, String key) throws Exception {
		Object result = null;
		GetFuture<Object> future = null;
		RetryLoop retryLoop = new RetryLoop();

		RetryResponse resp = new RetryResponse();
		int i = 0;
		while (retryLoop.shouldContinue()) {
			try {
				future = client.asyncGet(key);
				if (future != null) {
				    NodeMonitor.getInstance().logNode(future);
					result = future.get(timeoutMillis[i++], TimeUnit.MILLISECONDS);
					retryLoop.markComplete();
				}
			} catch (Exception e) {
				retryLoop.takeException(e, future);
			}
		}
		resp.setResult(result);
		resp.setRetries(i);
		return resp;
	}

}
