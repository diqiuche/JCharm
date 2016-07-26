/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.nio.channels.CompletionHandler;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import io.github.jcharm.common.ConstructCreator;
import io.github.jcharm.convert.annotation.ConvertColumn;
import io.github.jcharm.convert.parser.AbstractSimpleParser;
import io.github.jcharm.convert.parser.BigIntegerSimpleParser;
import io.github.jcharm.convert.parser.BooleanArraySimpleParser;
import io.github.jcharm.convert.parser.BooleanSimpleParser;
import io.github.jcharm.convert.parser.ByteArraySimpleParser;
import io.github.jcharm.convert.parser.ByteSimpleParser;
import io.github.jcharm.convert.parser.CharArraySimpleParser;
import io.github.jcharm.convert.parser.CharSequenceSimpleParser;
import io.github.jcharm.convert.parser.CharSimpleParser;
import io.github.jcharm.convert.parser.CompletionHandlerSimpleParser;
import io.github.jcharm.convert.parser.DoubleArraySimpleParser;
import io.github.jcharm.convert.parser.DoubleSimpleParser;
import io.github.jcharm.convert.parser.EnumSimpleParser;
import io.github.jcharm.convert.parser.FloatArraySimpleParser;
import io.github.jcharm.convert.parser.FloatSimpleParser;
import io.github.jcharm.convert.parser.InetAddressSimpleParser;
import io.github.jcharm.convert.parser.InetSocketAddressSimpleParser;
import io.github.jcharm.convert.parser.IntegerArraySimpleParser;
import io.github.jcharm.convert.parser.IntegerSimpleParser;
import io.github.jcharm.convert.parser.LocalDateSimpleParser;
import io.github.jcharm.convert.parser.LocalDateTimeSimpleParser;
import io.github.jcharm.convert.parser.LocalTimeSimpleParser;
import io.github.jcharm.convert.parser.LongArraySimpleParser;
import io.github.jcharm.convert.parser.LongSimpleParser;
import io.github.jcharm.convert.parser.NumberSimpleParser;
import io.github.jcharm.convert.parser.PatternSimpleParser;
import io.github.jcharm.convert.parser.ShortArraySimpleParser;
import io.github.jcharm.convert.parser.ShortSimpleParser;
import io.github.jcharm.convert.parser.StringArraySimpleParser;
import io.github.jcharm.convert.parser.StringSimpleParser;
import io.github.jcharm.convert.parser.TypeSimpleParser;
import io.github.jcharm.convert.parser.URISimpleParser;
import io.github.jcharm.convert.parser.URLSimpleParser;

/**
 * 双向序列化工厂类.
 *
 * @param <R> 反序列化输入流
 * @param <W> 序列化输出流
 */
public abstract class ConvertFactory<R extends DeserializeReader, W extends SerializeWriter> {

	private final ConvertFactory parentConvertFactory;

	protected Convert<R, W> convert;

	private final SerializeParser anySerializeParser = new AnySerializeParser(this);

	private final ConcurrentHashMap<Type, SerializeParser<W, ?>> serializeParserMap = new ConcurrentHashMap();

	private final ConcurrentHashMap<Type, DeSerializeParser<R, ?>> deSerializeParserMap = new ConcurrentHashMap();

	private final ConcurrentHashMap<Class, ConstructCreator> constructCreatorMap = new ConcurrentHashMap();

	private final ConcurrentHashMap<Field, ConvertColumnEntry> convertColumnEntryMap = new ConcurrentHashMap();

	/**
	 * 获取双向序列化工厂的父工厂类.
	 *
	 * @return ConvertFactory
	 */
	public ConvertFactory getParentConvertFactory() {
		return this.parentConvertFactory;
	}

	/**
	 * 获取双向序列化类.
	 *
	 * @return Convert
	 */
	public Convert getConvert() {
		return this.convert;
	}

	/**
	 * 获取双向序列化的方式.
	 *
	 * @return ConvertType
	 */
	public abstract ConvertType getConvertType();

