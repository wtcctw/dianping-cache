/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.squirrel.common.exception;


public class StoreInitializeException extends StoreException {

	private static final long serialVersionUID = -4052834884778586750L;

	protected String errorCode;

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public StoreInitializeException() {
		super();
	}

	public StoreInitializeException(String msg) {
		super(msg);
	}

	public StoreInitializeException(Throwable cause) {
		super(cause);
	}

	public StoreInitializeException(String msg, String errorCode, Throwable cause) {
		super(msg, cause);
		this.errorCode = errorCode;
	}

	public StoreInitializeException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
