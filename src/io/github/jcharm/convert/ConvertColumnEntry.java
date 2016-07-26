/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert;

import io.github.jcharm.convert.annotation.ConvertColumn;

/**
 * ConvertColumn对应的实体类.
 */
public final class ConvertColumnEntry {

	private String name;

	private boolean ignore;

	private ConvertType convertType;

	/**
	 * 构造函数.
	 */
	public ConvertColumnEntry() {
	}

	/**
	 * 构造函数.
	 *
	 * @param column ConvertColumn
	 */
	public ConvertColumnEntry(final ConvertColumn column) {
		if (column == null) {
			return;
		}
		this.name = column.name();
		this.ignore = column.ignore();
		this.convertType = column.type();
	}

	/**
	 * 构造函数.
	 *
	 * @param name String
	 */
	public ConvertColumnEntry(final String name) {
		this(name, false);
	}

	/**
	 * 构造函数.
	 *
	 * @param name String
	 * @param ignore boolean
	 */
	public ConvertColumnEntry(final String name, final boolean ignore) {
		this.name = name;
		this.ignore = ignore;
		this.convertType = ConvertType.ALL;
	}

	/**
	 * 构造函数.
	 *
	 * @param name String
	 * @param ignore boolean
	 * @param convertType ConvertType
	 */
	public ConvertColumnEntry(final String name, final boolean ignore, final ConvertType convertType) {
		this.name = name;
		this.ignore = ignore;
		this.convertType = convertType;
	}

	/**
	 * 获取字段别名.
	 *
	 * @return String
	 */
	public String getName() {
		return this.name == null ? "" : this.name;
	}

	/**
	 * 设置字段别名.
	 *
	 * @param name String
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * 获取双向序列化是否忽略字段.
	 *
	 * @return boolean
	 */
	public boolean isIgnore() {
		return this.ignore;
	}

	/**
	 * 设置双向序列化是否忽略字段.
	 *
	 * @param ignore boolean
	 */
	public void setIgnore(final boolean ignore) {
		this.ignore = ignore;
	}

	/**
	 * 获取在哪种双向序列化方式下ignore的值起作用.
	 *
	 * @return ConvertType
	 */
	public ConvertType getConvertType() {
		return this.convertType == null ? ConvertType.ALL : this.convertType;
	}

	/**
	 * 设置在哪种双向序列化方式下ignore的值起作用.
	 *
	 * @param convertType ConvertType
	 */
	public void setConvertType(final ConvertType convertType) {
		this.convertType = convertType;
	};

}