	/**
	 * 创建双向序列化工厂的子工厂类.
	 *
	 * @return ConvertFactory
	 */
	public abstract ConvertFactory createChildFactory();

	/**
	 * 构造函数.
	 *
	 * @param parentConvertFactory ConvertFactory
	 */
	protected ConvertFactory(final ConvertFactory parentConvertFactory) {
		this.parentConvertFactory = parentConvertFactory;
		if (parentConvertFactory == null) {
			this.registerParser(boolean.class, BooleanSimpleParser.INSTANCE);
			this.registerParser(Boolean.class, BooleanSimpleParser.INSTANCE);
			this.registerParser(byte.class, ByteSimpleParser.INSTANCE);
			this.registerParser(Byte.class, ByteSimpleParser.INSTANCE);
			this.registerParser(char.class, CharSimpleParser.INSTANCE);
			this.registerParser(Character.class, CharSimpleParser.INSTANCE);
			this.registerParser(double.class, DoubleSimpleParser.INSTANCE);
			this.registerParser(Double.class, DoubleSimpleParser.INSTANCE);
			this.registerParser(float.class, FloatSimpleParser.INSTANCE);
			this.registerParser(Float.class, FloatSimpleParser.INSTANCE);
			this.registerParser(int.class, IntegerSimpleParser.INSTANCE);
			this.registerParser(Integer.class, IntegerSimpleParser.INSTANCE);
			this.registerParser(long.class, LongSimpleParser.INSTANCE);
			this.registerParser(Long.class, LongSimpleParser.INSTANCE);
			this.registerParser(short.class, ShortSimpleParser.INSTANCE);
			this.registerParser(Short.class, ShortSimpleParser.INSTANCE);
			this.registerParser(String.class, StringSimpleParser.INSTANCE);
			this.registerParser(BigInteger.class, BigIntegerSimpleParser.INSTANCE);
			this.registerParser(CharSequence.class, CharSequenceSimpleParser.INSTANCE);
			this.registerParser(CompletionHandler.class, CompletionHandlerSimpleParser.INSTANCE);
			this.registerParser(InetAddress.class, InetAddressSimpleParser.INSTANCE);
			this.registerParser(InetSocketAddress.class, InetSocketAddressSimpleParser.INSTANCE);
			this.registerParser(LocalDate.class, LocalDateSimpleParser.INSTANCE);
			this.registerParser(LocalDateTime.class, LocalDateTimeSimpleParser.INSTANCE);
			this.registerParser(LocalTime.class, LocalTimeSimpleParser.INSTANCE);
			this.registerParser(Number.class, NumberSimpleParser.INSTANCE);
			this.registerParser(Pattern.class, PatternSimpleParser.INSTANCE);
			this.registerParser(Type.class, TypeSimpleParser.INSTANCE);
			this.registerParser(URI.class, URISimpleParser.INSTANCE);
			this.registerParser(URL.class, URLSimpleParser.INSTANCE);
			this.registerParser(boolean[].class, BooleanArraySimpleParser.INSTANCE);
			this.registerParser(byte[].class, ByteArraySimpleParser.INSTANCE);
			this.registerParser(char[].class, CharArraySimpleParser.INSTANCE);
			this.registerParser(double[].class, DoubleArraySimpleParser.INSTANCE);
			this.registerParser(float[].class, FloatArraySimpleParser.INSTANCE);
			this.registerParser(int[].class, IntegerArraySimpleParser.INSTANCE);
			this.registerParser(long[].class, LongArraySimpleParser.INSTANCE);
			this.registerParser(short[].class, ShortArraySimpleParser.INSTANCE);
			this.registerParser(String[].class, StringArraySimpleParser.INSTANCE);
		}
	}

	/**
	 * 注册双向序列化解析器.
	 *
	 * @param <T> 双向序列化数据类型
	 * @param type 注册解析器的数据类型
	 * @param parser 注册的双向序列化解析器
	 */
	public <T> void registerParser(final Type type, final AbstractSimpleParser<R, W, T> parser) {
		this.serializeParserMap.put(type, parser);
		this.deSerializeParserMap.put(type, parser);
	}

