package com.dianping.squirrel.common.compress;

import com.dianping.squirrel.common.exception.StoreException;


public class StoreCompressException extends StoreException {
    
    public StoreCompressException(String message) {
        super(message);
    }

    public StoreCompressException(String message, Exception exception) {
        super(message, exception);
    }

}
