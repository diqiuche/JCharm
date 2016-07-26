/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.source;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

import io.github.jcharm.common.FieldAttribute;
import io.github.jcharm.source.annotation.EntityId;

/**
 * Entity类FieldAttribute.
 */
public final class DataCallAttribute implements FieldAttribute<Object, Serializable> {

	/** 实例对象. */
	public static final DataCallAttribute INSTANCE = new DataCallAttribute();

	private static final ConcurrentHashMap<Class, FieldAttribute> fieldAttributeMap = new ConcurrentHashMap();

	/**
	 * 获取指定Entity类中标识数据库表主键的字段对应的FieldAttribute.
	 *
	 * @param <T> 声明字段的类
	 * @param clazz Entity类的Class
	 * @return FieldAttribute
	 */
	static <T> FieldAttribute<T, Serializable> load(final Class clazz) {
		FieldAttribute rs = DataCallAttribute.fieldAttributeMap.get(clazz);
		if (rs != null) {
			return rs;
		}
		synchronized (DataCallAttribute.fieldAttributeMap) {
			rs = DataCallAttribute.fieldAttributeMap.get(clazz);
			if (rs == null) {
				Class cltmp = clazz;
				do {
					for (final Field field : cltmp.getDeclaredFields()) {
						if (field.getAnnotation(EntityId.class) == null) {
							continue;
						}
						try {
							rs = FieldAttribute.create(cltmp, field);
							DataCallAttribute.fieldAttributeMap.put(clazz, rs);
							return rs;
						} catch (final RuntimeException e) {
						}
					}
				} while ((cltmp = cltmp.getSuperclass()) != Object.class);
			}
			return rs;
		}
	}

	@Override
	public Class<Object> getDeclaringClass() {
		return Object.class;
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
	public Serializable getFieldValue(final Object obj) {
		if (obj == null) {
			return null;
		}
		return DataCallAttribute.load(obj.getClass()).getFieldValue(obj);
	}

	@Override
	public void setFieldValue(final Object obj, final Serializable value) {
		if (obj == null) {
			return;
		}
		DataCallAttribute.load(obj.getClass()).setFieldValue(obj, value);
	}

	@Override
	public Class<Serializable> getFieldType() {
		return Serializable.class;
	}

}
