/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.convert;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.github.jcharm.common.FieldAttribute;
import io.github.jcharm.common.GenericsType;

/**
 * 对象序列化解析器.
 *
 * @param <W> 序列化输出流
 * @param <T> 序列化数据类型
 */
public final class ObjectSerializeParser<W extends SerializeWriter, T> implements SerializeParser<W, T> {

	/** Type类型. */
	protected final Type type;

	/** 双向序列化工厂类. */
	protected ConvertFactory convertFactory;

	/** Type类型Class. */
	protected final Class typeClass;

	/** 序列化成员. */
	protected SerializeMember<W, T, ?>[] serializeMembers;

	private final Object lock = new Object();

	private boolean inited = false;

	/**
	 * 构造函数.
	 *
	 * @param type Type
	 */
	protected ObjectSerializeParser(final Type type) {
		this.type = type;
		// ParameterizedType : 表示参数化类型, 如Collection<String>
		if (type instanceof ParameterizedType) {
			final ParameterizedType parameterizedType = (ParameterizedType) type;
			// getRawType : 返回Type对象, 表示声明此类型的类或接口
			this.typeClass = (Class) parameterizedType.getRawType();
		} else {
			this.typeClass = (Class) type;
		}
		this.serializeMembers = new SerializeMember[0];
	}

	/**
	 * 初始化对象序列化解析器.
	 *
	 * @param convertFactory ConvertFactory
	 */
	public void init(final ConvertFactory convertFactory) {
		this.convertFactory = convertFactory;
		try {
			if (this.type == Object.class) {
				return;
			}
			final Class clazz = this.typeClass;
			final Set<SerializeMember> set = new HashSet();
			ConvertColumnEntry convertColumnEntry;
			for (final Field field : clazz.getDeclaredFields()) {
				convertColumnEntry = convertFactory.loadConvertColumnEntry(field);
				if ((convertColumnEntry != null) && convertColumnEntry.isIgnore()) {
					continue;
				}
				final Method getMethod = this.getGetMethod(clazz, field);
				if (getMethod == null) {
					continue;
				}
				final Type t = this.createClassType(field.getGenericType(), this.type);
				final FieldAttribute fieldAttribute = this.createFieldAttribute(convertFactory, clazz, field, getMethod);
				set.add(new SerializeMember<>(fieldAttribute, convertFactory.loadSerializeParser(t)));
			}
			this.serializeMembers = set.toArray(new SerializeMember[set.size()]);
			Arrays.sort(this.serializeMembers);
		} catch (final Exception e) {
			throw new ConvertException(e);
		} finally {
			this.inited = true;
			synchronized (this.lock) {
				// notifyAll : 唤醒在此对象监视器上等待的所有线程
				this.lock.notifyAll();
			}
		}
	}

	@Override
	public final void convertTo(final W out, final T value) {
		if (value == null) {
			out.writeObjectNull();
			return;
		}
		if (!this.inited) {
			synchronized (this.lock) {
				try {
					this.lock.wait();
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}
		if ((value != null) && (value.getClass() != this.typeClass)) {
			final Class clz = value.getClass();
			if (out.needWriteClassName()) {
				out.writeClassName(clz);
			}
			this.convertFactory.loadSerializeParser(clz).convertTo(out, value);
			return;
		}
		out.writeObjectBegin(value);
		for (final SerializeMember member : this.serializeMembers) {
			out.writeObjectField(member, value);
		}
		out.writeObjectEnd(value);
	}

	@Override
	public final Type getType() {
		return this.type;
	}

	@Override
	public String toString() {
		return "ObjectSerializeParser{" + "type=" + this.type + ", members=" + Arrays.toString(this.serializeMembers) + '}';
	}

	private Method getGetMethod(final Class clazz, final Field field) {
		try {
			final Class fieldType = field.getType();
			final char[] fnChars = field.getName().toCharArray();
			fnChars[0] = Character.toUpperCase(fnChars[0]);
			final String fuFieldName = new String(fnChars);
			final String getPrefix = ((fieldType == boolean.class) || (fieldType == Boolean.class)) ? "is" : "get";
			final String getMethodName = getPrefix + fuFieldName;
			return clazz.getMethod(getMethodName);
		} catch (final Exception e) {
			return null;
		}
	}

	private Type createClassType(final Type fieldType, final Type type) {
		if (GenericsType.isClassType(fieldType)) {
			return fieldType;
		}
		if (fieldType instanceof ParameterizedType) {
			final ParameterizedType parameterizedType = (ParameterizedType) fieldType;
			final Type[] paramTypes = parameterizedType.getActualTypeArguments();
			for (int i = 0; i < paramTypes.length; i++) {
				paramTypes[i] = this.createClassType(paramTypes[i], type);
			}
			return GenericsType.createParameterizedType(parameterizedType.getOwnerType(), parameterizedType.getRawType(), paramTypes);
		}
		Type declaringType = type;
		if (declaringType instanceof Class) {
			do {
				// getGenericSuperclass : 返回表示此Class所表示的实体(类、接口、基本类型或 void)的直接超类的Type
				declaringType = ((Class) declaringType).getGenericSuperclass();
				if (declaringType == Object.class) {
					return Object.class;
				}
			} while (declaringType instanceof Class);
		}
		if (!(declaringType instanceof ParameterizedType)) {
			return Object.class;
		}
		final ParameterizedType declaringPType = (ParameterizedType) declaringType;
		// getTypeParameters : 按声明顺序返回TypeVariable对象的一个数组, 这些对象表示用此GenericDeclaration对象所表示的常规声明来声明的类型变量
		final Type[] virTypes = ((Class) declaringPType.getRawType()).getTypeParameters();
		final Type[] desTypes = declaringPType.getActualTypeArguments();
		if (fieldType instanceof WildcardType) {
			final WildcardType wt = (WildcardType) fieldType;
			// getUpperBounds : 返回表示此类型变量上边界的Type对象的数组
			for (final Type f : wt.getUpperBounds()) {
				for (int i = 0; i < virTypes.length; i++) {
					if (virTypes[i].equals(f)) {
						return desTypes.length <= i ? Object.class : desTypes[i];
					}
				}
			}
		} else if (fieldType instanceof TypeVariable) {
			for (int i = 0; i < virTypes.length; i++) {
				if (virTypes[i].equals(fieldType)) {
					return desTypes.length <= i ? Object.class : desTypes[i];
				}
			}
		}
		return fieldType;
	}

	private FieldAttribute createFieldAttribute(final ConvertFactory convertFactory, final Class clazz, final Field field, final Method getMethod) {
		String fieldAlias;
		final ConvertColumnEntry convertColumnEntry = convertFactory.loadConvertColumnEntry(field);
		fieldAlias = (convertColumnEntry == null) || convertColumnEntry.getName().isEmpty() ? field.getName() : convertColumnEntry.getName();
		return FieldAttribute.create(clazz, fieldAlias, field, getMethod, null);
	}

}
