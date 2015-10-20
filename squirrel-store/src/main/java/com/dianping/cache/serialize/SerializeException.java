package com.dianping.cache.serialize;

public class SerializeException extends Exception {

    public SerializeException(String message, Exception exception) {
        super(message, exception);
    }

    public SerializeException(String message) {
        super(message);
    }
    
}
