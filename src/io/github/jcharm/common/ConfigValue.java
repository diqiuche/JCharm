/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.common;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * 该类提供类似JSONObject的数据结构, 主要用于读取xml配置文件和http-header存储.
 */
public abstract class ConfigValue {

	/**
	 * 获取字符串类型Entry.
	 *
	 * @return Entry
	 */
	public abstract Entry<String>[] getStringEntrys();

	/**
	 * 获取ConfigValue类型Entry.
	 *
	 * @return Entry
	 */
	public abstract Entry<ConfigValue>[] getConfigEntrys();

	/**
	 * 获取名称集合.
	 *
	 * @return String[]
	 */
	public abstract String[] getNames();

	/**
	 * 根据名称获取值集合.
	 *
	 * @param name String
	 * @return String[]
	 */
	public abstract String[] getValues(String name);

	/**
	 * 根据名称获取值集合.
	 *
	 * @param names String
	 * @return String[]
	 */
	public abstract String[] getValues(String... names);

	/**
	 * 根据名称获取ConfigValue集合.
	 *
	 * @param name String
	 * @return ConfigValue[]
	 */
	public abstract ConfigValue[] getConfigValues(String name);

	/**
	 * 根据名称获取ConfigValue集合.
	 *
	 * @param names String
	 * @return ConfigValue[]
	 */
	public abstract ConfigValue[] getConfigValues(String... names);

	/**
	 * 根据名称获取ConfigValue.
	 *
	 * @param name String
	 * @return ConfigValue
	 */
	public abstract ConfigValue getConfigValue(String name);

	/**
	 * 根据name获取值.
	 *
	 * @param name String
	 * @return String
	 */
	public abstract String getValue(String name);

	/**
	 * 根据name获取值并转化为boolean.
	 *
	 * @param name String
	 * @return boolean
	 */
	public boolean getBoolValue(final String name) {
		return Boolean.parseBoolean(this.getValue(name));
	}

	/**
	 * 根据name获取值并转化为boolean, 如果值不存在返回默认.
	 *
	 * @param name String
	 * @param defaultValue boolean
	 * @return boolean
	 */
	public boolean getBoolValue(final String name, final boolean defaultValue) {
		final String value = this.getValue(name);
		return (value == null) || (value.length() == 0) ? defaultValue : Boolean.parseBoolean(value);
	}

	/**
	 * 根据name获取值并转化为byte.
	 *
	 * @param name String
	 * @return byte
	 */
	public byte getByteValue(final String name) {
		return Byte.parseByte(this.getValue(name));
	}

	/**
	 * 根据name获取值并转化为byte, 如果值不存在返回默认.
	 *
	 * @param name String
	 * @param defaultValue byte
	 * @return byte
	 */
	public byte getByteValue(final String name, final byte defaultValue) {
		final String value = this.getValue(name);
		return (value == null) || (value.length() == 0) ? defaultValue : Byte.decode(value);
	}

	/**
	 * 根据name获取值并转化为char.
	 *
	 * @param name String
	 * @return char
	 */
	public char getCharValue(final String name) {
		return this.getValue(name).charAt(0);
	}

	/**
	 * 根据name获取值并转化为char, 如果值不存在返回默认.
	 *
	 * @param name String
	 * @param defaultValue char
	 * @return char
	 */
	public char getCharValue(final String name, final char defaultValue) {
		final String value = this.getValue(name);
		return (value == null) || (value.length() == 0) ? defaultValue : value.charAt(0);
	}

	/**
	 * 根据name获取值并转化为short.
	 *
	 * @param name String
	 * @return short
	 */
	public short getShortValue(final String name) {
		return Short.decode(this.getValue(name));
	}

	/**
	 * 根据name获取值并转化为short, 如果值不存在返回默认.
	 *
	 * @param name String
	 * @param defaultValue short
	 * @return short
	 */
	public short getShortValue(final String name, final short defaultValue) {
		final String value = this.getValue(name);
		return (value == null) || (value.length() == 0) ? defaultValue : Short.decode(value);
	}

	/**
	 * 根据name获取值并转化为int.
	 *
	 * @param name String
	 * @return int
	 */
	public int getIntValue(final String name) {
		return Integer.decode(this.getValue(name));
	}

	/**
	 * 根据name获取值并转化为int, 如果值不存在返回默认.
	 *
	 * @param name String
	 * @param defaultValue int
	 * @return int
	 */
	public int getIntValue(final String name, final int defaultValue) {
		final String value = this.getValue(name);
		return (value == null) || (value.length() == 0) ? defaultValue : Integer.decode(value);
	}

