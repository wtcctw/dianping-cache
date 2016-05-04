package com.dianping.squirrel.common.serialize;


public class SerializeException extends Exception {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -7265924355794067458L;

	public SerializeException(Throwable t) {
        super(t);
    }
    
    public SerializeException(String message) {
        super(message);
    }

    public SerializeException(String message, Throwable t) {
        super(message, t);
    }
    
}
