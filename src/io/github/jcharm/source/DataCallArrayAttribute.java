/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.source;

import java.io.Serializable;
import java.lang.reflect.Array;

import io.github.jcharm.common.FieldAttribute;

/**
 * Entity类数组的FieldAttribute.
 *
 * @param <T> 声明字段的类
 * @param <F> 字段的数据类型
 */
public final class DataCallArrayAttribute<T, F> implements FieldAttribute<T[], F> {

	/** 实例对象. */
	public static final DataCallArrayAttribute INSTANCE = new DataCallArrayAttribute();

	@Override
	public Class<T[]> getDeclaringClass() {
		return (Class) Object[].class;
	}

	@Override
	public String getFieldAliasName() {
		return "";
	}

	@Override
	public String getFieldDefaultName() {
		return "";
	}

	@Override
	public F getFieldValue(final T[] objs) {
		if ((objs == null) || (objs.length == 0)) {
			return null;
		}
		final FieldAttribute<T, Serializable> attr = DataCallAttribute.load(objs[0].getClass());
		final Object keys = Array.newInstance(attr.getFieldType(), objs.length);
		for (int i = 0; i < objs.length; i++) {
			Array.set(keys, i, attr.getFieldValue(objs[i]));
		}
		return (F) keys;
	}

	@Override
	public void setFieldValue(final T[] objs, final F value) {
		if ((objs == null) || (objs.length == 0)) {
			return;
		}
		final FieldAttribute<T, Serializable> attr = DataCallAttribute.load(objs[0].getClass());
		for (int i = 0; i < objs.length; i++) {
			attr.setFieldValue(objs[i], (Serializable) Array.get(value, i));
		}
	}

	@Override
	public Class<F> getFieldType() {
		return (Class<F>) Object.class;
	}

}
