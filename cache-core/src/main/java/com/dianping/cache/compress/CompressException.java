package com.dianping.cache.compress;


public class CompressException extends Exception {
    
    public CompressException(String message) {
        super(message);
    }

    public CompressException(String message, Exception exception) {
        super(message, exception);
    }

}
