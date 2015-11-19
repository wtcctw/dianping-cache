/**
 * Project: avatar
 * 
 * File Created at 2010-8-10
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

import org.springframework.aop.config.AopNamespaceUtils;
import org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.StoreClientFactory;
import com.dianping.squirrel.client.impl.DefaultStoreClient;

/**
 * The meta-data parser for <squirrel:store id="" store-type=""/>
 * 
 * @author enlight.chen
 */
public class StoreBeanDefinitionParser implements BeanDefinitionParser {

	/**
	 * Bean property
	 */
	private static final String ADVICE_BEAN_NAME = "adviceBeanName";
	/**
	 * Bean property
	 */
	private static final String POINTCUT = "pointcut";

	/**
	 * Id attribute name
	 */
	private static final String STORE_CLIENT_ID_ATTR = "id";
	/**
	 * Default cache service id
	 */
	private static final String DEFAULT_STORE_CLIENT_ID = "storeClient";

	private static final String STORE_TYPE_ID_ATTR = "store-type";
	
	/**
	 * Store interceptor id attribute
	 */
	private static final String STORE_INTERCEPTOR_ID_ATTR = "storeInterceptor";
	/**
	 * Default store interceptor id
	 */
	private static final String DEFAULT_STORE_INTERCEPTOR_ID = "storeInterceptor";
	/**
	 * Cache pointcut id attribute
	 */
	private static final String STORE_POINTCUT_ID_ATTR = "storePointcut";
	/**
	 * Default cache pointcut id
	 */
	private static final String DEFAULT_STORE_POINTCUT_ID = "storePointcut";
	/**
	 * Store interceptor id attribute
	 */
	private static final String STORE_ADVISOR_ID_ATTR = "storeAdvisor";
	/**
	 * Default store advisor id
	 */
	private static final String DEFAULT_STORE_ADVISOR_ID = "storeAdvisor";

	/**
	 * StoreClient id
	 */
	private String storeClientId = DEFAULT_STORE_CLIENT_ID;

	/**
	 * Store interceptor id
	 */
	private String storeInterceptorId = DEFAULT_STORE_INTERCEPTOR_ID;
	/**
	 * Store pointcut id
	 */
	private String storePointcutId = DEFAULT_STORE_POINTCUT_ID;

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {

		// Register store client
		registerStoreClientDefinition(element, getBeanDefinitionRegistry(parserContext));

		// Register store interceptor
		registerStoreInterceptorDefinition(element, parserContext);

		// Register store pointcut
		registerStorePointcutDefinition(element, parserContext);

		// register advisor
		registerStoreAdvisorDefinition(element, parserContext);


		return null;
	}

	/**
     * Register {@link StoreClient} definition
     */
    protected void registerStoreClientDefinition(Element element,
            BeanDefinitionRegistry beanDefinitionRegistry) {
        GenericBeanDefinition storeDefinition = new GenericBeanDefinition();
        storeDefinition.setBeanClass(StoreClientFactory.class);
        storeDefinition.setFactoryMethodName("getStoreClient");

        String storeType = element.getAttribute(STORE_TYPE_ID_ATTR);
        if(StringUtils.hasText(storeType)) {
            storeDefinition.getConstructorArgumentValues().addGenericArgumentValue(storeType);
        }
        
        storeClientId = element.getAttribute(STORE_CLIENT_ID_ATTR);
        if (!StringUtils.hasText(storeClientId)) {
            storeClientId = DEFAULT_STORE_CLIENT_ID;
        }

        BeanDefinitionHolder holder = new BeanDefinitionHolder(storeDefinition, storeClientId);
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, beanDefinitionRegistry);
    }
    
	/**
	 * Register {@link StoreClient} definition
	 */
	protected void registerStoreClientDefinition0(Element element,
			BeanDefinitionRegistry beanDefinitionRegistry) {
		GenericBeanDefinition storeDefinition = new GenericBeanDefinition();
		storeDefinition.setBeanClass(DefaultStoreClient.class);
		storeDefinition.setAutowireCandidate(false);

		storeClientId = element.getAttribute(STORE_CLIENT_ID_ATTR);
		if (!StringUtils.hasText(storeClientId)) {
			storeClientId = DEFAULT_STORE_CLIENT_ID;
		}

        BeanDefinitionHolder holder = new BeanDefinitionHolder(storeDefinition, storeClientId);
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, beanDefinitionRegistry);
	}

	/**
	 * Register {@link StoreInterceptor} definition
	 */
	private void registerStoreInterceptorDefinition(Element element, ParserContext parserContext) {
		GenericBeanDefinition definition = new GenericBeanDefinition();
		definition.setBeanClass(StoreInterceptor.class);
		definition.getPropertyValues().addPropertyValue("storeClient", new RuntimeBeanReference(storeClientId));

		storeInterceptorId = element.getAttribute(STORE_INTERCEPTOR_ID_ATTR);
		if (!StringUtils.hasText(storeInterceptorId)) {
			storeInterceptorId = DEFAULT_STORE_INTERCEPTOR_ID;
		}

		BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, this.storeInterceptorId);
		BeanDefinitionReaderUtils.registerBeanDefinition(holder, parserContext.getRegistry());
	}

	/**
	 * Create store pointcut definition
	 */
	private void registerStorePointcutDefinition(Element element, ParserContext parserContext) {
		GenericBeanDefinition definition = new GenericBeanDefinition();
		definition.setBeanClass(AnnotationMatchingPointcut.class);
		definition.getConstructorArgumentValues().addGenericArgumentValue(new ValueHolder(null, "java.lang.Class"));
		definition.getConstructorArgumentValues().addGenericArgumentValue(
				new ValueHolder("com.dianping.squirrel.client.annotation.Store", "java.lang.Class"));

		storePointcutId = element.getAttribute(STORE_POINTCUT_ID_ATTR);
		if (!StringUtils.hasText(storePointcutId)) {
			storePointcutId = DEFAULT_STORE_POINTCUT_ID;
		}

		BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, this.storePointcutId);
		BeanDefinitionReaderUtils.registerBeanDefinition(holder, parserContext.getRegistry());
	}

	/**
	 * Register {@link DefaultBeanFactoryPointcutAdvisor} definition
	 */
	private void registerStoreAdvisorDefinition(Element element, ParserContext parserContext) {
		AopNamespaceUtils.registerAspectJAutoProxyCreatorIfNecessary(parserContext, element);

		GenericBeanDefinition definition = new GenericBeanDefinition();
		definition.setBeanClass(DefaultBeanFactoryPointcutAdvisor.class);
		definition.getPropertyValues().addPropertyValue(ADVICE_BEAN_NAME,
				new RuntimeBeanNameReference(storeInterceptorId));
		definition.getPropertyValues().addPropertyValue(POINTCUT, new RuntimeBeanReference(storePointcutId));

		String id = element.getAttribute(STORE_ADVISOR_ID_ATTR);
		if (!StringUtils.hasText(id)) {
			id = DEFAULT_STORE_ADVISOR_ID;
		}

		BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, "cacheAdvisor");
		BeanDefinitionReaderUtils.registerBeanDefinition(holder, parserContext.getRegistry());
	}

	private BeanDefinitionRegistry getBeanDefinitionRegistry(ParserContext parserContext) {
		BeanDefinitionRegistry beanDefinitionRegistry = null;
		if (parserContext != null) {
			beanDefinitionRegistry = parserContext.getRegistry();
		}
		return beanDefinitionRegistry;
	}

}
