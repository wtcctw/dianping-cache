package com.dianping.squirrel.common.exception;


public class StoreTimeoutException extends StoreException {

	public StoreTimeoutException(Throwable e) {
		super(e);
	}

	public StoreTimeoutException(String message, Throwable e) {
		super(message, e);
	}

    public StoreTimeoutException(String message) {
        super(message);
    }

}
