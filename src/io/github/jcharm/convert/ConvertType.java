/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert;

/**
 * 双向序列化方式的枚举类.
 */
public enum ConvertType {

	/** JSON. */
	JSON(1),
	/** BSON. */
	BSON(2),
	/** ALL. */
	ALL(127);

	private int value;

	private ConvertType(final int value) {
		this.value = value;
	}

	/**
	 * 比较枚举对象是否存在包含关系.
	 *
	 * @param convertType ConvertType
	 * @return boolean
	 */
	public boolean contains(final ConvertType convertType) {
		if (convertType == null) {
			return false;
		}
		return (this.value >= convertType.value) && ((this.value & convertType.value) > 0);
	}

}
