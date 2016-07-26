/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.source;

import java.io.Serializable;

/**
 * 翻页类.
 */
public final class PageTurn implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	/** 默认长度. */
	public static long DEFAULT_LIMIT = 20;

	private long limit = PageTurn.DEFAULT_LIMIT;

	private long offset = 0;

	private String sort = "";

	/**
	 * 构造函数.
	 */
	public PageTurn() {
	}

	/**
	 * 构造函数.
	 *
	 * @param limit 长度
	 */
	public PageTurn(final long limit) {
		this.limit = limit;
	}

	/**
	 * 构造函数.
	 *
	 * @param sortColumn 排序列
	 */
	public PageTurn(final String sortColumn) {
		this.sort = sortColumn;
	}

	/**
	 * 构造函数.
	 *
	 * @param limit 长度
	 * @param offset 起始位置
	 */
	public PageTurn(final long limit, final long offset) {
		this.limit = limit > 0 ? limit : PageTurn.DEFAULT_LIMIT;
		this.offset = offset < 0 ? 0 : offset;
	}

	/**
	 * 构造函数.
	 *
	 * @param limit 长度
	 * @param offset 起始位置
	 * @param sortColumn 排序列
	 */
	public PageTurn(final long limit, final long offset, final String sortColumn) {
		this.limit = limit > 0 ? limit : PageTurn.DEFAULT_LIMIT;
		this.offset = offset < 0 ? 0 : offset;
		this.sort = sortColumn;
	}

	/**
	 * 将当前翻页对象进行拷贝.
	 *
	 * @param copy PageTurn
	 */
	public void copyTo(final PageTurn copy) {
		if (copy == null) {
			return;
		}
		copy.offset = this.offset;
		copy.limit = this.limit;
		copy.sort = this.sort;
	}

	/**
	 * 将翻页拷贝到当前对象.
	 *
	 * @param copy PageTurn
	 */
	public void copyFrom(final PageTurn copy) {
		if (copy == null) {
			return;
		}
		this.offset = copy.offset;
		this.limit = copy.limit;
		this.sort = copy.sort;
	}

	/**
	 * 翻页.
	 *
	 * @return PageTurn
	 */
	public PageTurn next() {
		this.offset = this.getOffset() + this.limit;
		return this;
	}

	@Override
	public PageTurn clone() {
		return new PageTurn(this.limit, this.offset, this.sort);
	}

	/**
	 * 获取起始位置.
	 *
	 * @return long
	 */
	public long getOffset() {
		return this.offset;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{offset:" + this.offset + ", limit:" + this.limit + ", sort:" + this.sort + "}";
	}

	/**
	 * 获取长度.
	 *
	 * @return long
	 */
	public long getLimit() {
		return this.limit;
	}

	/**
	 * 设置长度.
	 *
	 * @param limit long
	 */
	public void setLimit(final long limit) {
		if (limit > 0) {
			this.limit = limit;
		}
	}

	/**
	 * 设置起始位置.
	 *
	 * @param offset long
	 */
	public void setOffset(final long offset) {
		this.offset = offset < 0 ? 0 : offset;
	}

	/**
	 * 获取排序列.
	 *
	 * @return String
	 */
	public String getSort() {
		return this.sort;
	}

	/**
	 * 翻页对象如果不存在排序列进行设置.
	 *
	 * @param sort 排序列
	 * @return PageTurn
	 */
	public PageTurn sortIfAbsent(final String sort) {
		if ((this.sort == null) || this.sort.isEmpty()) {
			this.sort = sort;
		}
		return this;
	}

	/**
	 * 设置排序列.
	 *
	 * @param sort String
	 */
	public void setSort(final String sort) {
		if (sort != null) {
			this.sort = sort.trim();
		}
	}

}
