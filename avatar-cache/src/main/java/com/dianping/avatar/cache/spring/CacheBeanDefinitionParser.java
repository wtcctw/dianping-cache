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
package com.dianping.avatar.cache.spring;

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

import com.dianping.avatar.cache.CacheService;
import com.dianping.avatar.cache.DefaultCacheService;
import com.dianping.avatar.cache.client.RemoteCacheClientFactory;
import com.dianping.avatar.cache.configuration.RemoteCacheItemConfigManager;
import com.dianping.avatar.cache.interceptor.CacheInterceptor;
import com.dianping.avatar.cache.interceptor.CacheMonitorInterceptor;
import com.dianping.cache.builder.metadata.XMLCacheClientFactory;

/**
 * The meta-data parser for avatar:cache
 * 
 * @author guoqing.chen
 * @author danson.liu
 * @author pengshan.zhang
 * @author youngphy.yang
 */
public class CacheBeanDefinitionParser implements BeanDefinitionParser {

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
	private static final String DEFAULT_CACHE_SERVICE_ID = "innerCacheService";

	/**
	 * Default cache service proxy id
	 */
	private static final String DEFAULT_CACHE_SERVICE_PROXY_ID = "cacheService";

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

	private static final String DEFAULT_CACHE_MESSAGE_LISTENER_ID = "cacheMessageListener";

	private static final String DEFAULT_CACHE_CURATOR_CLIENT_ID = "cacheCuratorClient";

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

	private String cacheMessageListener = DEFAULT_CACHE_MESSAGE_LISTENER_ID;

	private String cacheCuratorClient = DEFAULT_CACHE_CURATOR_CLIENT_ID;

	// private GenericBeanDefinition cacheDefinition = new
	// GenericBeanDefinition();

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {

		// Init cache service
		GenericBeanDefinition cacheDefinition = initCacheServiceDefinition(element,
				getBeanDefinitionRegistry(parserContext));

		// Register the statistics cache interceptor
		// registerStatisticsCacheInterceptor(element, getBeanDefinitionRegistry(parserContext));

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
		cacheDefinition.setBeanClass(DefaultCacheService.class);
		cacheDefinition.setAutowireCandidate(false);

		cacheServiceId = element.getAttribute(CACHE_SERVICE_ID_ATTR);

		if (!StringUtils.hasText(cacheServiceId)) {
			cacheServiceId = DEFAULT_CACHE_SERVICE_ID;
		}

		// Add reference to CacheFactory
//		ConstructorArgumentValues constructorArgumentValues = cacheDefinition.getConstructorArgumentValues();

//		GenericBeanDefinition cacheCuratorClientBean = new GenericBeanDefinition();
//		cacheCuratorClientBean.setBeanClass(CacheCuratorClient.class);
//		BeanDefinitionReaderUtils.registerBeanDefinition(new BeanDefinitionHolder(cacheCuratorClientBean,
//				cacheCuratorClient), beanDefinitionRegistry);

		cacheClientFactory = element.getAttribute(CACHE_CLIENT_FACTORY_ID_ATTR);
		cacheItemConfigManager = element.getAttribute(CACHE_ITEM_MANAGER_ID_ATTR);
		if (!StringUtils.hasText(cacheClientFactory) || !StringUtils.hasText(cacheItemConfigManager)) {
			registerCacheRelatedWebService(beanDefinitionRegistry);
		}

//		if (!StringUtils.hasText(cacheClientFactory)) {
//			cacheClientFactory = DEFAULT_CACHE_CLIENT_FACTORY_ID;
//			// Register default cache client factory
//			registerDefaultCacheClientFactory(beanDefinitionRegistry);
//		}
//		constructorArgumentValues.addGenericArgumentValue(new RuntimeBeanReference(cacheClientFactory));
//		constructorArgumentValues.addGenericArgumentValue(new RuntimeBeanReference(ONEWAY_CACHE_MANAGE_WEB_SERVICE_ID));

//		if (!StringUtils.hasText(cacheItemConfigManager)) {
//			cacheItemConfigManager = DEFAULT_ITEM_CONFIG_MANAGER_ID;
//			// Register default cache item config manager
//			registerDefaultCacheItemConfigManager(beanDefinitionRegistry);
//		}
//		constructorArgumentValues.addGenericArgumentValue(new RuntimeBeanReference(cacheItemConfigManager));

//		GenericBeanDefinition definition = new GenericBeanDefinition();
//		MutablePropertyValues propertyValues = definition.getPropertyValues();
//		definition = new GenericBeanDefinition();
//		definition.setBeanClass(CacheKeyTypeVersionUpdateListener.class);
//		propertyValues = definition.getPropertyValues();
//		propertyValues.addPropertyValue("cacheItemConfigManager", new RuntimeBeanReference(cacheItemConfigManager));
//		BeanDefinitionReaderUtils.registerBeanDefinition(new BeanDefinitionHolder(definition,
//				"keyTypeVersionUpdateListener"), beanDefinitionRegistry);

//		definition = new GenericBeanDefinition();
//		definition.setBeanClass(SingleCacheRemoveListener.class);
//		propertyValues = definition.getPropertyValues();
//		propertyValues.addPropertyValue("cacheClientFactory", new RuntimeBeanReference(cacheClientFactory));
//		BeanDefinitionReaderUtils.registerBeanDefinition(new BeanDefinitionHolder(definition,
//				"singleCacheRemoveListener"), beanDefinitionRegistry);

//		definition = new GenericBeanDefinition();
//		definition.setBeanClass(CacheConfigurationUpdateListener.class);
//		propertyValues = definition.getPropertyValues();
//		propertyValues.addPropertyValue("cacheClientFactory", new RuntimeBeanReference(cacheClientFactory));
//		BeanDefinitionReaderUtils.registerBeanDefinition(new BeanDefinitionHolder(definition,
//				"cacheConfigUpdateListener"), beanDefinitionRegistry);

//		definition = new GenericBeanDefinition();
//		definition.setBeanClass(CacheKeyConfigUpdateListener.class);
//		propertyValues = definition.getPropertyValues();
//		propertyValues.addPropertyValue("cacheItemConfigManager", new RuntimeBeanReference(cacheItemConfigManager));
//		BeanDefinitionReaderUtils.registerBeanDefinition(new BeanDefinitionHolder(definition,
//				"cacheKeyConfigUpdateListener"), beanDefinitionRegistry);

//		definition = new GenericBeanDefinition();
//		definition.setBeanClass(CacheMessageListener.class);
//		propertyValues = definition.getPropertyValues();
//		propertyValues.addPropertyValue("mappingList[0]", new RuntimeBeanReference("keyTypeVersionUpdateListener"));
//		propertyValues.addPropertyValue("mappingList[1]", new RuntimeBeanReference("singleCacheRemoveListener"));
//		propertyValues.addPropertyValue("mappingList[2]", new RuntimeBeanReference("cacheConfigUpdateListener"));
//		propertyValues.addPropertyValue("mappingList[3]", new RuntimeBeanReference("cacheKeyConfigUpdateListener"));
//		BeanDefinitionReaderUtils.registerBeanDefinition(new BeanDefinitionHolder(definition, cacheMessageListener),
//				beanDefinitionRegistry);

//		definition = new GenericBeanDefinition();
//		definition.setBeanClass(CacheMessageManager.class);
//		propertyValues = definition.getPropertyValues();
//		propertyValues.addPropertyValue("cacheItemConfigManager", new RuntimeBeanReference(cacheItemConfigManager));
//		propertyValues.addPropertyValue("cacheClientFactory", new RuntimeBeanReference(cacheClientFactory));
//		BeanDefinitionReaderUtils.registerBeanDefinition(new BeanDefinitionHolder(definition, "cacheMessageManager"),
//				beanDefinitionRegistry);

//		propertyValues = cacheCuratorClientBean.getPropertyValues();
//		propertyValues.addPropertyValue("cacheItemConfigManager", new RuntimeBeanReference(cacheItemConfigManager));
//		propertyValues.addPropertyValue("cacheClientFactory", new RuntimeBeanReference(cacheClientFactory));
//		propertyValues.addPropertyValue("cacheMessageListener", new RuntimeBeanReference(cacheMessageListener));

		return cacheDefinition;
	}

