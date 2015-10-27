# Squirrel 用户文档
---
## 概述
Squirrel 是点评的 Key-Value 存储框架，继承自 Avatar-Cache 缓存框架。存储的申请流程和 Avatar-Cache 缓存一样，存储集群选择 redis 的集群就可以，存储过期时间可以输入-1，这样存储的 Key-Value

## 主要改变
1. API 接口较 Avatar-Cache 更加合理。
2. 支持基于 redis-cluster 的 KV 存储。
3. 后续版本会增加多备份，自动扩容等功能。

## 客户端使用说明
a. 依赖

```
<dependency>
    <groupId>com.dianping.squirrel</groupId>
    <artifactId>squirrel-client</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

b. Spring 配置使用方式

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
    <!-- <squirrel:store store-type="redis-hua"/> -->
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

c. Java API 使用方式

```
StoreClient storeClient = StoreClientFactory.getStoreClient();
```

## StoreClient 接口

```
public interface StoreClient {
    
    // sync single key operations
    public <T> T get(StoreKey key) throws StoreException;
    
    public Boolean set(StoreKey key, Object value) throws StoreException;
    
    public Boolean add(StoreKey key, Object value) throws StoreException;
    
    public Boolean delete(StoreKey key) throws StoreException;
    
    // future single key operations
    public <T> Future<T> asyncGet(StoreKey key) throws StoreException;
    
    public Future<Boolean> asyncSet(StoreKey key, Object value) throws StoreException;
    
    public Future<Boolean> asyncAdd(StoreKey key, Object value) throws StoreException;
    
    public Future<Boolean> asyncDelete(StoreKey key) throws StoreException;
    
    // callback single key operations
    public <T> Void asyncGet(StoreKey key, StoreCallback<T> callback);
    
    public Void asyncSet(StoreKey key, Object value, StoreCallback<Boolean> callback);
    
    public Void asyncAdd(StoreKey key, Object value, StoreCallback<Boolean> callback);
    
    public Void asyncDelete(StoreKey key, StoreCallback<Boolean> callback);
    
    // increment & decrement
    public Long increase(StoreKey key, int amount) throws StoreException;

    public Long decrease(StoreKey key, int amount) throws StoreException;
    
    // batch operations
    public <T> Map<StoreKey, T> multiGet(List<StoreKey> keys) throws StoreException;
    
    <T> Void asyncMultiGet(List<StoreKey> keys, StoreCallback<Map<StoreKey, T>> callback);
    
    public <T> Boolean multiSet(List<StoreKey> keys, List<T> values) throws StoreException;

	<T> Void asyncMultiSet(List<StoreKey> keys, List<T> values, StoreCallback<Boolean> callback);

}

```

## RedisStoreClient 接口
Redis cluster 专用接口支持一些 redis 特有的命令，支持 Hash，List 和 Set 的相关操作。
Redis 相关操作暂时只支持同步接口，multi 和 async 相关操作由于 jedis 驱动不支持，我们现在也不支持，将在后续版本增加。

1. 获取 RedisStoreClient 实例

```
使用工厂方法
RedisStoreClient storeClient = (RedisStoreClient)StoreClientFactory.getStoreClient("redis-hua");
redis-hua 是申请存储时选择的 redis 集群的名字
 
```


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