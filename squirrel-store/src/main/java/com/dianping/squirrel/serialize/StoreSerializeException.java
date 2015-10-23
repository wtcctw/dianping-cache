package com.dianping.squirrel.serialize;

import com.dianping.squirrel.exception.StoreException;

public class StoreSerializeException extends StoreException {

    public StoreSerializeException(String message, Exception exception) {
        super(message, exception);
    }

    public StoreSerializeException(String message) {
        super(message);
    }
    
}
