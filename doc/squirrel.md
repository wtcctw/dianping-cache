# Squirrel 用户文档
---
## 概述
Squirrel 是点评的 Key-Value 存储框架，继承自 Avatar-Cache 缓存框架。主要包括管理端（squirrel-console）、客户端（squirrel-client）和存储节点（memcached、redis、dcache等）组成。

### 基本概念

* key：存储的键，用于唯一定位一个 value
* value：存储的值，经序列化后存储在后端存储中
* cluster：存储集群，用于指定后端的存储节点，主要类型有 memcached，redis，dcache，ehcache 等
* category：存储类别，用于指定某一类存储数据，可以类比的理解为数据库中的一张表，或者一个 namespace，存储类别被用于这个类别的 key 的前缀

### 存储 key 的组成

存储 key 的组成包括3部分：category、template、version，最终存储的 key 字符串是由 **${category}.${template}_${version}** 组合而成：
1)、category 是某一类存储的名称，它不是存储 key，它是存储 key 的前缀，可以理解它是数据库里的一张表
2)、template 是存储 key 的模板，由参数组成，如c{0}d{1}这个模板，{0}代表第1个参数比如userId，{1}代表第2个参数比如cityId，template 会被 StoreKey 构造函数里的params参数列表填充，最后的字符串类似：c1000d2
3)、version 是 squirrel 内部管理的一个数字，如果这个 category 清一次缓存，那么 version 会加1，这样最后的key字符串会变成全新的字符串，应用使用新的key之后需要重新从数据源加载数据到缓存
比如申请一个category：shopDetail，template为s{0}，最终落到存储上的key字符串就是：
> StoreKey key = new StoreKey("shopDetail", shopId); // shopId = 123456
> String finalKey = storeClient.getFinalKey(key); // finalKey = shopDetail.s123456_0

### 基本架构
![Squirrel 架构](http://code.dianpingoa.com/arch/squirrel/raw/master/doc/squirrel-arch.png)

* squirrel-console 是管理端，主要负责
	* 配置存储集群(cluster)和存储类别(category)的属性
	* 查询某个 key 对应的 value
	* 查询变更操作日志，如什么时候修改了集群的属性等
	* 存储节点监控，实时监控所有后端存储节点的健康状况并报警
	* 存储节点的扩容，缩容，支持一键扩容缩容
	* 存储节点数据迁移
* zookeeper 用作配置中心，所有配置保存在 zookeeper 上，同时保存一份到数据库中，所有配置变更通过 zookeeper 实时推送到相应客户端。同时 zookeeper 也作为分布式清缓存的消息中心，本地缓存的清除消息通过 zookeeper 通知到相应客户端进行本地清除。
* squirrel-client 是客户端，启动时从 zookeeper 读取缓存集群和缓存类别的配置，然后基于配置，直接连接相应的后端存储节点，同时响应存储集群和存储类别的配置变更事件.
* 存储节点，视不同的存储类别，分为好几种，当前线上部署的有：memcached, redis, ehcache, dcache


## 用户接入

用户接入主要在 DBA 的存储管理平台完成：

a、存储申请和变更
为了统一管理存储，需要申请存储，申请存储需要指定以下：
1)、存储类别定义，包括存储类别（category）、key 的模板、失效时间、是否热点key等
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

##	支持的存储类型

a、memcached

memcached集群由DBA维护，线上一般按业务划分为不同的集群，对于数据量较大或访问较多的业务，一般需要申请独立的memcached集群，如果需要申请独立的memcached集群，请联系DBA戴骁雄
客户端通过spymemcached实现对memcached的访问

b、ehcache

ehcache 是应用本地缓存，它的优势是性能高，相比memcached没有网络开销，但除非对性能要求极高的业务，建议还是使用memcached

c、dcache

dcache 是由腾讯MIG开发的一套KV系统，相比memcached，它提供了数据持久化的功能。在很多业务场景下，使用 dcache 并不是用来取代 memcached，而是取代 mysql，数据的写和读都直接操作 dcache。squirrel-client 针对 dcache 接口进行了封装，只需要在申请缓存时指定缓存集群为 dcache。目前 dcache 在 beta 环境、PPE环境，线上环境有部署，由 DBA 负责维护，其他环境暂时没有部署。对于数据量较大或访问较多的业务，可以申请独立的集群，如果需要申请独立的 dcache 集群，请联系 DBA 戴骁雄

