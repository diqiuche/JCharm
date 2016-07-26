/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.source;

import java.io.Serializable;

/**
 * 数据缓存监听.
 */
public interface DataCacheListener {

	/**
	 * 插入缓存.
	 *
	 * @param <T> Entity类泛型
	 * @param clazz Entity类
	 * @param entitys Entity对象
	 */
	public <T> void insertCache(Class<T> clazz, T... entitys);

	/**
	 * 更新缓存.
	 *
	 * @param <T> Entity类泛型
	 * @param clazz Entity类
	 * @param entitys Entity对象
	 */
	public <T> void updateCache(Class<T> clazz, T... entitys);

	/**
	 * 删除缓存.
	 *
	 * @param <T> Entity类泛型
	 * @param clazz Entity类
	 * @param ids 标识
	 */
	public <T> void deleteCache(Class<T> clazz, Serializable... ids);

}
