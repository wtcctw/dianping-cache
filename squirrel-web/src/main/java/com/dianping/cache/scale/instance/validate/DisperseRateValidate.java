package com.dianping.cache.scale.instance.validate;

import com.dianping.cache.scale.InstanceDispersionException;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/1/28.
 */
public class DisperseRateValidate extends AbstractValidate {

    public DisperseRateValidate(Validate validator) {
        super(validator);
    }

    @Override
    public void validate() throws Exception {
        validator.validate();
        if(true){
            throw new InstanceDispersionException("low disperse rate");
        }
    }
}
