package com.dianping.squirrel.exception;

import java.util.concurrent.TimeoutException;

public class StoreTimeoutException extends StoreException {

	public StoreTimeoutException(TimeoutException e) {
		super(e);
	}

	public StoreTimeoutException(String message, TimeoutException e) {
		super(message, e);
	}

}