	/**
	 * 根据name获取值并转化为long.
	 *
	 * @param name String
	 * @return long
	 */
	public long getLongValue(final String name) {
		return Long.decode(this.getValue(name));
	}

	/**
	 * 根据name获取值并转化为long, 如果值不存在返回默认.
	 *
	 * @param name String
	 * @param defaultValue long
	 * @return long
	 */
	public long getLongValue(final String name, final long defaultValue) {
		final String value = this.getValue(name);
		return (value == null) || (value.length() == 0) ? defaultValue : Long.decode(value);
	}

	/**
	 * 根据name获取值并转化为float.
	 *
	 * @param name String
	 * @return float
	 */
	public float getFloatValue(final String name) {
		return Float.parseFloat(this.getValue(name));
	}

	/**
	 * 根据name获取值并转化为float, 如果值不存在返回默认.
	 *
	 * @param name String
	 * @param defaultValue float
	 * @return float
	 */
	public float getFloatValue(final String name, final float defaultValue) {
		final String value = this.getValue(name);
		return (value == null) || (value.length() == 0) ? defaultValue : Float.parseFloat(value);
	}

	/**
	 * 根据name获取值并转化为double.
	 *
	 * @param name String
	 * @return double
	 */
	public double getDoubleValue(final String name) {
		return Double.parseDouble(this.getValue(name));
	}

	/**
	 * 根据name获取值并转化为double, 如果值不存在返回默认.
	 *
	 * @param name String
	 * @param defaultValue double
	 * @return double
	 */
	public double getDoubleValue(final String name, final double defaultValue) {
		final String value = this.getValue(name);
		return (value == null) || (value.length() == 0) ? defaultValue : Double.parseDouble(value);
	}

	/**
	 * 根据name获取值, 如果值不存在返回默认.
	 *
	 * @param name String
	 * @param defaultValue String
	 * @return String
	 */
	public String getValue(final String name, final String defaultValue) {
		final String value = this.getValue(name);
		return value == null ? defaultValue : value;
	}

	/**
	 * 创建ConfigValue.
	 *
	 * @return ConfigValue
	 */
	public static ConfigValue create() {
		return new DefaultConfigValue();
	}

	/**
	 * 字符串输入对象.
	 *
	 * @param len int
	 * @return String
	 */
	public String toString(int len) {
		if (len < 0) {
			len = 0;
		}
		final char[] chars = new char[len];
		Arrays.fill(chars, ' ');
		final String space = new String(chars);
		final StringBuilder sb = new StringBuilder();
		sb.append("{\r\n");
		for (final Entry<String> en : this.getStringEntrys()) {
			sb.append(space).append("    '").append(en.name).append("': '").append(en.value).append("',\r\n");
		}
		for (final Entry<ConfigValue> en : this.getConfigEntrys()) {
			sb.append(space).append("    '").append(en.name).append("': '").append(en.value.toString(len + 4)).append("',\r\n");
		}
		sb.append(space).append('}');
		return sb.toString();
	}

	/**
	 * ConfigValue默认实现类.
	 */
	public static final class DefaultConfigValue extends ConfigValue {

		/** BiPredicate函数判断字符串相等. */
		public static final BiPredicate<String, String> EQUALS = (name1, name2) -> name1.equals(name2);

		/** BiPredicate函数判断字符串忽略大小写相等. */
		public static final BiPredicate<String, String> EQUALSIGNORE = (name1, name2) -> name1.equalsIgnoreCase(name2);

		private final BiPredicate<String, String> predicate;

		private Entry<String>[] stringValues = new Entry[0];

		private Entry<ConfigValue>[] entityValues = new Entry[0];

		/**
		 * 创建缺省DefaultConfigValue.
		 *
		 * @return DefaultConfigValue
		 */
		public static final DefaultConfigValue create() {
			return new DefaultConfigValue();
		}

		/**
		 * 创建DefaultConfigValue.
		 *
		 * @param name String
		 * @param value String
		 * @return DefaultConfigValue
		 */
		public static final DefaultConfigValue create(final String name, final String value) {
			final DefaultConfigValue conf = new DefaultConfigValue();
			conf.addValue(name, value);
			return conf;
		}

		/**
		 * 创建DefaultConfigValue.
		 *
		 * @param name String
		 * @param value ConfigValue
		 * @return DefaultConfigValue
		 */
		public static final DefaultConfigValue create(final String name, final ConfigValue value) {
			final DefaultConfigValue conf = new DefaultConfigValue();
			conf.addValue(name, value);
			return conf;
		}

		/**
		 * 构造函数.
		 */
		public DefaultConfigValue() {
			this(false);
		}

