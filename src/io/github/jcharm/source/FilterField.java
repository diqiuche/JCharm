/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.source;

import java.io.Serializable;
import java.util.Objects;

/**
 * FilterField主要用于自身字段间的表达式, 如: a.recordid = a.parentid, a.parentid就需要FilterKey来表示new FilterField("parentid").
 */
public class FilterField implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String column;

	/**
	 * 构造函数.
	 *
	 * @param column Entity类字段名
	 */
	public FilterField(final String column) {
		this.column = Objects.requireNonNull(column);
	}

	/**
	 * 获取过滤字段名.
	 *
	 * @return String
	 */
	public String getColumn() {
		return this.column;
	}

	@Override
	public String toString() {
		return "a." + this.getColumn();
	}

}
