## avatar-cache版本更新说明
------

· 2.6.7
优化了CacheKey的字符串拼接计算代码，提升了性能
增加了CacheServiceFactory接口，可以通过api方式使用CacheService
去除了无用的MonitorInterceptor
针对zookeeper连接丢失的问题加入了外部检测重建的功能
· 2.6.6
增加了dcache实现
CacheService接口进行了重构，增加了部分异步接口：
<T> Future<T> asyncGet(final CacheKey key) throws CacheException;
<T> void asyncGet(final CacheKey key, final CacheCallback<T> callback);
<T> void asyncBatchGet(final List<CacheKey> keys, final CacheCallback<Map<CacheKey, T>> callback);
Future<Boolean> asyncSet(final CacheKey key, final Object value) throws CacheException;
void asyncSet(final CacheKey key, final Object value, final CacheCallback<Boolean> callback);
Future<Boolean> asyncAddIfAbsent(final CacheKey key, final Object value) throws CacheException;
void asyncAddIfAbsent(final CacheKey key, final Object value, final CacheCallback<Boolean> callback);
boolean delete(CacheKey key) throws CacheException, TimeoutException;
Future<Boolean> asyncDelete(CacheKey key) throws CacheException;
· 2.6.5
修复了客户端收到缓存配置通知时因多线程问题导致配置可能无法生效的问题
· 2.6.4
修复了本地ehcache清缓存时cache-server写zookeeper压力过大导致有遗漏的问题，改为批量读写zookeeper
· 2.6.3
修复了热点key缓存失效后可能导致大量并发请求去数据库的问题，热点key在memcached里会保存两份数据
· 2.6.2
增加了独立的zookeeper集群作为缓存配置中心，缓存的配置不再依赖cache-server
解决了因swallow不稳定导致清缓存失败的问题，改用zookeeper作为清缓存的消息通道
memcached连接改为读写分离，get和set请求不再共享一个连接，避免相互影响
支持多个连接，get和set请求都可以分别设置连接池的连接数，降低了超时的可能性，提高了客户端性能，经过测试，新版本客户端QPS达到23000，是老版本的2.3倍
加入了mget命中率统计，在cat-event里会打Cache.mget.keyCount.xxx（传的key个数）和Cache.mget.hitRate.xxx（命中率百分比）
加入了qps统计
加入了针对每个memcached节点的ip统计
· 2.5.10
修复了读取long类型数据时转换错误的bug
· 2.5.9
增加了以下接口：
boolean set(CacheKey key, Object value) throws CacheException, TimeoutException;
boolean set(CacheKey key, Object value, long timeout) throws CacheException, TimeoutException;
· 2.5.7  
增加了以下接口：
add：
boolean addIfAbsent(CacheKey key, Object value) throws CacheException, TimeoutException;
boolean addIfAbsent(CacheKey key, Object value, long timeout) throws CacheException, TimeoutException;
void addIfAbsentWithNoReply(CacheKey key, Object value) throws CacheException;
cas：
<T> CASValue<T> gets(CacheKey key) throws CacheException, TimeoutException;
CASResponse cas(CacheKey key, long casId, Object value) throws CacheException, TimeoutException;
desc/inc：
public long increment(CacheKey key, int amount) throws CacheException, TimeoutException;
public long increment(CacheKey key, int amount, long defaultValue) throws CacheException, TimeoutException;
public long decrement(CacheKey key, int amount) throws CacheException, TimeoutException;
public long decrement(CacheKey key, int amount, long defaultValue) throws CacheException, TimeoutException;
· 2.5.6  
增加了以下原子接口：
public long increment(CacheKey key, int amount);
public long decrement(CacheKey key, int amount);
· 2.5.3  
解决了部分使用swallow项目的兼容性问题（swallow bean冲突）；   
· 2.5.2  
升级了cache客户端依赖的swallow版本，解决批量清cache导致mongodb负载过高的问题；  
加入了cache对象大小等cat监控信息； 
· 2.4.4  
加入了cache操作异常cat监控信息； 
针对mget进行了优化，之前的<T> List<T> mGet(List<CacheKey> keys);接口如果有一个key没返回数据会导致整个mget返回null，这样会使得memcached查询资源被严重浪费，下面2个接口是尽量返回已经查到的key结果列表：
1、<T> Map<CacheKey, T> mGetWithNonExists(List<CacheKey> keys);
2、<T> List<T> mGet(final List<CacheKey> keys, final boolean returnNullIfAnyKeyMissed);//请设置returnNullIfAnyKeyMissed为false，返回结果List与传入参数keys的List顺序保持一致和对应，如果某个key返回null在返回结果List里是null