	/**
	 * 注册序列化解析器.
	 *
	 * @param <T> 序列化数据类型
	 * @param type 注册解析器的数据类型
	 * @param serializeParser 注册的序列化解析器
	 */
	public <T> void registerSerializeParser(final Type type, final SerializeParser<W, T> serializeParser) {
		this.serializeParserMap.put(type, serializeParser);
	}

	/**
	 * 注册反序列化解析器.
	 *
	 * @param <T> 反序列化数据类型
	 * @param type 注册解析器的数据类型
	 * @param deSerializeParser 注册的反序列化解析器
	 */
	public <T> void registerDeSerializeParser(final Type type, final DeSerializeParser<R, T> deSerializeParser) {
		this.deSerializeParserMap.put(type, deSerializeParser);
	}

	/**
	 * 注册指定对象中的指定字段名的简单配置.
	 *
	 * @param clazz 声明字段类
	 * @param column 字段名称
	 * @param convertColumnEntry ConvertColumnEntry
	 */
	public void registerConvertColumn(final Class clazz, final String column, final ConvertColumnEntry convertColumnEntry) {
		if ((clazz == null) || (column == null) || (convertColumnEntry == null)) {
			return;
		}
		try {
			final Field field = clazz.getDeclaredField(column);
			String get = "get";
			if ((field.getType() == boolean.class) || (field.getType() == Boolean.class)) {
				get = "is";
			}
			final char[] cols = column.toCharArray();
			cols[0] = Character.toUpperCase(cols[0]);
			final String col2 = new String(cols);
			final Method getMethod = clazz.getMethod(get + col2);
			final Method setMethod = clazz.getMethod("set" + col2, field.getType());
			if ((getMethod != null) || (setMethod != null)) {
				this.convertColumnEntryMap.put(field, convertColumnEntry);
			}
		} catch (final Exception e) {
			return;
		}
	}

	/**
	 * 注册指定对象中的指定字段名的简单配置.
	 *
	 * @param clazz 声明字段的类
	 * @param ignore 是否忽略进行双向序列化
	 * @param columns 字段名称数组
	 */
	public void registerConvertColumn(final Class clazz, final boolean ignore, final String... columns) {
		for (final String column : columns) {
			this.registerConvertColumn(clazz, column, new ConvertColumnEntry(column, ignore));
		}
	}

	/**
	 * 注册指定对象中的指定字段名的简单配置.
	 *
	 * @param clazz 声明字段的类
	 * @param column 字段名称
	 * @param columnAlias 字段别名
	 */
	public void registerConvertColumn(final Class clazz, final String column, final String columnAlias) {
		this.registerConvertColumn(clazz, column, new ConvertColumnEntry(columnAlias));
	}

	/**
	 * 重新加载Parser使之覆盖旧Factory的配置.
	 *
	 * @param type Type
	 */
	public void reloadParser(final Type type) {
		Class clazz;
		if (type instanceof ParameterizedType) {
			final ParameterizedType pts = (ParameterizedType) type;
			clazz = (Class) (pts).getRawType();
		} else if (type instanceof Class) {
			clazz = (Class) type;
		} else {
			throw new ConvertException("not support the type (" + type + ")");
		}
		this.registerSerializeParser(type, this.createSerializeParser(type, clazz));
		this.registerDeSerializeParser(type, this.createDeSerializeParser(type, clazz));
	}

	/**
	 * 获取AnySerializeParser序列化解析器.
	 *
	 * @param <T> 序列化数据类型
	 * @return SerializeParser
	 */
	public <T> SerializeParser<W, T> getAnySerializeParser() {
		return this.anySerializeParser;
	}

	private <T> ConstructCreator<T> findConstructCreator(final Class<T> clazz) {
		final ConstructCreator constructCreator = this.constructCreatorMap.get(clazz);
		if (constructCreator != null) {
			return constructCreator;
		}
		return this.parentConvertFactory == null ? null : this.parentConvertFactory.findConstructCreator(clazz);
	}

