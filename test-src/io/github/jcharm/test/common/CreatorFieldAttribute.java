/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.test.common;

import io.github.jcharm.common.FieldAttribute;

/**
 * FieldAttribute通过ASM映射产生的示例类.
 */
public final class CreatorFieldAttribute implements FieldAttribute<SimpleBean, Double> {

	@Override
	public Class<SimpleBean> getDeclaringClass() {
		return SimpleBean.class;
	}

	@Override
	public String getFieldDefaultName() {
		return "testDouble";
	}

	@Override
	public String getFieldAliasName() {
		return "testDouble";
	}

	@Override
	public Double getFieldValue(final SimpleBean obj) {
		return Double.valueOf(obj.getTestDouble());
	}

	@Override
	public void setFieldValue(final SimpleBean obj, final Double value) {
		obj.setTestDouble(value.doubleValue());
	}

	@Override
	public Class<Double> getFieldType() {
		return Double.class;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{getDeclaringClass=" + this.getDeclaringClass() + ", getFieldDefaultName=" + this.getFieldDefaultName() + ", getFieldAliasName=" + this.getFieldAliasName() + ", getFieldType=" + this.getFieldType() + "}";
	}

}