		/**
		 * 构造函数.
		 *
		 * @param ignoreCase boolean
		 */
		public DefaultConfigValue(final boolean ignoreCase) {
			this.predicate = ignoreCase ? DefaultConfigValue.EQUALSIGNORE : DefaultConfigValue.EQUALS;
		}

		/**
		 * 构造函数.
		 *
		 * @param predicate BiPredicate
		 */
		public DefaultConfigValue(final BiPredicate<String, String> predicate) {
			this.predicate = predicate;
		}

		/**
		 * 复制操作.
		 *
		 * @return DefaultConfigValue
		 */
		public DefaultConfigValue duplicate() {
			final DefaultConfigValue rs = new DefaultConfigValue(this.predicate);
			rs.stringValues = this.stringValues;
			rs.entityValues = this.entityValues;
			return rs;
		}

		/**
		 * 添加ConfigValue.
		 *
		 * @param cv ConfigValue
		 * @return DefaultConfigValue
		 */
		public DefaultConfigValue addAll(final ConfigValue cv) {
			if (cv == null) {
				return this;
			}
			if (cv instanceof DefaultConfigValue) {
				final DefaultConfigValue dcv = (DefaultConfigValue) cv;
				if (dcv.stringValues != null) {
					for (final Entry<String> en : dcv.stringValues) {
						this.addValue(en.name, en.value);
					}
				}
				if (dcv.entityValues != null) {
					for (final Entry<ConfigValue> en : dcv.entityValues) {
						this.addValue(en.name, en.value);
					}
				}
			} else {
				final Entry<String>[] strings = cv.getStringEntrys();
				if (strings != null) {
					for (final Entry<String> en : strings) {
						this.addValue(en.name, en.value);
					}
				}
				final Entry<ConfigValue>[] configs = cv.getConfigEntrys();
				if (configs != null) {
					for (final Entry<ConfigValue> en : configs) {
						this.addValue(en.name, en.value);
					}
				}
			}
			return this;
		}

		/**
		 * 设置ConfigValue,如果不存在则添加.
		 *
		 * @param cv ConfigValue
		 * @return DefaultConfigValue
		 */
		public DefaultConfigValue setAll(final ConfigValue cv) {
			if (cv == null) {
				return this;
			}
			if (cv instanceof DefaultConfigValue) {
				final DefaultConfigValue dcv = (DefaultConfigValue) cv;
				if (dcv.stringValues != null) {
					for (final Entry<String> en : dcv.stringValues) {
						this.setValue(en.name, en.value);
					}
				}
				if (dcv.entityValues != null) {
					for (final Entry<ConfigValue> en : dcv.entityValues) {
						this.setValue(en.name, en.value);
					}
				}
			} else {
				final Entry<String>[] strings = cv.getStringEntrys();
				if (strings != null) {
					for (final Entry<String> en : strings) {
						this.setValue(en.name, en.value);
					}
				}
				final Entry<ConfigValue>[] configs = cv.getConfigEntrys();
				if (configs != null) {
					for (final Entry<ConfigValue> en : configs) {
						this.setValue(en.name, en.value);
					}
				}
			}
			return this;
		}

		/**
		 * 根据name设置value, 如果不存在则添加.
		 *
		 * @param name String
		 * @param value String
		 * @return DefaultConfigValue
		 */
		public DefaultConfigValue setValue(final String name, final String value) {
			if (name == null) {
				return this;
			}
			if (this.getValue(name) == null) {
				this.addValue(name, value);
			} else {
				for (final Entry<String> en : this.stringValues) {
					if (this.predicate.test(en.name, name)) {
						en.value = value;
						return this;
					}
				}
			}
			return this;
		}

		/**
		 * 根据name设置ConfigValue, 如果不存在则添加.
		 *
		 * @param name String
		 * @param value ConfigValue
		 * @return DefaultConfigValue
		 */
		public DefaultConfigValue setValue(final String name, final ConfigValue value) {
			if (name == null) {
				return this;
			}
			if (this.getValue(name) == null) {
				this.addValue(name, value);
			} else {
				for (final Entry<ConfigValue> en : this.entityValues) {
					if (this.predicate.test(en.name, name)) {
						en.value = value;
						return this;
					}
				}
			}
			return this;
		}

		/**
		 * 添加name和value.
		 *
		 * @param name String
		 * @param value String
		 * @return DefaultConfigValue
		 */
		public DefaultConfigValue addValue(final String name, final String value) {
			if (name == null) {
				return this;
			}
			final int len = this.stringValues.length;
			final Entry[] news = new Entry[len + 1];
			System.arraycopy(this.stringValues, 0, news, 0, len);
			news[len] = new Entry(name, value);
			this.stringValues = news;
			return this;
		}

