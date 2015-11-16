# Squirrel 用户文档
---
## 概述
Squirrel 是点评的 Key-Value 存储框架，继承自 Avatar-Cache 缓存框架。主要包括管理端（squirrel-console）、客户端（squirrel-client）和存储节点（memcached、redis、dcache等）组成：

TODO：arch-picture

## 主要改变
1. API 接口较 Avatar-Cache 更加合理。
2. 支持基于 redis-cluster 的 KV 存储。
3. 后续版本会增加多备份，自动扩容等功能。

## 存储 Key 的组成

存储包括 key 和 value 两部分，key 的组成包括3部分：category、template、version，最终存储的 key 字符串是由 **${category}.${template}_${version}** 组合而成：
1)、category 是某一类存储的名称，它不是存储 key，它是存储 key 的前缀，可以理解它是数据库里的一张表
2)、template 是存储 key 的模板，由参数组成，如c{0}d{1}这个模板，{0}代表第1个参数比如userId，{1}代表第2个参数比如cityId，如果最终把2个参数值填充进去，最后的字符串类似：c1000d2
3)、version 是 squirrel 内部管理的一个数字，如果这个 category 清一次缓存，那么 version 会自动加1，这样最后的key字符串会变成全新的字符串，应用使用新的key之后需要重新从数据源加载数据到缓存
比如申请一个category：test，template为c{0}d{1}，最后的key字符串可能是：test.c1000d2_0

## 存储管理平台

存储的管理包括以下几个方面：

a、存储申请和变更
为了统一管理存储，需要申请存储，申请存储需要指定以下：
1)、存储类别定义，包括存储类别（category）、key模板、失效时间、是否热点key
2)、存储集群，包括memcached集群、web（web cache是本地ehcache）、dcache集群，比如memcached在线上有多套集群，需要根据自己业务申请相应的集群
3)、存储访问大概qps、数据平均大小、数量等

申请存储需要通过DBA缓存管理平台进行申请：
http://tools.dba.dp/cache_admin/audit_index.php
存储如果需要变更参数比如修改失效时间，也可以通过该平台进行变更

b、清理缓存
如果要清理缓存，也可以在缓存管理平台进行申请：
http://tools.dba.dp/cache_admin/flush_index.php

c、存储查询
memcached缓存内容的查询可以通过以下页面进行：
http://tools.dba.dp/cache_admin/info_get_value.php

存储的申请和变更等操作均由DBA负责，如有问题请联系DBA戴骁雄

##	存储节点

a、memcached

memcached集群由DBA维护，线上一般按业务划分为不同的集群，对于数据量较大或访问较多的业务，一般需要申请独立的memcached集群，如果需要申请独立的memcached集群，请联系DBA戴骁雄
客户端通过spymemcached实现对memcached的访问

b、ehcache

ehcache其实是应用本地缓存，它的优势是性能高，相比memcached没有网络开销，但除非对性能要求极高的业务，建议还是使用memcached

c、dcache

dcache是由腾讯MIG开发的一套KV系统，相比memcached，它提供了数据持久化的功能
在很多业务场景下，使用dcache并不是用来取代memcached，而是取代mysql，数据的写和读都直接操作dcache
avatar-cache针对dcache接口进行了封装，只需要在申请缓存时指定缓存集群为dcache
目前dcache在beta环境、线上环境有部署，由DBA负责维护，其他环境暂时没有部署
对于数据量较大或访问较多的业务，可以申请独立的集群，如果需要申请独立的dcache集群，请联系DBA戴骁雄

d、redis-cluster

redis 既可以作为缓存也可以作为 KV 来使用。作为缓存，相较于 memcached，redis 的接口更加丰富，支持 list，hash，set 等数据结构，同时具有很高的性能。作为 KV，redis-cluster 提供了主备，数据迁移，数据持久化等的支持。但由于 redis 是全内存的，存储成本比较高，而且一般都有主备，更加剧了内存消耗，所以一般 redis-cluster 的使用场景是：数据量不太大的存储，或者数据量大的缓存。


