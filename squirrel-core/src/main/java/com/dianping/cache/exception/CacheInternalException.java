/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.cache.exception;


public class CacheInternalException extends RuntimeException {

	private static final long serialVersionUID = -4052834884778586750L;

	protected String errorCode;

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public CacheInternalException() {
		super();
	}

	public CacheInternalException(String msg) {
		super(msg);
	}

	public CacheInternalException(Throwable cause) {
		super(cause);
	}

	public CacheInternalException(String msg, String errorCode, Throwable cause) {
		super(msg, cause);
		this.errorCode = errorCode;
	}

	public CacheInternalException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
