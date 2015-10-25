package com.dianping.squirrel.common.serialize;

import com.dianping.squirrel.common.exception.StoreException;

public class StoreSerializeException extends StoreException {

    public StoreSerializeException(String message, Exception exception) {
        super(message, exception);
    }

    public StoreSerializeException(String message) {
        super(message);
    }
    
}
