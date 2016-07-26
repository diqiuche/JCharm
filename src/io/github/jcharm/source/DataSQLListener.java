/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.source;

/**
 * 数据SQL监听.
 *
 * @see DataSQLEvent
 */
public interface DataSQLListener {

	/**
	 * 插入数据.
	 *
	 * @param sqls String
	 */
	public void insert(String... sqls);

	/**
	 * 更新数据.
	 *
	 * @param sqls String
	 */
	public void update(String... sqls);

	/**
	 * 删除数据.
	 *
	 * @param sqls String
	 */
	public void delete(String... sqls);

}
