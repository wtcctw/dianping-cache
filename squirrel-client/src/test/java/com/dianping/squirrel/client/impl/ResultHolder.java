package com.dianping.squirrel.client.impl;

public class ResultHolder<T> {
    
    private T result;

    private Throwable exception;
    
    public T get() throws Throwable {
        if(exception != null) {
            throw exception;
        }
        return result;
    }
    
    public T getResult() {
        return result;
    }
    
    public void setResult(T result) {
        this.result = result;
    }
    
    public Throwable getException() {
        return exception;
    }
    
    public void setException(Throwable exception) {
        this.exception = exception;
    }
    
}