	/**
	 * 根据Class加载动态映射实现.
	 *
	 * @param <T> 构建对象的数据类型
	 * @param clazz Class
	 * @return ConstructCreator
	 */
	public <T> ConstructCreator<T> loadConstructCreator(final Class<T> clazz) {
		ConstructCreator constructCreator = this.findConstructCreator(clazz);
		if (constructCreator == null) {
			constructCreator = ConstructCreator.create(clazz);
			if (constructCreator != null) {
				this.constructCreatorMap.put(clazz, constructCreator);
			}
		}
		return constructCreator;
	}

	private <T> SerializeParser<W, T> findSerializeParser(final Type type) {
		final SerializeParser<W, T> serializeParser = (SerializeParser<W, T>) this.serializeParserMap.get(type);
		if (serializeParser != null) {
			return serializeParser;
		}
		return this.parentConvertFactory == null ? null : this.parentConvertFactory.findSerializeParser(type);
	}

	private <T> SerializeParser<W, T> createSerializeParser(final Type type, final Class clazz) {
		SerializeParser<W, T> serializeParser = null;
		ObjectSerializeParser objectSerializeParser = null;
		if (clazz.isEnum()) {
			serializeParser = new EnumSimpleParser(clazz);
		} else if (clazz.isArray()) {
			serializeParser = new ArraySerializeParser(this, type);
		} else if (Collection.class.isAssignableFrom(clazz)) {
			serializeParser = new CollectionSerializeParser(this, type);
		} else if (Map.class.isAssignableFrom(clazz)) {
			serializeParser = new MapSerializeParser(this, type);
		} else if (clazz == Object.class) {
			return this.anySerializeParser;
		} else if (!clazz.getName().startsWith("java.")) {
			objectSerializeParser = new ObjectSerializeParser(type);
			serializeParser = objectSerializeParser;
		}
		if (serializeParser == null) {
			throw new ConvertException("not support the type (" + type + ")");
		}
		this.registerSerializeParser(type, serializeParser);
		if (objectSerializeParser != null) {
			objectSerializeParser.init(this);
		}
		return serializeParser;
	}

	/**
	 * 根据type加载序列化解析器.
	 *
	 * @param <T> 序列化数据类型
	 * @param type 注册解析器的数据类型
	 * @return SerializeParser
	 */
	public <T> SerializeParser<W, T> loadSerializeParser(final Type type) {
		SerializeParser<W, T> serializeParser = this.findSerializeParser(type);
		if (serializeParser != null) {
			return serializeParser;
		}
		if (type instanceof GenericArrayType) {
			return new ArraySerializeParser(this, type);
		}
		Class clazz;
		if (type instanceof ParameterizedType) {
			final ParameterizedType pts = (ParameterizedType) type;
			clazz = (Class) (pts).getRawType();
		} else if (type instanceof TypeVariable) {
			final TypeVariable tv = (TypeVariable) type;
			Type t = Object.class;
			if (tv.getBounds().length == 1) {
				t = tv.getBounds()[0];
			}
			if (!(t instanceof Class)) {
				t = Object.class;
			}
			clazz = (Class) t;
		} else if (type instanceof Class) {
			clazz = (Class) type;
		} else {
			throw new ConvertException("not support the type (" + type + ")");
		}
		serializeParser = this.findSerializeParser(clazz);
		if (serializeParser != null) {
			return serializeParser;
		}
		return this.createSerializeParser(type, clazz);
	}

	private <T> DeSerializeParser<R, T> findDeSerializeParser(final Type type) {
		final DeSerializeParser deSerializeParser = this.deSerializeParserMap.get(type);
		if (deSerializeParser != null) {
			return deSerializeParser;
		}
		return this.parentConvertFactory == null ? null : this.parentConvertFactory.findDeSerializeParser(type);
	}

