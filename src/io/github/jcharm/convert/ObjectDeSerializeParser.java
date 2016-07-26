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

import io.github.jcharm.common.ConstructCreator;
import io.github.jcharm.common.FieldAttribute;
import io.github.jcharm.common.GenericsType;

/**
 * 对象反序列化解析器.
 *
 * @param <R> 反序列化输入流
 * @param <T> 反序列化数据类型
 */
public final class ObjectDeSerializeParser<R extends DeserializeReader, T> implements DeSerializeParser<R, T> {

	/** Type类型. */
	protected final Type type;

	/** 双向序列化工厂. */
	protected ConvertFactory convertFactory;

	/** Type类型Class. */
	protected final Class typeClass;

	/** AMS动态映射类构造方法. */
	protected ConstructCreator<T> constructCreator;

	/** 反序列化成员. */
	protected DeSerializeMember<R, T, ?>[] deSerializeMembers;

	/** 反序列化构造函数参数成员. */
	protected DeSerializeMember<R, T, ?>[] constructMembers;

	private boolean inited = false;

	private final Object lock = new Object();

	/**
	 * 构造函数.
	 *
	 * @param type Type
	 */
	protected ObjectDeSerializeParser(final Type type) {
		this.type = ((type instanceof Class) && ((Class) type).isInterface()) ? Object.class : type;
		if (type instanceof ParameterizedType) {
			final ParameterizedType parameterizedType = (ParameterizedType) type;
			this.typeClass = (Class) parameterizedType.getRawType();
		} else {
			this.typeClass = (Class) type;
		}
		this.deSerializeMembers = new DeSerializeMember[0];
	}

	/**
	 * 初始化反序列化解析器.
	 *
	 * @param convertFactory ConvertFactory
	 */
	public void init(final ConvertFactory convertFactory) {
		this.convertFactory = convertFactory;
		try {
			if (this.type == Object.class) {
				return;
			}
			Class clazz = null;
			if (this.type instanceof ParameterizedType) {
				final ParameterizedType parameterizedType = (ParameterizedType) this.type;
				clazz = (Class) (parameterizedType).getRawType();
			} else if (!(this.type instanceof Class)) {
				throw new ConvertException("[" + this.type + "] is no a class");
			} else {
				clazz = (Class) this.type;
			}
			this.constructCreator = convertFactory.loadConstructCreator(clazz);
			final String[] paramNames = this.findConstructorParameters(this.constructCreator);
			final Set<DeSerializeMember> set = new HashSet();
			ConvertColumnEntry convertColumnEntry;
			for (final Field field : clazz.getDeclaredFields()) {
				convertColumnEntry = convertFactory.loadConvertColumnEntry(field);
				if ((convertColumnEntry != null) && convertColumnEntry.isIgnore()) {
					continue;
				}
				final Method setMethod = this.getSetMethod(clazz, field);
				if (setMethod == null) {
					continue;
				}
				final Type t = this.createClassType(field.getGenericType(), this.type);
				final FieldAttribute fieldAttribute = this.createFieldAttribute(convertFactory, clazz, field, setMethod);
				set.add(new DeSerializeMember<>(fieldAttribute, convertFactory.loadDeSerializeParser(t)));
			}
			this.deSerializeMembers = set.toArray(new DeSerializeMember[set.size()]);
			Arrays.sort(this.deSerializeMembers);
			if (paramNames != null) {
				final String[] fieldNames = paramNames;
				final DeSerializeMember<R, T, ?>[] deSerializeMembers = new DeSerializeMember[fieldNames.length];
				for (int i = 0; i < fieldNames.length; i++) {
					for (final DeSerializeMember member : this.deSerializeMembers) {
						if (member.getFieldAttribute().getFieldDefaultName().equals(fieldNames[i])) {
							deSerializeMembers[i] = member;
							break;
						}
					}
				}
				this.constructMembers = deSerializeMembers;
			}
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
	public final T convertFrom(final R in) {
		try {
			final String clazz = in.readObjectBegin(this.typeClass);
			if (clazz == null) {
				return null;
			}
			if (!clazz.isEmpty()) {
				return (T) this.convertFactory.loadDeSerializeParser(Class.forName(clazz)).convertFrom(in);
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
			if (this.constructMembers == null) { // 无参构造函数
				final T result = this.constructCreator.construct();
				while (in.hasNext()) {
					final DeSerializeMember member = in.readFieldName(this.deSerializeMembers);
					in.readBlank();
					if (member == null) {
						in.skipValue(); // 跳过不存在的属性的值
					} else {
						member.read(in, result);
					}
				}
				in.readObjectEnd(this.typeClass);
				return result;
			} else {// 带参数构造函数
				final DeSerializeMember<R, T, ?>[] constructMembers = this.constructMembers;
				final Object[] constructorParams = new Object[constructMembers.length];
				final Object[][] otherParams = new Object[this.deSerializeMembers.length][2];
				int oc = 0;
				while (in.hasNext()) {
					final DeSerializeMember<R, T, ?> member = in.readFieldName(this.deSerializeMembers);
					in.readBlank();
					if (member == null) {
						in.skipValue(); // 跳过不存在的属性的值
					} else {
						final Object val = member.read(in);
						boolean flag = true;
						for (int i = 0; i < constructMembers.length; i++) {
							if (member == constructMembers[i]) {
								constructorParams[i] = val;
								flag = false;
								break;
							}
						}
						if (flag) {
							otherParams[oc++] = new Object[] { member.getFieldAttribute(), val };
						}
					}
				}
				in.readObjectEnd(this.typeClass);
				final T result = this.constructCreator.construct(constructorParams);
				for (int i = 0; i < oc; i++) {
					((FieldAttribute) otherParams[i][0]).setFieldValue(result, otherParams[i][1]);
				}
				return result;
			}
		} catch (final Exception e) {
			throw new ConvertException(e);
		}
	}

	@Override
	public final Type getType() {
		return this.type;
	}

	@Override
	public String toString() {
		return "ObjectDeSerializeParser{" + "type=" + this.type + ", members=" + Arrays.toString(this.deSerializeMembers) + '}';
	}

	private String[] findConstructorParameters(final ConstructCreator constructCreator) {
		try {
			final ConstructCreator.ConstructorParameters constructorParameters = constructCreator.getClass().getMethod("constructCreator", Object[].class).getAnnotation(ConstructCreator.ConstructorParameters.class);
			return constructorParameters == null ? null : constructorParameters.value();
		} catch (final Exception e) {
			return null;
		}
	}

	private Method getSetMethod(final Class clazz, final Field field) {
		try {
			final char[] fnChars = field.getName().toCharArray();
			fnChars[0] = Character.toUpperCase(fnChars[0]);
			final String fuFieldName = new String(fnChars);
			final String setPrefix = "set";
			final String getMethodName = setPrefix + fuFieldName;
			return clazz.getMethod(getMethodName, field.getType());
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

	private FieldAttribute createFieldAttribute(final ConvertFactory convertFactory, final Class clazz, final Field field, final Method setMethod) {
		String fieldAlias;
		final ConvertColumnEntry convertColumnEntry = convertFactory.loadConvertColumnEntry(field);
		fieldAlias = (convertColumnEntry == null) || convertColumnEntry.getName().isEmpty() ? field.getName() : convertColumnEntry.getName();
		return FieldAttribute.create(clazz, fieldAlias, field, null, setMethod);
	}

}
