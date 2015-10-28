package com.dianping.squirrel.common.serialize;


public class SerializeException extends Exception {
    
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
