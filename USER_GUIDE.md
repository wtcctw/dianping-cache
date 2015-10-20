### Cache用户文档 
------

### 概述
avatar-cache是点评缓存平台整体解决方案，主要包括缓存管理节点（cache-server）、缓存客户端（avatar-cache）、缓存存储节点（memcached、dcache等）组成：
 

### 缓存定义
a、key组成
缓存包括key和value两部分，key的组成包括3部分：category、template、version，最终存储在缓存里的key字符串是由category.template_version组合而成：
1)、category是某一类缓存的名称，它不是缓存key，可以理解它是缓存key的前缀
2)、template是缓存模板，由参数组成，如c{0}d{1}这个模板，{0}代表第1个参数比如userId，{1}代表第2个参数比如cityId，如果最终把2个参数值填充进去，最后的字符串类似：c1000d2
3)、version是avatar-cache内部管理的一个数字，如果这个category清一次缓存，那么version会自动加1，这样最后的key字符串会变成全新的字符串，应用使用新的key之后需要重新从数据源加载数据到缓存
比如申请一个category：test，template为c{0}d{1}，最后的key字符串可能是：test.c1000d2_0

b、热点key
热点key代表这个key访问频率高，avatar-cache内部针对热点key会特殊处理，在这个key和value第一次加载到缓存时，avatar-cache会备份一份相同key/value的数据在缓存里，
但失效时间会比原始数据稍长，当原始数据接近失效时，会使用备份的数据，只会有一个请求返回空导致去数据库取新的数据内容再放入缓存，这样可以减少对数据库访问的压力

### 缓存管理平台

缓存的管理包括以下几个方面：

a、缓存申请和变更
为了统一管理缓存，需要申请缓存，申请缓存需要指定以下：
1)、缓存key定义，包括缓存名（category）、缓存模板、失效时间、是否热点key
2)、缓存所存储的集群，包括memcached集群、web（web cache是本地ehcache）、dcache集群，比如memcached在线上有多套集群，需要根据自己业务申请相应的集群
3)、缓存访问大概qps、数据平均大小、数量等

申请缓存需要通过DBA缓存管理平台进行申请：
http://tools.dba.dp/cache_admin/audit_index.php
缓存如果需要变更参数比如修改失效时间，也可以通过该平台进行变更

b、清理缓存
如果要清理缓存，也可以在缓存管理平台进行申请：
http://tools.dba.dp/cache_admin/flush_index.php

c、缓存查询
memcached缓存内容的查询可以通过以下页面进行：
http://tools.dba.dp/cache_admin/info_get_value.php

缓存的申请和变更等操作均由DBA负责，如有问题请联系DBA戴骁雄

###	缓存存储节点

a、memcached

memcached集群由DBA维护，线上一般按业务划分为不同的集群，对于数据量较大或访问较多的业务，一般需要申请独立的memcached集群，如果需要申请独立的memcached集群，请联系DBA戴骁雄
客户端avatar-cache是通过spymemcached实现对memcached的访问

b、web

web cache其实是应用本地缓存，内部是ehcache实现，它的优势是性能高，相比memcached没有网络开销，但除非对性能要求极高的业务，建议还是使用memcached

c、dcache

dcache是由腾讯MIG开发的一套KV系统，相比memcached，它提供了数据持久化的功能
在很多业务场景下，使用dcache并不是用来取代memcached，而是取代mysql，数据的写和读都直接操作dcache
avatar-cache针对dcache接口进行了封装，只需要在申请缓存时指定缓存集群为dcache
目前dcache在beta环境、线上环境有部署，由DBA负责维护，其他环境暂时没有部署
对于数据量较大或访问较多的业务，可以申请独立的集群，如果需要申请独立的dcache集群，请联系DBA戴骁雄

### 客户端使用说明

a、依赖 
  
		<dependency>    
		<groupId>com.dianping</groupId>    
		<artifactId>avatar-cache</artifactId>    
		<version>2.6.7</version>    
		</dependency>
		
请注意下面依赖的ehcache和spymemcached的jar包版本是否正确：
		
		<dependency>    
		<groupId>net.sf.ehcache</groupId>    
		<artifactId>ehcache-core</artifactId>    
		<version>2.5.2</version>    
		</dependency>    
		<dependency>      
		<groupId>net.spy</groupId>      
		<artifactId>spymemcached</artifactId>      
		<version>2.11.6</version>      
		</dependency>
		
如果存储节点使用腾讯dcache，还需要增加以下依赖：
		
		<dependency>
		<groupId>qq-central</groupId>
		<artifactId>dcache_client_api</artifactId>
		<version>1.0.0-SNAPSHOT</version>
		</dependency>
		
		
b、spring配置使用方式