	protected void registerStatisticsCacheInterceptor(Element element, BeanDefinitionRegistry beanDefinitionRegistry) {
		GenericBeanDefinition definition = new GenericBeanDefinition();
		definition.setBeanClass(getStatisticsCacheInterceptor());
		String cacheInterceptorId = "monitorInterceptor";
		// register the cache item config manager
		cacheItemConfigManager = element.getAttribute(CACHE_ITEM_MANAGER_ID_ATTR);
		if (!StringUtils.hasText(cacheItemConfigManager)) {
			cacheItemConfigManager = DEFAULT_ITEM_CONFIG_MANAGER_ID;
		}
		MutablePropertyValues propertyValues = definition.getPropertyValues();
		propertyValues.addPropertyValue(cacheItemConfigManager, new RuntimeBeanReference(cacheItemConfigManager));
		BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, cacheInterceptorId);
		BeanDefinitionReaderUtils.registerBeanDefinition(holder, beanDefinitionRegistry);
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
	 * Register {@link XMLCacheClientFactory} definition
	 */
	private void registerDefaultCacheClientFactory(BeanDefinitionRegistry beanDefinitionRegistry) {
		GenericBeanDefinition definition = new GenericBeanDefinition();
		definition.setBeanClass(RemoteCacheClientFactory.class);
		definition.setLazyInit(true);
		MutablePropertyValues propertyValues = definition.getPropertyValues();
		propertyValues.addPropertyValue("configurationWebService", new RuntimeBeanReference(
				CACHE_CONFIGURATION_WEB_SERVICE_ID));
		propertyValues.addPropertyValue("cacheCuratorClient", new RuntimeBeanReference(cacheCuratorClient));
		BeanDefinitionReaderUtils.registerBeanDefinition(new BeanDefinitionHolder(definition,
				DEFAULT_CACHE_CLIENT_FACTORY_ID), beanDefinitionRegistry);
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
	 * @param parserContext
	 * 
	 */
	private void registerDefaultCacheItemConfigManager(BeanDefinitionRegistry beanDefinitionRegistry) {
		GenericBeanDefinition definition = new GenericBeanDefinition();
		definition.setBeanClass(RemoteCacheItemConfigManager.class);
		// lazy init because default CacheItemConfigManager is not required
		definition.setLazyInit(true);
		MutablePropertyValues propertyValues = definition.getPropertyValues();
		propertyValues.addPropertyValue("configurationWebService", new RuntimeBeanReference(
				CACHE_CONFIGURATION_WEB_SERVICE_ID));
		propertyValues.addPropertyValue("cacheCuratorClient", new RuntimeBeanReference(cacheCuratorClient));
		BeanDefinitionReaderUtils.registerBeanDefinition(new BeanDefinitionHolder(definition,
				DEFAULT_ITEM_CONFIG_MANAGER_ID), beanDefinitionRegistry);
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
				new ValueHolder("com.dianping.avatar.cache.annotation.Cache", "java.lang.Class"));

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

	protected Class<?> getStatisticsCacheInterceptor() {
		return CacheMonitorInterceptor.class;
	}

}