## 客户端使用说明
a) 依赖

```
<dependency>
    <groupId>com.dianping.squirrel</groupId>
    <artifactId>squirrel-client</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```


b) Spring 配置使用方式

```
<?xml version="1.0" encoding="UTF-8"?>

<beans xmlns="http://www.springframework.org/schema/beans"  
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:aop="http://www.springframework.org/schema/aop"  
    xmlns:tx="http://www.springframework.org/schema/tx" xmlns:context="http://www.springframework.org/schema/context"  
    xmlns:amq="http://activemq.apache.org/schema/core" xmlns:squirrel="http://www.dianping.com/schema/squirrel"  
    xsi:schemaLocation="  
    http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.5.xsd  
    http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-2.5.xsd  
    http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-2.5.xsd  
    http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-2.5.xsd  
    http://activemq.apache.org/schema/core http://activemq.apache.org/schema/core/activemq-core.xsd  
    http://www.dianping.com/schema/squirrel http://www.dianping.com/schema/squirrel/squirrel-1.0.xsd">  

    <!-- squirrel store annotation -->
    <!-- <squirrel:store store-type="redis-default"/> -->
    <!-- store-type 是可选参数，用于指定存储类型，用于拿到指定存储的客户端 -->
    <squirrel:store />

    <!-- spring annotation -->
    <context:component-scan base-package="com.dianping" />  
    <context:annotation-config />
    
</beans>
```
通过以上配置后，可以直接在spring中拿到name是storeClient、实现了StoreClient接口的bean。例如：

```
@Component
public class Bean {

    @Autowired
    private StoreClient storeClient;

    public String getValue(String key) {
        StoreKey storeKey = new StoreKey("myredis", key);
        return storeClient.get(storeKey);
    }
}
```

c) Java API 使用方式

```
StoreClient storeClient = StoreClientFactory.getStoreClient();
```

## StoreClient 接口

