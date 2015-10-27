/**
 * Project: avatar
 * 
 * File Created at 2010-7-19
 * $Id$
 * 
 * Copyright 2010 Dianping.com Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Dianping.com.
 */
package com.dianping.squirrel.client.spring;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.beanutils.MethodUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.client.annotation.Store;
import com.dianping.squirrel.client.annotation.StoreOperation;
import com.dianping.squirrel.client.util.StoreAnnotationUtils;

/**
 * StoreInterceptor to support {@link Store} annotation
 * 
 * @author danson.liu
 * @author guoqing.chen
 * 
 */
public class StoreInterceptor implements MethodInterceptor, InitializingBean {
    
    private StoreClient storeClient;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Store store = AnnotationUtils.findAnnotation(method, Store.class);
        if (store == null) {
            Class<? extends Object> targetClazz = invocation.getThis().getClass();
            method = MethodUtils.getAccessibleMethod(targetClazz, method.getName(), method.getParameterTypes());
            store = AnnotationUtils.findAnnotation(method, Store.class);
        }
        if (store != null) {

            StoreOperation operation = store.operation();

            StoreKey storeKey = StoreAnnotationUtils.getStoreKey(method, invocation.getArguments());

            if (operation == StoreOperation.SetAndGet) {
                Object storedItem = storeClient.get(storeKey);

                if (storedItem != null) {
                    return storedItem;
                }

                Object item = invocation.proceed();
                // consider create an null object instead of null
                storeClient.add(storeKey, item);

                return item;
            } else if (operation == StoreOperation.Update || operation == StoreOperation.Remove) {
                storeClient.delete(storeKey);
                return invocation.proceed();
            }
        }
        return invocation.proceed();
    }

    public void setStoreClient(StoreClient storeClient) {
        this.storeClient = storeClient;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(storeClient, "store client is null");
    }
    
}
