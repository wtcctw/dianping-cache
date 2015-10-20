/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.cache.exception;


public class CacheException extends Exception {

	private static final long serialVersionUID = -4052834884778586750L;

	protected String errorCode;

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public CacheException() {
		super();
	}

	public CacheException(String msg) {
		super(msg);
	}

	public CacheException(Throwable cause) {
		super(cause);
	}

	public CacheException(String msg, String errorCode, Throwable cause) {
		super(msg, cause);
		this.errorCode = errorCode;
	}

	public CacheException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