	private <T> DeSerializeParser<R, T> createDeSerializeParser(final Type type, final Class clazz) {
		DeSerializeParser deSerializeParser = null;
		ObjectDeSerializeParser objectDeSerializeParser = null;
		if (clazz.isEnum()) {
			deSerializeParser = new EnumSimpleParser(clazz);
		} else if (clazz.isArray()) {
			deSerializeParser = new ArrayDeSerializeParser(this, type);
		} else if (Collection.class.isAssignableFrom(clazz)) {
			deSerializeParser = new CollectionDeSerializeParser(this, type);
		} else if (Map.class.isAssignableFrom(clazz)) {
			deSerializeParser = new MapDeSerializeParser(this, type);
		} else if (clazz == Object.class) {
			objectDeSerializeParser = new ObjectDeSerializeParser(type);
			deSerializeParser = objectDeSerializeParser;
		} else if (!clazz.getName().startsWith("java.")) {
			objectDeSerializeParser = new ObjectDeSerializeParser(type);
			deSerializeParser = objectDeSerializeParser;
		}
		if (deSerializeParser == null) {
			throw new ConvertException("not support the type (" + type + ")");
		}
		this.registerDeSerializeParser(type, deSerializeParser);
		if (objectDeSerializeParser != null) {
			objectDeSerializeParser.init(this);
		}
		return deSerializeParser;
	}

	/**
	 * 根据type加载反序列化解析器.
	 *
	 * @param <T> 反序列化数据类型
	 * @param type 注册解析器的数据类型
	 * @return DeSerializeParser
	 */
	public <T> DeSerializeParser<R, T> loadDeSerializeParser(final Type type) {
		DeSerializeParser deSerializeParser = this.findDeSerializeParser(type);
		if (deSerializeParser != null) {
			return deSerializeParser;
		}
		if (type instanceof GenericArrayType) {
			return new ArrayDeSerializeParser(this, type);
		}
		Class clazz;
		if (type instanceof ParameterizedType) {
			final ParameterizedType pts = (ParameterizedType) type;
			clazz = (Class) (pts).getRawType();
		} else if (type instanceof TypeVariable) {
			final TypeVariable tv = (TypeVariable) type;
			Class cz = tv.getBounds().length == 0 ? Object.class : null;
			for (final Type f : tv.getBounds()) {
				if (f instanceof Class) {
					cz = (Class) f;
					break;
				}
			}
			clazz = cz;
			if (cz == null) {
				throw new ConvertException("not support the type (" + type + ")");
			}
		} else if (type instanceof WildcardType) {
			final WildcardType wt = (WildcardType) type;
			Class cz = null;
			for (final Type f : wt.getUpperBounds()) {
				if (f instanceof Class) {
					cz = (Class) f;
					break;
				}
			}
			clazz = cz;
			if (cz == null) {
				throw new ConvertException("not support the type (" + type + ")");
			}
		} else if (type instanceof Class) {
			clazz = (Class) type;
		} else {
			throw new ConvertException("not support the type (" + type + ")");
		}
		deSerializeParser = this.findDeSerializeParser(clazz);
		if (deSerializeParser != null) {
			return deSerializeParser;
		}
		return this.createDeSerializeParser(type, clazz);
	}

	/**
	 * 根据Field加载其对应ConvertColumn注解的实体对象.
	 *
	 * @param field Field
	 * @return ConvertColumnEntry
	 */
	public ConvertColumnEntry loadConvertColumnEntry(final Field field) {
		if (field == null) {
			return null;
		}
		final ConvertColumnEntry convertColumnEntry = this.convertColumnEntryMap.get(field);
		if (convertColumnEntry != null) {
			return convertColumnEntry;
		}
		final ConvertType ct = this.getConvertType();
		final ConvertColumn[] ccs = field.getAnnotationsByType(ConvertColumn.class);
		if ((ccs != null) && (ccs.length > 0)) {
			final ConvertColumn cc = ccs[0];
			if (cc.type().contains(ct)) {
				final ConvertColumnEntry entry = new ConvertColumnEntry(cc);
				this.convertColumnEntryMap.put(field, entry);
				return entry;
			}
		}
		return null;
	}

}
