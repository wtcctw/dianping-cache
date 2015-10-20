package com.dianping.cache.scale;

public interface Scaler<T extends ScalePlan> {

    public void scale() throws ScaleException;
    
    public void execute(T scalePlan) throws ScaleException;
    
}
