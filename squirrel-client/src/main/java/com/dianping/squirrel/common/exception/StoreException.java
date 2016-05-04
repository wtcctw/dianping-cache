package com.dianping.squirrel.common.exception;


public class StoreException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4667161373558731453L;

	public StoreException(Throwable cause) {
		super(cause);
	}

	public StoreException(String message, Throwable cause) {
		super(message, cause);
	}

	public StoreException(String message) {
		super(message);
	}

    public StoreException() {
        super();
    }

}