d、redis-cluster

redis 既可以作为缓存也可以作为 KV 来使用。作为缓存，相较于 memcached，redis 的接口更加丰富，支持 list，hash，set 等数据结构，同时具有很高的性能。作为 KV，redis-cluster 提供了主备，数据迁移，数据持久化等的支持。但由于 redis 是全内存的，存储成本比较高，而且一般都有主备，更加剧了内存消耗，所以一般 redis-cluster 的使用场景是：**数据量不太大的存储，或者数据量大的缓存**。


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

## 特定存储相关接口
各种后端存储都支持一些自身特有的命令。如 dcache 支持二级表，memcached 支持 cas 操作，redis支持 Hash，List 和 Set 的操作等。
如果要使用和后端存储相关的特定接口时，可以使用以下方式获得特定存储相关的接口：

**请注意：**Redis 相关操作暂时只支持同步接口，multi 和 async 相关操作由于 jedis 驱动不支持，我们现在也不支持，将在后续版本增加。

**请注意：**一般情况下，不建议直接获取特定存储相关的实例，因为当你使用的 category 更换了后端存储类型，存储特定实例将得不到通知。

获取特定存储相关接口的实例

```
使用工厂方法
RedisStoreClient storeClient = (RedisStoreClient)StoreClientFactory.getStoreClientByCategory("myredis");
// myredis 是申请存储时指定的存储类别 category，如果这个 category 的存储集群是 redis 类型的，那么返回的就是 RedisStoreClient 接口，同样的，如果这个 category 的存储集群是 memcached 的，那么返回的就是 MemcachedStoreClient 接口。
 
```

