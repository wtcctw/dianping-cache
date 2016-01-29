package com.dianping.cache.scale.instance.validate;

import com.dianping.cache.scale.instance.Result;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/1/27.
 */
public interface Validate {
    void validate() throws Exception;
    Result getResult();
    void setResult(Result result);
}
