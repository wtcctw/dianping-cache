package com.dianping.squirrel.exception;


public class StoreException extends RuntimeException {

	public StoreException(Throwable cause) {
		super(cause);
	}

	public StoreException(String message, Throwable cause) {
		super(message, cause);
	}

	public StoreException(String message) {
		super(message);
	}

}
