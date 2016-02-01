package com.dianping.cache.scale.instance.validate;

import com.dianping.cache.scale.instance.Result;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/1/27.
 */
public class Validator implements Validate {

    private Result result;

    public Validator(Result result){
        this.result = result;
    }

    @Override
    public void validate() {
        if(result.getInstances() == null  || result.getInstances().size() < 1){
            throw new IllegalArgumentException("Result is null");
        }
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }
}
