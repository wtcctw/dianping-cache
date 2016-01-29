package com.dianping.cache.scale.instance.validate;

import com.dianping.cache.scale.instance.Result;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/1/28.
 */
public abstract class AbstractValidate implements Validate {
    protected Validate validator;

    public AbstractValidate(Validate validator) {
        this.validator = validator;
    }

    @Override
    public void validate() throws Exception {
        validator.validate();
    }

    @Override
    public Result getResult() {
        return validator.getResult();
    }

    @Override
    public void setResult(Result result) {
        validator.setResult(result);
    }
}
