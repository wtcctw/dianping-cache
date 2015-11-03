package com.dianping.squirrel.client;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import com.dianping.squirrel.client.core.StoreCallback;
import com.dianping.squirrel.common.exception.StoreException;

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

	public String getFinalKey(StoreKey storeKey);
	
    public Boolean delete(String finalKey) throws StoreException;
    
    public boolean isDistributed();

}
