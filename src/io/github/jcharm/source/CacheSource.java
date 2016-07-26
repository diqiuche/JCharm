/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.source;

import java.io.Serializable;
import java.nio.channels.CompletionHandler;
import java.util.Collection;

/**
 * 缓存数据源.
 *
 * @param <K> key的类型
 * @param <V> value的类型
 */
public interface CacheSource<K extends Serializable, V extends Object> {

	/**
	 * 判断是否打开缓存数据源.
	 *
	 * @return boolean
	 */
	default boolean isOpen() {
		return true;
	}

	/**
	 * 判断指定key是否在缓存数据源中存在.
	 *
	 * @param key key值
	 * @return boolean
	 */
	public boolean exists(final K key);

	/**
	 * 根据指定key从缓存数据源中得到value.
	 *
	 * @param key key值
	 * @return value值
	 */
	public V get(final K key);

	/**
	 * 根据指定key从缓存数据源中得到value, 并刷新其在缓存数据源中的过期时间.
	 *
	 * @param key key值
	 * @param expireSeconds 过期时间(秒)
	 * @return value值
	 */
	public V getAndRefresh(final K key, final int expireSeconds);

	/**
	 * 根据指定key刷新其在缓存数据源中的过期时间.
	 *
	 * @param key key值
	 * @param expireSeconds 过期时间(秒)
	 */
	public void refresh(final K key, final int expireSeconds);

	/**
	 * 将指定key和value设置到缓存数据源中.
	 *
	 * @param key key值
	 * @param value value值
	 */
	public void set(final K key, final V value);

	/**
	 * 将指定key和value设置到缓存数据源中, 并设置其在缓存数据源中的过期时间.
	 *
	 * @param expireSeconds 过期时间(秒)
	 * @param key key值
	 * @param value value值
	 */
	public void set(final int expireSeconds, final K key, final V value);

	/**
	 * 根据指定key设置其在缓存数据源中的过期时间.
	 *
	 * @param key key值
	 * @param expireSeconds 过期时间(秒)
	 */
	public void setExpireSeconds(final K key, final int expireSeconds);

	/**
	 * 将指定key从缓存数据源中移除.
	 *
	 * @param key key值
	 */
	public void remove(final K key);

	/**
	 * 根据指定key从缓存数据源中得到value集合.
	 *
	 * @param key key值
	 * @return value集合
	 */
	public Collection<V> getCollection(final K key);

	/**
	 * 根据指定key从缓存数据源中得到value集合, 并且刷新其在缓存数据源中的过期时间.
	 *
	 * @param key key值
	 * @param expireSeconds 过期时间(秒)
	 * @return value集合
	 */
	public Collection<V> getCollectionAndRefresh(final K key, final int expireSeconds);

	/**
	 * 将指定key和value添加到List.
	 *
	 * @param key key值
	 * @param value value值
	 */
	public void appendListItem(final K key, final V value);

	/**
	 * 将指定key和value从List移除.
	 *
	 * @param key key值
	 * @param value value值
	 */
	public void removeListItem(final K key, final V value);

	/**
	 * 将指定key和value添加到Set.
	 *
	 * @param key key值
	 * @param value value值
	 */
	public void appendSetItem(final K key, final V value);

	/**
	 * 将指定key和value从Set移除.
	 *
	 * @param key key值
	 * @param value value值
	 */
	public void removeSetItem(final K key, final V value);

	// ======================================异步版=======================================

	/**
	 * 判断是否打开缓存数据源.
	 *
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 */
	default void isOpen(final CompletionHandler<Boolean, Void> handler) {
		if (handler != null) {
			handler.completed(Boolean.TRUE, null);
		}
	}

	/**
	 * 判断指定key是否在缓存数据源中存在.
	 *
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param key key值
	 */
	public void exists(final CompletionHandler<Boolean, K> handler, final K key);

	/**
	 * 根据指定key从缓存数据源中得到value.
	 *
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param key key值
	 */
	public void get(final CompletionHandler<V, K> handler, final K key);

	/**
	 * 根据指定key从缓存数据源中得到value, 并刷新其在缓存数据源中的过期时间.
	 *
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param key key值
	 * @param expireSeconds 过期时间(秒)
	 * @return value值
	 */
	public void getAndRefresh(final CompletionHandler<V, K> handler, final K key, final int expireSeconds);

	/**
	 * 根据指定key刷新其在缓存数据源中的过期时间.
	 *
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param key key值
	 * @param expireSeconds 过期时间(秒)
	 */
	public void refresh(final CompletionHandler<Void, K> handler, final K key, final int expireSeconds);

	/**
	 * 将指定key和value设置到缓存数据源中.
	 *
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param key key值
	 * @param value value值
	 */
	public void set(final CompletionHandler<Void, K> handler, final K key, final V value);

	/**
	 * 将指定key和value设置到缓存数据源中, 并设置其在缓存数据源中的过期时间.
	 *
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param expireSeconds 过期时间(秒)
	 * @param key key值
	 * @param value value值
	 */
	public void set(final CompletionHandler<Void, K> handler, final int expireSeconds, final K key, final V value);

	/**
	 * 根据指定key设置其在缓存数据源中的过期时间.
	 *
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param key key值
	 * @param expireSeconds 过期时间(秒)
	 */
	public void setExpireSeconds(final CompletionHandler<Void, K> handler, final K key, final int expireSeconds);

	/**
	 * 将指定key从缓存数据源中移除.
	 *
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param key key值
	 */
	public void remove(final CompletionHandler<Void, K> handler, final K key);

	/**
	 * 根据指定key从缓存数据源中得到value集合.
	 *
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param key key值
	 * @return value集合
	 */
	public void getCollection(final CompletionHandler<Collection<V>, K> handler, final K key);

	/**
	 * 根据指定key从缓存数据源中得到value集合, 并且刷新其在缓存数据源中的过期时间.
	 *
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param key key值
	 * @param expireSeconds 过期时间(秒)
	 * @return value集合
	 */
	public void getCollectionAndRefresh(final CompletionHandler<Collection<V>, K> handler, final K key, final int expireSeconds);

	/**
	 * 将指定key和value添加到List.
	 *
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param key key值
	 * @param value value值
	 */
	public void appendListItem(final CompletionHandler<Void, K> handler, final K key, final V value);

	/**
	 * 将指定key和value从List移除.
	 *
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param key key值
	 * @param value value值
	 */
	public void removeListItem(final CompletionHandler<Void, K> handler, final K key, final V value);

	/**
	 * 将指定key和value添加到Set.
	 *
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param key key值
	 * @param value value值
	 */
	public void appendSetItem(final CompletionHandler<Void, K> handler, final K key, final V value);

	/**
	 * 将指定key和value从Set移除.
	 *
	 * @param handler CompletionHandler异步IO操作结果的回调接口
	 * @param key key值
	 * @param value value值
	 */
	public void removeSetItem(final CompletionHandler<Void, K> handler, final K key, final V value);

}