		/**
		 * 添加name和ConfigValue.
		 *
		 * @param name String
		 * @param value ConfigValue
		 * @return DefaultConfigValue
		 */
		public DefaultConfigValue addValue(final String name, final ConfigValue value) {
			if ((name == null) || (value == null)) {
				return this;
			}
			final int len = this.entityValues.length;
			final Entry[] news = new Entry[len + 1];
			System.arraycopy(this.entityValues, 0, news, 0, len);
			news[len] = new Entry(name, value);
			this.entityValues = news;
			return this;
		}

		/**
		 * 清理数据.
		 *
		 * @return DefaultConfigValue
		 */
		public DefaultConfigValue clear() {
			this.stringValues = new Entry[0];
			this.entityValues = new Entry[0];
			return this;
		}

		@Override
		public Entry<String>[] getStringEntrys() {
			return this.stringValues;
		}

		@Override
		public Entry<ConfigValue>[] getConfigEntrys() {
			return this.entityValues;
		}

		@Override
		public String[] getNames() {
			final Set<String> set = new LinkedHashSet<>();
			for (final Entry en : this.stringValues) {
				set.add(en.name);
			}
			for (final Entry en : this.entityValues) {
				set.add(en.name);
			}
			return set.toArray(new String[set.size()]);
		}

		@Override
		public String[] getValues(final String name) {
			return Entry.getValues(this.predicate, String.class, this.stringValues, name);
		}

		@Override
		public String[] getValues(final String... names) {
			return Entry.getValues(this.predicate, String.class, this.stringValues, names);
		}

		@Override
		public ConfigValue[] getConfigValues(final String name) {
			return Entry.getValues(this.predicate, ConfigValue.class, this.entityValues, name);
		}

		@Override
		public ConfigValue[] getConfigValues(final String... names) {
			return Entry.getValues(this.predicate, ConfigValue.class, this.entityValues, names);
		}

		@Override
		public ConfigValue getConfigValue(final String name) {
			for (final Entry<ConfigValue> en : this.entityValues) {
				if (this.predicate.test(en.name, name)) {
					return en.value;
				}
			}
			return null;
		}

		@Override
		public String getValue(final String name) {
			for (final Entry<String> en : this.stringValues) {
				if (this.predicate.test(en.name, name)) {
					return en.value;
				}
			}
			return null;
		}

		@Override
		public String toString() {
			return this.toString(0);
		}

	}

	/**
	 * 类似Map.Entry.
	 *
	 * @param <T> 泛型
	 */
	public static final class Entry<T> {

		/** 名称. */
		public final String name;

		/** 名称对应的值. */
		T value;

		/**
		 * 构造函数.
		 *
		 * @param name String
		 * @param value String
		 */
		public Entry(final String name, final T value) {
			this.name = name;
			this.value = value;
		}

		/**
		 * 获取Entry中的值.
		 *
		 * @return T
		 */
		public T getValue() {
			return this.value;
		}

		/**
		 * 根据名称获取值的集合.
		 *
		 * @param <T> 泛型
		 * @param comparison BiPredicate
		 * @param clazz Class
		 * @param entitys Entry
		 * @param name String
		 * @return T[]
		 */
		static <T> T[] getValues(final BiPredicate<String, String> comparison, final Class<T> clazz, final Entry<T>[] entitys, final String name) {
			int len = 0;
			for (final Entry en : entitys) {
				if (comparison.test(en.name, name)) {
					++len;
				}
			}
			if (len == 0) {
				return (T[]) Array.newInstance(clazz, len);
			}
			final T[] rs = (T[]) Array.newInstance(clazz, len);
			int i = 0;
			for (final Entry<T> en : entitys) {
				if (comparison.test(en.name, name)) {
					rs[i++] = en.value;
				}
			}
			return rs;
		}

		/**
		 * 根据名称获取值的集合.
		 *
		 * @param <T> 泛型
		 * @param comparison BiPredicate
		 * @param clazz Class
		 * @param entitys Entry
		 * @param names String
		 * @return T[]
		 */
		static <T> T[] getValues(final BiPredicate<String, String> comparison, final Class<T> clazz, final Entry<T>[] entitys, final String... names) {
			int len = 0;
			for (final Entry en : entitys) {
				for (final String name : names) {
					if (comparison.test(en.name, name)) {
						++len;
						break;
					}
				}
			}
			if (len == 0) {
				return (T[]) Array.newInstance(clazz, len);
			}
			final T[] rs = (T[]) Array.newInstance(clazz, len);
			int i = 0;
			for (final Entry<T> en : entitys) {
				for (final String name : names) {
					if (comparison.test(en.name, name)) {
						rs[i++] = en.value;
						break;
					}
				}
			}
			return rs;
		}

	}

}
