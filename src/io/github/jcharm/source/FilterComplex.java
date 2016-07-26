/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.source;

import java.io.Serializable;

/**
 * FilterComplex主要用于复杂的表达式, 例如: col/10 = 3 、MOD(col, 8)>0这些都不是单独一个数值能表达的，因此需要FilterOperation才构建 8 、 > 、0 组合值.
 */
public class FilterComplex implements Serializable {

	private static final long serialVersionUID = 1L;

	private Number optvalue;

	private FilterExpress express;

	private Number destvalue;

	/**
	 * 构造函数.
	 */
	public FilterComplex() {
	}

	/**
	 * 构造函数.
	 *
	 * @param optvalue Number
	 * @param destvalue Number
	 */
	public FilterComplex(final Number optvalue, final Number destvalue) {
		this(optvalue, FilterExpress.EQUAL, destvalue);
	}

	/**
	 * 构造函数.
	 *
	 * @param optvalue Number
	 * @param express FilterExpress
	 */
	public FilterComplex(final Number optvalue, final FilterExpress express) {
		this(optvalue, express, 0);
	}

	/**
	 * 构造函数.
	 *
	 * @param optvalue Number
	 * @param express FilterExpress
	 * @param destvalue Number
	 */
	public FilterComplex(final Number optvalue, final FilterExpress express, final Number destvalue) {
		this.optvalue = optvalue;
		this.express = express;
		this.destvalue = destvalue;
	}

	/**
	 * 获取表达式选择值.
	 *
	 * @return Number
	 */
	public Number getOptvalue() {
		return this.optvalue == null ? 0 : this.optvalue;
	}

	/**
	 * 设置表达式选择值.
	 *
	 * @param optvalue Number
	 */
	public void setOptvalue(final Number optvalue) {
		this.optvalue = optvalue;
	}

	/**
	 * 获取表达式.
	 *
	 * @return FilterExpress
	 */
	public FilterExpress getExpress() {
		return this.express == null ? FilterExpress.EQUAL : this.express;
	}

	/**
	 * 设置表达式.
	 *
	 * @param express FilterExpress
	 */
	public void setExpress(final FilterExpress express) {
		this.express = express;
	}

	/**
	 * 获取表达式目标值.
	 *
	 * @return Number
	 */
	public Number getDestvalue() {
		return this.destvalue == null ? 0 : this.destvalue;
	}

	/**
	 * 设置表达式目标值.
	 *
	 * @param destvalue Number
	 */
	public void setDestvalue(final Number destvalue) {
		this.destvalue = destvalue;
	}

	@Override
	public String toString() {
		return FilterComplex.class.getSimpleName() + "[optvalue=" + this.getOptvalue() + ", express=" + this.getExpress() + ", destvalue=" + this.getDestvalue() + "]";
	}

}