```
public interface StoreClient {
    
    /**
     * 获取指定 Key 的值
     * @param key 要获取的 Key
     * @return Key 对应的值，如果 Key 不存在，返回 null
     * @throws StoreException 异常都是 StoreException 的子类且是 RuntimeException，可以根据需要捕获相应异常。
     *         如：如果需要捕获超时异常，可以捕获 StoreTimeoutException
     */
    public <T> T get(StoreKey key) throws StoreException;
    
    /**
     * 设置 Key 对应的值为 Value，如果 Key 不存在则添加，如果 Key 已经存在则覆盖
     * @param key 要设置的 Key
     * @param value 要设置的 Value
     * @return 如果成功，返回 true<br>
     *         如果失败，返回 false
     * @throws StoreException 异常都是 StoreException 的子类且是 RuntimeException，可以根据需要捕获相应异常。
     *         如：如果需要捕获超时异常，可以捕获 StoreTimeoutException
     */
    public Boolean set(StoreKey key, Object value) throws StoreException;
    
    /**
     * 添加 Key 对应的值为 Value，只有当 Key 不存在时才添加，如果 Key 已经存在，不改变现有的值
     * @param key 要添加的  Key
     * @param value 要添加的 Value
     * @return 如果 Key 不存在且添加成功，返回 true<br>
     *         如果 Key 已经存在，返回 false
     * @throws StoreException 异常都是 StoreException 的子类且是 RuntimeException，可以根据需要捕获相应异常。
     *         如：如果需要捕获超时异常，可以捕获 StoreTimeoutException
     */
    public Boolean add(StoreKey key, Object value) throws StoreException;
    
    /**
     * 删除指定 Key
     * @param key 要删除的 Key
     * @return 如果 Key 存在且被删除，返回 true<br>
     *         如果 Key 不存在，返回 false
     * @throws StoreException 异常都是 StoreException 的子类且是 RuntimeException，可以根据需要捕获相应异常。
     *         如：如果需要捕获超时异常，可以捕获 StoreTimeoutException
     */
    public Boolean delete(StoreKey key) throws StoreException;
    
    /**
     * 异步获取指定 Key 的值
     * @param key 要获取的 Key
     * @return 返回 Future 对象<br>
     *         如果操作成功，Future 返回 Key 对应的值<br>
     *         如果 Key 不存在，Future 返回 null
     * @throws StoreException 异常都是 StoreException 的子类且是 RuntimeException，可以根据需要捕获相应异常。
     */
    public <T> Future<T> asyncGet(StoreKey key) throws StoreException;
    
    /**
     * 异步设置 Key 对应的值为 Value，如果 Key 不存在则添加，如果 Key 已经存在则覆盖
     * @param key 要设置的 Key
     * @param value 要设置的 Value
     * @return 返回 Future 对象<br>
     *         如果操作成功，Future 返回 true<br>
     *         如果操作失败，Future 返回 false
     * @throws StoreException 异常都是 StoreException 的子类且是 RuntimeException，可以根据需要捕获相应异常。
     */
    public Future<Boolean> asyncSet(StoreKey key, Object value) throws StoreException;
    
    /**
     * 异步添加 Key 对应的值为 Value，只有当 Key 不存在时才添加，如果 Key 已经存在，不改变现有的值
     * @param key 要添加的 Key
     * @param value 要添加的 Value
     * @return 返回 Future 对象<br>
     *         如果 Key 不存在且添加成功，Future 返回 true<br>
     *         如果 Key 已经存在，Future 返回 false
     * @throws StoreException 异常都是 StoreException 的子类且是 RuntimeException，可以根据需要捕获相应异常。
     */
    public Future<Boolean> asyncAdd(StoreKey key, Object value) throws StoreException;
    
    /**
     * 异步删除指定 Key
     * @param key 要删除的 Key
     * @return 返回 Future 对象<br>
     *         如果 Key 存在且被删除，Future 返回 true<br>
     *         如果 Key 不存在，Future 返回 false
     * @throws StoreException 异常都是 StoreException 的子类且是 RuntimeException，可以根据需要捕获相应异常。
     */
    public Future<Boolean> asyncDelete(StoreKey key) throws StoreException;
    
    /**
     * 异步获取指定 Key 的值
     * @param key 要获取的 Key
     * @param callback 操作完成时的回调函数
     * @return 返回 null<br>
     *         如果操作成功，会调用 callback 的 onSuccess 方法，参数为 Key 对应的值<br>
     *         如果 Key 不存在，会调用 callback 的 onSuccess 方法，参数为 null<br>
     *         如果发生异常，会调用 callback 的 onFailure 方法，参数为异常
     */
    public <T> Void asyncGet(StoreKey key, StoreCallback<T> callback);
    
    /**
     * 异步设置 Key 对应的值为 Value，如果 Key 不存在则添加，如果 Key 已经存在则覆盖
     * @param key 要设置的 Key
     * @param value 要设置的 Value
     * @param callback 操作完成时的回调函数
     * @return 返回 null<br>
     *         如果操作成功，会调用 callback 的 onSuccess 方法，参数为 true<br>
     *         如果操作失败，会调用 callback 的 onSuccess 方法，参数为 false<br>
     *         如果发生异常，会调用 callback 的 onFailure 方法，参数为异常
     */
    public Void asyncSet(StoreKey key, Object value, StoreCallback<Boolean> callback);
    
    /**
     * 异步添加 Key 对应的值为 Value，只有当 Key 不存在时才添加，如果 Key 已经存在，不改变现有的值
     * @param key 要添加的 Key
     * @param value 要添加的 Value
     * @param callback 操作完成时的回调函数
     * @return 返回 null<br>
     *         如果 Key 不存在且添加成功，会调用 callback 的 onSuccess 方法，参数为 true<br>
     *         如果 Key 已经存在，会调用 callback 的 onSuccess 方法，参数为 false<br>
     *         如果发生异常，会调用 callback 的 onFailure 方法，参数为异常
     */
    public Void asyncAdd(StoreKey key, Object value, StoreCallback<Boolean> callback);
    
    /**
     * 异步删除指定 Key
     * @param key 要删除的 Key
     * @param callback 操作完成时的回调函数
     * @return 返回 null<br>
     *         如果 Key 存在且被删除，会调用 callback 的 onSuccess 方法，参数为 true<br>
     *         如果 Key 不存在，会调用 callback 的 onSuccess 方法，参数为 false<br>
     *         如果发生异常，会调用 callback 的 onFailure 方法，参数为异常
     */
    public Void asyncDelete(StoreKey key, StoreCallback<Boolean> callback);
    
    /**
     * 增加指定 Key 的值
     * @param key 要增加的 Key
     * @param amount 要增加的值
     * @return 增加后的值，如果 Key 不存在，会创建这个 Key，且值为0，然后再增加
     * @throws StoreException 异常都是 StoreException 的子类且是 RuntimeException，可以根据需要捕获相应异常。
     *         如：如果需要捕获超时异常，可以捕获 StoreTimeoutException
     */
    public Long increase(StoreKey key, int amount) throws StoreException;

    /**
     * 减少指定 Key 的值
     * @param key 要减少的 Key
     * @param amount 要减少的值
     * @return 减少后的值，如果 Key 不存在，会创建这个 Key，且值为0，然后再减少
     * @throws StoreException 异常都是 StoreException 的子类且是 RuntimeException，可以根据需要捕获相应异常。
     *         如：如果需要捕获超时异常，可以捕获 StoreTimeoutException
     */
    public Long decrease(StoreKey key, int amount) throws StoreException;
    
    /**
     * 批量获取指定 Key 的值
     * @param keys 要获取的 Key 列表
     * @return 返回存在的 Key <-> Value Map，如果某个 Key 不存在，则在返回的 Map 中没有这个 Key
     * @throws StoreException 异常都是 StoreException 的子类且是 RuntimeException，可以根据需要捕获相应异常。
     *         如：如果需要捕获超时异常，可以捕获 StoreTimeoutException
     */
    public <T> Map<StoreKey, T> multiGet(List<StoreKey> keys) throws StoreException;
    
    /**
     * 异步批量获取指定 Key 的值
     * @param keys 要获取的 Key 列表
     * @param callback 操作完成时的回调函数
     * @return 返回 null<br>
     *         如果操作成功，会调用 callback 的 onSuccess 方法，参数为存在的 Key <-> Value Map，
     *         如果某个 Key 不存在，则在传入的参数中没有这个 Key<br>
     *         如果发生异常，会调用 callback 的 onFailure 方法，参数为异常
     */
    <T> Void asyncMultiGet(List<StoreKey> keys, StoreCallback<Map<StoreKey, T>> callback);
    
    /**
     * 批量设置指定的 Key 到指定的 Value
     * @param keys 要设置的 Key 列表
     * @param values 要设置的 Value 列表
     * @return 如果操作成功，返回 true<br>
     *         如果操作失败，返回 false
     * @throws StoreException 异常都是 StoreException 的子类且是 RuntimeException，可以根据需要捕获相应异常。
     *         如：如果需要捕获超时异常，可以捕获 StoreTimeoutException
     */
    public <T> Boolean multiSet(List<StoreKey> keys, List<T> values) throws StoreException;

    /**
     * 异步批量设置指定的 Key 到指定的 Value
     * @param keys 要设置的 Key 列表
     * @param values 要设置的 Value 列表
     * @param callback 操作完成时的回调函数
     * @return 返回 null<br>
     *         如果操作成功，会调用 callback 的 onSuccess 方法，参数为 true<br>
     *         如果操作失败，会调用 callback 的 onSuccess 方法，参数为 false<br>
     *         如果发生异常，会调用 callback 的 onFailure 方法，参数为异常
     */
	<T> Void asyncMultiSet(List<StoreKey> keys, List<T> values, StoreCallback<Boolean> callback);
}

```

