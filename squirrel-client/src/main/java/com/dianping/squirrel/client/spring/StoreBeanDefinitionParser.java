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
import org.springframework.beans.MutablePropertyValues;
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

import com.dianping.squirrel.client.impl.DefaultStoreClient;

/**
 * The meta-data parser for avatar:cache
 * 
 * @author guoqing.chen
 * @author danson.liu
 * @author pengshan.zhang
 * @author youngphy.yang
 */
public class StoreBeanDefinitionParser implements BeanDefinitionParser {

	private static final String CACHE_CONFIGURATION_WEB_SERVICE_ID = "configurationWebService";

	private static final String ONEWAY_CACHE_MANAGE_WEB_SERVICE_ID = "oneWayManageWebService";

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
	private static final String CACHE_SERVICE_ID_ATTR = "id";
	/**
	 * Default cache service id
	 */
	private static final String DEFAULT_CACHE_SERVICE_ID = "innerStoreClient";

	/**
	 * Default cache service proxy id
	 */
	private static final String DEFAULT_CACHE_SERVICE_PROXY_ID = "storeClient";

	/**
	 * Cache factory bean name
	 */
	private static final String CACHE_CLIENT_FACTORY_ID_ATTR = "factory";
	/**
	 * Default cache factory id
	 */
	private static final String DEFAULT_CACHE_CLIENT_FACTORY_ID = "cacheClientFactory";
	/**
	 * Cache interceptor id attribute
	 */
	private static final String CACHE_INTERCEPTOR_ID_ATTR = "cacheInterceptor";
	/**
	 * Default cache interceptor id
	 */
	private static final String DEFAULT_CACHE_INTERCEPTOR_ID = "cacheInterceptor";
	/**
	 * Cache pointcut id attribute
	 */
	private static final String CACHE_POINTCUT_ID_ATTR = "cachePointcut";
	/**
	 * Default cache pointcut id
	 */
	private static final String DEFAULT_CACHE_POINTCUT_ID = "cachePointcut";
	/**
	 * Cache interceptor id attribute
	 */
	private static final String ADVISOR_ID_ATTR = "cacheAdvisor";
	/**
	 * Default cache advisor id
	 */
	private static final String DEFAULT_ADVISOR_ID = "cacheAdvisor";

	/**
	 * default cache item config manager id
	 */
	private static final String DEFAULT_ITEM_CONFIG_MANAGER_ID = "cacheItemConfigManager";

	private static final String CACHE_ITEM_MANAGER_ID_ATTR = "itemConfigManager";

	/**
	 * CacheService id
	 */
	private String cacheServiceId = DEFAULT_CACHE_SERVICE_ID;

	/**
	 * CacheService proxy id
	 */
	private String cacheServiceProxyId = DEFAULT_CACHE_SERVICE_PROXY_ID;

	/**
	 * Cache interceptor id
	 */
	private String cacheInterceptorId = DEFAULT_CACHE_INTERCEPTOR_ID;
	/**
	 * Cache pointcut id
	 */
	private String cachePointcutId = DEFAULT_CACHE_POINTCUT_ID;

	/**
	 * Cache item config manager
	 */
	private String cacheItemConfigManager = DEFAULT_ITEM_CONFIG_MANAGER_ID;

	private String cacheClientFactory = DEFAULT_CACHE_CLIENT_FACTORY_ID;

	// private GenericBeanDefinition cacheDefinition = new
	// GenericBeanDefinition();

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {

		// Init cache service
		GenericBeanDefinition cacheDefinition = initCacheServiceDefinition(element,
				getBeanDefinitionRegistry(parserContext));

		// Register the statistics cache interceptor proxy bean
		registerCacheProxyBean(element, getBeanDefinitionRegistry(parserContext), cacheDefinition);

		// Register cache interceptor
		registerCacheInterceptorDefinition(element, parserContext);

		// Register cache pointcut
		registerCachePointcutDefinition(element, parserContext);

		// register advisor
		registerAdvisorDefinition(element, parserContext);

		return null;
	}

	/**
	 * Register {@link CacheService} definition. DefaultCacheServiceProxy
	 * delegates the DefaultCacheService on behalf of the cache hit-rate
	 * statistics.
	 */
	protected GenericBeanDefinition initCacheServiceDefinition(Element element,
			BeanDefinitionRegistry beanDefinitionRegistry) {
		GenericBeanDefinition cacheDefinition = new GenericBeanDefinition();
		cacheDefinition.setBeanClass(DefaultStoreClient.class);
		cacheDefinition.setAutowireCandidate(false);

		cacheServiceId = element.getAttribute(CACHE_SERVICE_ID_ATTR);

		if (!StringUtils.hasText(cacheServiceId)) {
			cacheServiceId = DEFAULT_CACHE_SERVICE_ID;
		}

		cacheClientFactory = element.getAttribute(CACHE_CLIENT_FACTORY_ID_ATTR);
		cacheItemConfigManager = element.getAttribute(CACHE_ITEM_MANAGER_ID_ATTR);
		if (!StringUtils.hasText(cacheClientFactory) || !StringUtils.hasText(cacheItemConfigManager)) {
			registerCacheRelatedWebService(beanDefinitionRegistry);
		}

		return cacheDefinition;
	}