## 缓存 API 使用约定
![缓存 API 使用约定](http://code.dianpingoa.com/arch/squirrel/raw/master/doc/cache-pattern.png)

1. 应用从缓存取值，如果取到，返回
2. 如果没取到，应用从数据库 load 数据
3. 把 load 到的数据放到缓存中

相应的，缓存 api 的接口遵循这一模式：
1. 如果缓存中有值，返回值
2. 如果缓存中没有值，返回 null，应用需要从数据库 load
3. 如果发生异常，抛出相应的异常，包括超时异常，连接异常等


## 缓存清理
申请存储时，会要求指定 category 的过期时间，如果过期时间为负值，则认为 category 需要持久存储，不做过期处理。如果是正值，那么 key 会在指定时间后过期，无法再取到。再次去取过期的 key 会返回 null，这时需要应用方从数据源再次 load 数据并放入缓存中。如果在缓存数据过期前就需要清楚缓存，那么就要用到缓存清理。

squirrel 支持两种缓存清理：

* 清理单个 key
* 清理整个 category，category 下所有 key 都清除

#### 清理单个 key
* 对于 memcached，redis，dcache 等远程存储，可以直接通过客户端清除对应的 key。
* 对于 ehcache 这样的本地存储，需要通过消息中间件通知到所有使用这个 category 的客户端，进行相应的清除操作。在 squirrel 中，如上面的架构图所示，使用了 zookeeper 作为消息中间件，来通知对应的客户端进行清除操作。

#### 清理整个 category
清理整个 category，用到了上面 key 组成中的 version 字段，清理整个 cagetory 的实现，就是把这个 category 的属性中的 version 字段 +1。然后通过 zookeeper 通知到所有使用了这个 category 的客户端。这样客户端再次从缓存中取值时，因为 version 变了，所以最后的 final key 也变了，不再能取到原来的值，变相地清除了 category 下的所有 key。原有的 key 通过过期时间让其自动淘汰。

## 热点 key
所谓热点 key，就是这个 key 的访问 qps 极高，如果这个 key 一旦过期或者被清除，可能导致很多客户端同时取到 null，而同时去从数据库 load 数据，瞬间的高流量可能导致数据库顶不住压力而崩溃。针对热点 key，memcahed 对于热点 key 的处理如下：

* 热点 key 的写入
	1. 保存热点 key，value 到 memcached
	2. 保存热点 key 的备份 key，value 到 memcached，备份 key 的名字为 key + "_h"，value 不变，过期时间为热点 key 的过期时间 + 缓冲时间，缓冲时间一般是10s，设置为10s的原因是一般情况下10s足够从数据库 load 数据了
* 热点 key 的读取
	1. 读取热点 key 对应的值，如果读到，直接返回。如果发生异常，抛出异常
	2. 如果读取到的值为 null，那么分情况，一种是热点 key 被清除了，一种是热点 key 过期了：
		* 热点 key 被清除了：尝试对热点 key 加锁，如果加锁成功，返回 null，由客户端去数据库 load 数据并放入缓存中；如果加锁失败，尝试去取热点 key 上一个版本的数据，上一个版本的 key 由当前热点 key 的version-1得到，并返回取到的数据
		* 热点 key 过期了：尝试对热点 key 加锁，如果加锁成功，返回 null，由客户端去数据库 load 数据并放入缓存中；如果加锁失败，尝试去取热点 key 的备份 key 的数据，并返回取到的数据
		* 对热点 key 加锁的过程是这样的：加锁也是在 memcached 上新增一个 key，key 的名字是热点 key 的名字加上 "_lock"，因为 memcached 的 add 操作是原子的，而且仅当不存在这个 key 时才能添加成功，所以可以利用这一特性做一个互斥锁，这个加锁的 key 的超时时间设置为30s，避免长期占用锁。
* 热点 key 的删除
	1. 清除热点 key
	2. 清除热点 key 的备份 key

## 存储监控
### 客户端
### 服务端

## 扩容缩容
### memcached
新建的 memcached 集群都由 docker 来负责创建和销毁实例。memcached 作为缓存，在扩容，缩容时不需要做数据迁移。

* 扩容：首先申请一个 memcached 的 docker 实例，待实例启动后，验证 memcached 端口开启，把实例 ip 加到 memcached 集群中，通过 zookeeper 通知客户端集群配置已更新，客户端基于最新的集群配置重建连接
* 缩容：把 memcached 实例 ip 从集群中移除，通过 zookeeper 通知客户端集群配置已更新，客户端基于最新的集群配置重建连接，最后销毁 docker 实例

### redis-cluster
redis 集群也是部署在 docker 上，redis 实例通过 docker 来创建和销毁。redis 我们部署的是 redis cluster。每个 redis cluster 至少有3个 master，每个 master 都有一个 slave。redis 实例默认都启用了 aof，每秒会把 aof 刷到磁盘，保证数据是持久化的，而且即使丢数据，也仅丢失最近1秒的数据。redis 定位为 KV，所以默认数据不会过期，过期清理策略是 noeviction，也就是说如果内存满了，不会淘汰旧的数据，而是返回错误。

redis 的扩容缩容涉及到数据的迁移，比 memcached 要复杂的多。但整个迁移过程并不会阻塞客户端，客户端的读写照常进行，是无缝的。

* 扩容：redis 的扩容都是成对的扩，也就是说最小扩容单位是1主1从，也可以一下子扩多主多从。扩容步骤如下：首先申请两个 redis 实例，一个作为 master，一个作为 slave，把 master 和 slave 加入现有 redis 集群中，设置好主备关系。然后计算需要从集群现有节点迁移的数据量，接着从集群现有节点一个 key 一个 key 的迁移数据。等到所有 key 都迁移完成，redis cluster 会更新路由表，然后通知客户端
* 缩容：redis 缩容之前，需要先迁移已有实例上的数据，等到所有 key 都从已有实例上迁移后，把 master 和 slave 从集群移除，然后通过 docker 销毁这两个实例

### dcache
dcache 的扩容和缩容由 dba 操作 dcache 的管理端进行，不在 squirrel 的管理范围之内。

## 后续展望
### redis-cluster
#### 客户端
#### 管理端
### 冷热分离