## RedisStoreClient 接口
Redis cluster 专用接口支持一些 redis 特有的命令，支持 Hash，List 和 Set 的相关操作。
Redis 相关操作暂时只支持同步接口，multi 和 async 相关操作由于 jedis 驱动不支持，我们现在也不支持，将在后续版本增加。
一般情况下，不建议直接获取特定存储相关的实例，因为当你使用的 category 更换了后端存储类型，存储特定实例将得不到通知。

a) 获取 RedisStoreClient 实例

```
使用工厂方法
RedisStoreClient storeClient = (RedisStoreClient)StoreClientFactory.getStoreClient("redis-default");
redis-default 是申请存储时选择的 redis 集群的名字
 
```

b) RedisStoreClient 接口

```
public interface RedisStoreClient extends StoreClient {

	// expire related
	Boolean exists(StoreKey key);

	String type(StoreKey key);

	Boolean expire(StoreKey key, int seconds);

	/**
	 * @return TTL in seconds<br>
	 *         -2 if key does not exist<br>
	 *         -1 if key exists but has no associated expire
	 */
	Long ttl(StoreKey key);

	/**
	 * Remove the existing timeout on key
	 * @param key
	 * @return 
	 * 		true if the timeout was removed<br>
	 * 		false if key does not exist or does not have an associated timeout.
	 */
	Boolean persist(StoreKey key);

	// hash related
	/**
	 * @return
	 *		1 if field is a new field in the hash and value was set<br>
  	 *      0 if field already exists in the hash and the value was updated
	 */
	Long hset(StoreKey key, String field, Object value);

	<T> T hget(StoreKey key, String field);

	/**
	 * @return the number of fields that were removed
	 */
	Long hdel(StoreKey key, String... field);

	Set<String> hkeys(StoreKey key);

	List<Object> hvals(StoreKey key);

	Map<String, Object> hgetAll(StoreKey key);

	/**
	 * @return list of values for the fields, if some field
	 *         does not exist, a null value is in the returned list<br>
	 *         null if the key does not exist or fields are not specified
	 */
	List<Object> hmget(StoreKey key, final String... fields);
	
	Boolean hmset(StoreKey key, final Map<String, Object> valueMap);
	
	// list related
	/**
	 * Insert the values at the tail of the list stored at key
	 * 
	 * @return length of the list after the push operation
	 */
	Long rpush(StoreKey key, Object... value);

	/**
	 * Insert the values at the head of the list stored at key<br>
	 * Value are inserted one after the other to the head of the list
	 * 
	 * @return the length of the list after the push operations
	 */
	Long lpush(StoreKey key, Object... value);

	<T> T lpop(StoreKey key);

	<T> T rpop(StoreKey key);

	<T> T lindex(StoreKey key, long index);

	Boolean lset(StoreKey key, long index, Object value);

	/**
	 * Returns the length of the list stored at key

	 * @return the length of the list stored at key<br>
	 *         0 if the key does not exist
	 */
	Long llen(StoreKey key);

	/**
	 * Returns the specified elements of the list stored at key
	 *
	 * @return list of elements in the specified range
	 */
	List<Object> lrange(StoreKey key, long start, long end);

	/**
	 * Trim an existing list so that it will contain only the specified range of elements
	 * 
	 * @return
	 */
	Boolean ltrim(StoreKey key, long start, long end);

	// set related
	/**
	 * @return number of elements that were added to the set
	 */
	Long sadd(StoreKey key, Object... member);
	
	/**
	 * @return number of members that were removed from the set
	 */
	Long srem(StoreKey key, Object... member);

	Set<Object> smembers(StoreKey key);

	Long scard(StoreKey key);

	Boolean sismember(StoreKey key, Object member);
	
	// sorted set related
	
}

```


