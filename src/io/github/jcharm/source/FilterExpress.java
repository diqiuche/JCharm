/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.source;

/**
 * 过滤数据时使用的表达式.
 */
public enum FilterExpress {

	/** 等于. */
	EQUAL("="),

	/** 不等于. */
	NOTEQUAL("<>"),

	/** 大于. */
	GREATERTHAN(">"),

	/** 小于. */
	LESSTHAN("<"),

	/** 大于等于. */
	GREATERTHANOREQUALTO(">="),

	/** 小于等于. */
	LESSTHANOREQUALTO("<="),

	/** 是否字符串是以XX开头. */
	STARTSWITH("LIKE"),

	/** 是否字符串不是以XX开头. */
	NOTSTARTSWITH("NOT LIKE"),

	/** 是否字符串是以XX结尾. */
	ENDSWITH("LIKE"),

	/** 是否字符串不是以XX结尾. */
	NOTENDSWITH("NOT LIKE"),

	/** LIKE. */
	LIKE("LIKE"),

	/** NOT LIKE. */
	NOTLIKE("NOT LIKE"),

	/** 不区分大小写的LIKE. */
	IGNORECASELIKE("LIKE"),

	/** 不区分大小写的NOT LIKE. */
	IGNORECASENOTLIKE("NOT LIKE"),

	/** 包含. */
	CONTAIN("CONTAIN"),

	/** 不包含. */
	NOTCONTAIN("NOT CONTAIN"),

	/** 不区分大小写的包含. */
	IGNORECASECONTAIN("CONTAIN"),

	/** 不区分大小写的不包含. */
	IGNORECASENOTCONTAIN("NOT CONTAIN"),

	/** BETWEEN. */
	BETWEEN("BETWEEN"),

	/** NOT BETWEEN. */
	NOTBETWEEN("NOT BETWEEN"),

	/** IN. */
	IN("IN"),

	/** NOT IN. */
	NOTIN("NOT IN"),

	/** IS NULL. */
	ISNULL("IS NULL"),

	/** IS NOT NULL. */
	ISNOTNULL("IS NOT NULL"),

	/** 与运算 > 0. */
	OPAND("&"),

	/** 或运算 > 0. */
	OPOR("|"),

	/** 与运算 == 0. */
	OPANDNO("&"),

	/** 取模运算, 需要与FilterComplex配合使用. */
	FV_MOD("%"),

	/** 整除运算，需要与FilterComplex配合使用. */
	FV_DIV("DIV");

	private final String value;

	private FilterExpress(final String v) {
		this.value = v;
	}

	/**
	 * 获取表达式内容.
	 *
	 * @return String
	 */
	public String value() {
		return this.value;
	}

}
