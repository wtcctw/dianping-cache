package com.dianping.squirrel.client.core;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CacheFuture<T> implements Future<T> {

	private T result = null;
	private boolean done = false;
	private long start;
	private String key = null;
	private Throwable e = null;

	public CacheFuture(String key) {
		this.key = key;
		start = System.currentTimeMillis();
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return this.done;
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		try {
			return get(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw new ExecutionException("timeout", e);
		}
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		synchronized (this) {
			while (!this.done) {
				long timeoutMillis_ = timeout - (System.currentTimeMillis() - start);
				if (timeoutMillis_ <= 0) {
					StringBuilder sb = new StringBuilder();
					sb.append("timeout with key:").append(key);
					TimeoutException e = new TimeoutException(sb.toString());
					throw e;
				} else {
					this.wait(timeoutMillis_);
				}
			}
			if (this.e != null) {
				if (this.e instanceof TimeoutException) {
					throw (TimeoutException) this.e;
				} else if (this.e instanceof InterruptedException) {
					throw (InterruptedException) this.e;
				} else {
					throw new ExecutionException(this.e);
				}
			}
			return this.result;
		}
	}

	public void onSuccess(T result) {
		this.result = result;
		synchronized (this) {
			this.done = true;
			this.notifyAll();
		}
	}

	public void onFailure(Throwable e) {
		this.e = e;
		synchronized (this) {
			this.done = true;
			this.notifyAll();
		}
	}
}
