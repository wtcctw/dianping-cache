package com.dianping.cache.scale;

public interface Scaler<T extends ScalePlan> {

    void scaleUp() throws ScaleException;
    
    void execute(T scalePlan) throws ScaleException;
    
    void scaleDown() throws ScaleException;
}