在spring配置文件里增加avatar-cache annotation配置：

		<?xml version="1.0" encoding="UTF-8"?>  
		<beans xmlns="http://www.springframework.org/schema/beans"  
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:aop="http://www.springframework.org/schema/aop"  
		xmlns:tx="http://www.springframework.org/schema/tx" xmlns:context="http://www.springframework.org/schema/context"  
		xmlns:amq="http://activemq.apache.org/schema/core" xmlns:avatar="http://www.dianping.com/schema/avatar"  
		xsi:schemaLocation="  
		http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.5.xsd  
		http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-2.5.xsd  
		http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-2.5.xsd  
		http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-2.5.xsd  
		http://activemq.apache.org/schema/core http://activemq.apache.org/schema/core/activemq-core.xsd  
		http://www.dianping.com/schema/avatar http://www.dianping.com/schema/avatar/avatar-1.0.xsd">  
		  
		<!-- avatar-cache annotation -->
		<avatar:cache />  
  
  		<!-- spring annotation -->
		<context:component-scan base-package="com.dianping" />  
		<context:annotation-config />
  

通过以上配置后，可以直接在spring中拿到name是cacheService、实现了CacheService接口的bean

例如：

		@Component
		public class MemcacheDemoService implements CacheDemoService {
		
			@Autowired
			private CacheService cacheService;
			
			public void ayncSetKeyValue(String key, String value) {
				CacheKey cacheKey = new CacheKey("mymemcache", key);
				try {
					cacheService.asyncSet(cacheKey, value);
				} catch (CacheException e) {
					logger.error("", e);
				}
			}
			
			public String getKeyValue(String key) {
				CacheKey cacheKey = new CacheKey("mymemcache", key);
				return cacheService.get(cacheKey);
			}
		}
		
c、java api使用示例
api使用方式必须2.6.7以上版本才支持，使用方式相比spring方式更简单：
		
		CacheService cacheService = CacheServiceFactory.getCacheService();

例如：

		@Component
		public class MemcacheDemoService implements CacheDemoService {
		
			private CacheService cacheService = CacheServiceFactory.getCacheService();
			
			public void ayncSetKeyValue(String key, String value) {
				CacheKey cacheKey = new CacheKey("mymemcache", key);
				try {
					cacheService.asyncSet(cacheKey, value);
				} catch (CacheException e) {
					logger.error("", e);
				}
			}
			
			public String getKeyValue(String key) {
				CacheKey cacheKey = new CacheKey("mymemcache", key);
				return cacheService.get(cacheKey);
			}
		}


d、使用规范
如果是set，请尽量使用异步asyncSet接口(老版本中的add接口)
如果是同时get多个key，尽量使用mGetWithNonExists接口		

e、CacheService接口