	protected void registerCacheProxyBean(Element element, BeanDefinitionRegistry beanDefinitionRegistry,
			GenericBeanDefinition cacheDefinition) {
		GenericBeanDefinition definition = new GenericBeanDefinition();
		definition.setBeanClass(org.springframework.aop.framework.ProxyFactoryBean.class);
		String cacheProxyId = cacheServiceProxyId;
		// definition.getPropertyValues().addPropertyValue("interceptorNames", new String[] { "monitorInterceptor" });
		definition.getPropertyValues().addPropertyValue("target", cacheDefinition);
		BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, cacheProxyId);
		BeanDefinitionReaderUtils.registerBeanDefinition(holder, beanDefinitionRegistry);
	}


	/**
	 * @param parserContext
	 */
	private void registerCacheRelatedWebService(BeanDefinitionRegistry beanDefinitionRegistry) {
		registerCacheWebService(beanDefinitionRegistry, CACHE_CONFIGURATION_WEB_SERVICE_ID,
				"http://service.dianping.com/cacheService/cacheConfigService_1.0.0",
				"com.dianping.remote.cache.CacheConfigurationWebService", false);
		// registerCacheWebService(beanDefinitionRegistry,
		// CACHE_MANAGE_WEB_SERVICE_ID,
		// "http://service.dianping.com/cacheService/cacheManageService_1.0.0",
		// "com.dianping.remote.cache.CacheManageWebService", false);
		registerCacheWebService(beanDefinitionRegistry, ONEWAY_CACHE_MANAGE_WEB_SERVICE_ID,
				"http://service.dianping.com/cacheService/cacheManageService_1.0.0",
				"com.dianping.remote.cache.CacheManageWebService", true);
	}

	/**
	 * @param parserContext
	 * @param serviceName
	 *            TODO
	 * @param serviceInterface
	 *            TODO
	 */
	private void registerCacheWebService(BeanDefinitionRegistry beanDefinitionRegistry, String beanName,
			String serviceName, String serviceInterface, boolean isOneWay) {
		GenericBeanDefinition definition = new GenericBeanDefinition();
		definition.setBeanClassName("com.dianping.dpsf.spring.ProxyBeanFactory");
		definition.setLazyInit(true);
		definition.setInitMethodName("init");
		MutablePropertyValues propertyValues = definition.getPropertyValues();
		propertyValues.addPropertyValue("serviceName", serviceName);
		propertyValues.addPropertyValue("iface", serviceInterface);
		propertyValues.addPropertyValue("serialize", "hessian");
		propertyValues.addPropertyValue("callMethod", isOneWay ? "oneway" : "sync");
		propertyValues.addPropertyValue("timeout", "10000");
		BeanDefinitionReaderUtils.registerBeanDefinition(new BeanDefinitionHolder(definition, beanName),
				beanDefinitionRegistry);
	}

	/**
	 * Register {@link CacheInterceptor} definition
	 */
	private void registerCacheInterceptorDefinition(Element element, ParserContext parserContext) {
		GenericBeanDefinition definition = new GenericBeanDefinition();
		definition.setBeanClass(CacheInterceptor.class);

		// Add reference to CacheService
		definition.getPropertyValues().addPropertyValue("cacheService", new RuntimeBeanReference(cacheServiceProxyId));

		cacheInterceptorId = element.getAttribute(CACHE_INTERCEPTOR_ID_ATTR);

		if (!StringUtils.hasText(cacheInterceptorId)) {
			cacheInterceptorId = DEFAULT_CACHE_INTERCEPTOR_ID;
		}

		BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, this.cacheInterceptorId);

		BeanDefinitionReaderUtils.registerBeanDefinition(holder, parserContext.getRegistry());
	}

	/**
	 * Create cache pointcut definition
	 */
	private void registerCachePointcutDefinition(Element element, ParserContext parserContext) {

		GenericBeanDefinition definition = new GenericBeanDefinition();
		definition.setBeanClass(AnnotationMatchingPointcut.class);

		definition.getConstructorArgumentValues().addGenericArgumentValue(new ValueHolder(null, "java.lang.Class"));

		definition.getConstructorArgumentValues().addGenericArgumentValue(
				new ValueHolder("com.dianping.squirrel.client.annotation.Cache", "java.lang.Class"));

		cachePointcutId = element.getAttribute(CACHE_POINTCUT_ID_ATTR);

		if (!StringUtils.hasText(cachePointcutId)) {
			cachePointcutId = DEFAULT_CACHE_POINTCUT_ID;
		}

		BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, this.cachePointcutId);

		BeanDefinitionReaderUtils.registerBeanDefinition(holder, parserContext.getRegistry());
	}

	/**
	 * Register {@link DefaultBeanFactoryPointcutAdvisor} definition
	 */
	private void registerAdvisorDefinition(Element element, ParserContext parserContext) {

		AopNamespaceUtils.registerAspectJAutoProxyCreatorIfNecessary(parserContext, element);

		GenericBeanDefinition definition = new GenericBeanDefinition();
		definition.setBeanClass(DefaultBeanFactoryPointcutAdvisor.class);

		definition.getPropertyValues().addPropertyValue(ADVICE_BEAN_NAME,
				new RuntimeBeanNameReference(cacheInterceptorId));

		definition.getPropertyValues().addPropertyValue(POINTCUT, new RuntimeBeanReference(cachePointcutId));

		String id = element.getAttribute(ADVISOR_ID_ATTR);

		if (!StringUtils.hasText(id)) {
			id = DEFAULT_ADVISOR_ID;
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
