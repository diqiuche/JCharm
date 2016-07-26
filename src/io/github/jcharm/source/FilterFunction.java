/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.source;

/**
 * 过滤数据时使用的统计函数.
 */
public enum FilterFunction {

	/** 平均. */
	AVG,

	/** 汇总. */
	COUNT,

	/** 非重复汇总. */
	DISTINCTCOUNT,

	/** 最大. */
	MAX,

	/** 最小. */
	MIN,

	/** 累加和. */
	SUM;

	/**
	 * 获取统计函数内容.
	 *
	 * @param col 列名
	 * @return String
	 */
	public String getColumn(final String col) {
		if (this == DISTINCTCOUNT) {
			return "COUNT(DISTINCT " + col + ")";
		}
		return this.name() + "(" + col + ")";
	}

}