com.dianping.avatar.cache.CacheService接口：

		/**
		 * 获取指定key的value
		 * @param key
		 * @return
		 */
		<T> T get(CacheKey key);
		
		/**
		 * 批量获取指定key集合的结果
		 * @param keys
		 * @return 如果某个key没有查询到，则不会出现在结果里
		 */
		<T> Map<CacheKey, T> mGetWithNonExists(List<CacheKey> keys);
		
		/**
		 * 带超时的get接口，业务需要自己catch TimeouException
		 * @param key
		 * @return
		 * @throws TimeoutException
		 */
		<T> T getOrTimeout(CacheKey key) throws TimeoutException;
		
		/**
		 * 带超时的get接口，业务需要自己catch TimeouException
		 * @param keys
		 * @return
		 * @throws TimeoutException
		 */
		<T> Map<CacheKey, T> mGetOrTimeout(List<CacheKey> keys) throws TimeoutException;
		
		/**
		 * 异步set，如果有相同key存在，也会覆盖相同key的内容
		 * Use asyncSet instead
		 */
		boolean add(CacheKey key, Object value);
	
		/**
		 * 同步set，如果有相同key存在，也会覆盖相同key的内容
		 * @param key
		 * @param value
		 * @return 如果成功返回true
		 * @throws CacheException
		 * @throws TimeoutException
		 */
		boolean set(CacheKey key, Object value) throws CacheException, TimeoutException;
		
		/**
		 * 同步set，如果有相同key存在，也会覆盖相同key的内容
		 * @param key
		 * @param value
		 * @param timeout 超时时间，毫秒
		 * @return 如果成功返回true
		 * @throws CacheException
		 * @throws TimeoutException
		 */
		boolean set(CacheKey key, Object value, long timeout) throws CacheException, TimeoutException;
		
		/**
		 * 同步add，如果存在相同key则不增加，不存在相同key才会增加
		 * @param key
		 * @param value
		 * @return 如果存在相同返回false，不存在相同key增加成功返回true
		 * @throws CacheException
		 * @throws TimeoutException
		 */
		boolean addIfAbsent(CacheKey key, Object value) throws CacheException, TimeoutException;
		
		/**
		 * 同步add，如果存在相同key则不增加，不存在相同key才会增加
		 * @param key
		 * @param value
		 * @param timeout 超时时间，毫秒
		 * @return 如果存在相同返回false，不存在相同key增加成功返回true
		 * @throws CacheException
		 * @throws TimeoutException
		 */
		boolean addIfAbsent(CacheKey key, Object value, long timeout) throws CacheException, TimeoutException;
		
		/**
		 * 异步get接口，future方式
		 * @param key
		 * @return 返回future对象
		 * @throws CacheException
		 */
		<T> Future<T> asyncGet(final CacheKey key) throws CacheException;
		
		/**
		 * 异步get接口，callback方式，memcached暂不支持
		 * @param key
		 * @param callback
		 */
		<T> void asyncGet(final CacheKey key, final CacheCallback<T> callback);
		
		/**
		 * 异步批量get接口，callback方式，memcached暂不支持
		 * @param keys
		 * @param callback
		 */
		<T> void asyncBatchGet(final List<CacheKey> keys, final CacheCallback<Map<CacheKey, T>> callback);
		
		/**
		 * 异步set接口，future方式
		 * @param key
		 * @param value
		 * @return future
		 * @throws CacheException
		 */
		Future<Boolean> asyncSet(final CacheKey key, final Object value) throws CacheException;
		
		/**
		 * 异步set接口，callback方式
		 * @param key
		 * @param value
		 * @param callback
		 */
		void asyncSet(final CacheKey key, final Object value, final CacheCallback<Boolean> callback);
		
		/**
		 * 异步add接口，future方式
		 * @param key
		 * @param value
		 * @return
		 * @throws CacheException
		 */
		Future<Boolean> asyncAddIfAbsent(final CacheKey key, final Object value) throws CacheException;
		
		/**
		 * 异步add接口，callback方式
		 * @param key
		 * @param value
		 * @param callback
		 */
		void asyncAddIfAbsent(final CacheKey key, final Object value, final CacheCallback<Boolean> callback);
		
		/**
		 * 同步delete接口
		 * @param key
		 * @return 删除成功返回true
		 * @throws CacheException
		 * @throws TimeoutException
		 */
		boolean delete(CacheKey key) throws CacheException, TimeoutException;
		
		/**
		 * 异步delete接口，future方式
		 * @param key
		 * @return
		 * @throws CacheException
		 */
		Future<Boolean> asyncDelete(CacheKey key) throws CacheException;
		
		/**
		 * Atomic-increase cached data with specified key by specified amount, and
		 * return the new value.
		 * not supported with dcache
		 * @param key the key
		 * @param amount the amount to increment
		 * @return the new value (-1 if the key doesn't exist)
		 * @throws CacheException
		 * @throws TimeoutException
		 */
		public long increment(CacheKey key, int amount) throws CacheException, TimeoutException;

		/**
		 * Atomic-increase cached data with specified key by specified amount, and
		 * return the new value.
		 * not supported with dcache
		 * @param key the key
		 * @param amount the amount to increment
		 * @param defaultValue the default value (if the counter does not exist)
		 * @return the new value, or -1 if we were unable to increment or add
		 * @throws CacheException
		 * @throws TimeoutException
		 */
		public long increment(CacheKey key, int amount, long defaultValue) throws CacheException, TimeoutException;
		
		/**
		 * Atomic-decrement the cache data with amount.
		 * not supported with dcache
		 * @param key the key
		 * @param amount the amount to decrement
		 * @return the new value (-1 if the key doesn't exist)
		 * @throws CacheException
		 * @throws TimeoutException
		 */
		public long decrement(CacheKey key, int amount) throws CacheException, TimeoutException;

		/**
		 * Atomic-decrement the cache data with amount.
		 * not supported with dcache
		 * @param key the key
		 * @param amount the amount to decrement
		 * @param defaultValue the default value (if the counter does not exist)
		 * @return the new value, or -1 if we were unable to increment or add
		 * @throws CacheException
		 * @throws TimeoutException
		 */
		public long decrement(CacheKey key, int amount, long defaultValue) throws CacheException, TimeoutException;
		
		<T> CASValue<T> gets(CacheKey key) throws CacheException, TimeoutException;

		CASResponse cas(CacheKey key, long casId, Object value) throws CacheException, TimeoutException;
		

f、dcache特定接口

暂不支持

### 自定义配置

· 设置连接数（2.6.x及以上版本支持）
在项目lion里加一个key，比如xxx项目（lion项目名必须是点评统一标准项目名称，标准项目名称来自cmdb，是自动设置在classes/META-INF/app.properties里app.name=xxx）
xxx.avatar-cache.spymemcached.poolsize.read，比如设置读的连接是6个，线上默认是4个
xxx.avatar-cache.spymemcached.poolsize.write，比如设置写的连接是1个，线上默认是1个
· 设置自定义超时时间
类似上述配置方式，在项目里设置以下key：
xxx.avatar-cache.memcached.get.timeoutlist，get的超时时间，默认50
xxx.avatar-cache.memcached.mget.timeout，mget的超时时间，默认80
xxx.avatar-cache.memcached.add.timeout，set或add的超时时间，默认50
如果需要修改dcache的get超时时间，可以设置：
xxx.avatar-cache.dcache.timeout.read，默认1000
· 默认不会记录web cache的统计信息到cat，如果需要统计，可以设置：
xxx.avatar-cache.log.web.enable，配置为true，默认false
. 关闭avatar-cache（2.7.3及以上版本支持）
一般用于测试目的，xxx.avatar-cache.enable，配置为false，默认为true