package com.dianping.cache.scale;

public interface Scaler<T extends ScalePlan> {

    public void scaleUp() throws ScaleException;
    
    public void execute(T scalePlan) throws ScaleException;
    
    public void scaleDown() throws ScaleException;
}
