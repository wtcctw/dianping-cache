package com.dianping.squirrel.common.compress;

public class CompressException extends Exception {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 2407719416074685845L;

	public CompressException(String message) {
        super(message);
    }

    public CompressException(String message, Exception exception) {
        super(message, exception);
    }

}
