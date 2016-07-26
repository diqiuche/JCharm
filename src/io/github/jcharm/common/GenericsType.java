/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.common;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Objects;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.FieldVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;

/**
 * 获取泛型的Type类.
 *
 * @param <T> 泛型
 */
public abstract class GenericsType<T> {

	private final Type type;

	/**
	 * 构造函数.
	 */
	public GenericsType() {
		// getGenericSuperclass : 返回表示此Class所表示的实体(类、接口、基本类型或void)的直接超类的Type
		this.type = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}

	/**
	 * 获取泛型类型.
	 *
	 * @return Type
	 */
	public final Type getType() {
		return this.type;
	}

	/**
	 * 判断Type是否能确定最终的class, 是则返回true, 存在通配符或者不确定类型则返回false.
	 *
	 * @param type Type对象
	 * @return boolean
	 */
	public static final boolean isClassType(final Type type) {
		if (type instanceof Class) {
			return true;
		}
		// WildcardType : 表示一个通配符类型表达式, 如 ?、? extends Number 或 ? super Integer
		if (type instanceof WildcardType) {
			return false;
		}
		// TypeVariable : 是各种类型变量的公共高级接口
		if (type instanceof TypeVariable) {
			return false;
		}
		// GenericArrayType : 表示一种数组类型, 其组件类型为参数化类型或类型变量
		if (type instanceof GenericArrayType) {
			return GenericsType.isClassType(((GenericArrayType) type).getGenericComponentType());
		}
		if (!(type instanceof ParameterizedType)) {
			return false;
		}
		final ParameterizedType parameterizedType = (ParameterizedType) type;
		// getOwnerType : 返回Type对象, 表示此类型是其成员之一的类型, 例如 : 如果此类型为O<T>.I<S>, 则返回O<T> 的表示形式
		if ((parameterizedType.getOwnerType() != null) && !GenericsType.isClassType(parameterizedType.getOwnerType())) {
			return false;
		}
		// getRawType : 返回Type对象, 表示声明此类型的类或接口
		if (!GenericsType.isClassType(parameterizedType.getRawType())) {
			return false;
		}
		// getActualTypeArguments : 返回表示此类型实际类型参数的Type对象的数组
		for (final Type t : parameterizedType.getActualTypeArguments()) {
			if (!GenericsType.isClassType(t)) {
				return false;
			}
		}
		return true;
	}

	private static CharSequence getClassTypeDescriptor(final Type type) {
		if (!GenericsType.isClassType(type)) {
			throw new IllegalArgumentException(type + " not a class type");
		}
		if (type instanceof Class) {
			return jdk.internal.org.objectweb.asm.Type.getDescriptor((Class) type);
		}
		final ParameterizedType parameterizedType = (ParameterizedType) type;
		final CharSequence rawTypeDesc = GenericsType.getClassTypeDescriptor(parameterizedType.getRawType());
		final StringBuilder sb = new StringBuilder();
		sb.append(rawTypeDesc.subSequence(0, rawTypeDesc.length() - 1)).append('<');
		for (final Type c : parameterizedType.getActualTypeArguments()) {
			sb.append(GenericsType.getClassTypeDescriptor(c));
		}
		sb.append(">;");
		return sb;
	}

	private static Type createParameterizedType(final Class rawType, final Type... actualTypeArguments) {
		final ClassLoader loader = GenericsType.class.getClassLoader();
		String newDynName = GenericsType.class.getName().replace('.', '/') + "_Dyn" + System.currentTimeMillis();
		for (;;) {
			try {
				Class.forName(newDynName.replace('/', '.'));
				newDynName = GenericsType.class.getName().replace('.', '/') + "_Dyn" + Math.abs(System.nanoTime());
			} catch (final Exception e) {
				break;
			}
		}
		// =====================================ASM代码=====================================
		final ClassWriter cw = new ClassWriter(0);
		FieldVisitor fv;
		MethodVisitor mv;
		cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_SUPER, newDynName, null, "java/lang/Object", null);
		final String rawTypeDesc = jdk.internal.org.objectweb.asm.Type.getDescriptor(rawType);// 获取其在ASM中的类型描述符
		final StringBuilder sb = new StringBuilder();
		sb.append(rawTypeDesc.substring(0, rawTypeDesc.length() - 1)).append('<');
		for (final Type c : actualTypeArguments) {
			sb.append(GenericsType.getClassTypeDescriptor(c));
		}
		sb.append(">;");
		{
			// ASM编写字段定义
			fv = cw.visitField(Opcodes.ACC_PUBLIC, "field", rawTypeDesc, sb.toString(), null);
			fv.visitEnd();
		}
		{// ASM编写构造函数
			mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		cw.visitEnd();
		final byte[] bytes = cw.toByteArray();
		final Class<?> newClazz = new ClassLoader(loader) {

			public final Class<?> loadClass(final String name, final byte[] b) {
				return this.defineClass(name, b, 0, b.length);
			}
		}.loadClass(newDynName.replace('/', '.'), bytes);
		try {
			return newClazz.getField("field").getGenericType();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 动态创建ParameterizedType.
	 *
	 * @param ownerType0 ParameterizedType.getOwnerType()
	 * @param rawType0 ParameterizedType.getRawType()
	 * @param actualTypeArguments0 ParameterizedType.getActualTypeArguments()
	 * @return ParameterizedType
	 */
	public static Type createParameterizedType(final Type ownerType0, final Type rawType0, final Type... actualTypeArguments0) {
		if ((ownerType0 == null) && (rawType0 instanceof Class)) {
			int count = 0;
			for (final Type t : actualTypeArguments0) {
				if (GenericsType.isClassType(t)) {
					count++;
				}
			}
			if (count == actualTypeArguments0.length) {
				return GenericsType.createParameterizedType((Class) rawType0, actualTypeArguments0);
			}
		}
		return new ParameterizedType() {

			private final Class<?> rawType = (Class<?>) rawType0;

			private final Type ownerType = ownerType0;

			private final Type[] actualTypeArguments = actualTypeArguments0;

			@Override
			public Type getRawType() {
				return this.rawType;
			}

			@Override
			public Type getOwnerType() {
				return this.ownerType;
			}

			@Override
			public Type[] getActualTypeArguments() {
				return this.actualTypeArguments.clone();
			}

			@Override
			public int hashCode() {
				return Arrays.hashCode(this.actualTypeArguments) ^ Objects.hashCode(this.rawType) ^ Objects.hashCode(this.ownerType);
			}

			@Override
			public boolean equals(final Object obj) {
				if (!(obj instanceof ParameterizedType)) {
					return false;
				}
				final ParameterizedType that = (ParameterizedType) obj;
				if (this == that) {
					return true;
				}
				return Objects.equals(this.ownerType, that.getOwnerType()) && Objects.equals(this.rawType, that.getRawType()) && Arrays.equals(this.actualTypeArguments, that.getActualTypeArguments());
			}

			@Override
			public String toString() {
				final StringBuilder sb = new StringBuilder();
				if (this.ownerType != null) {
					sb.append((this.ownerType instanceof Class) ? (((Class) this.ownerType).getName()) : this.ownerType.toString()).append(".");
				}
				sb.append(this.rawType.getName());
				if ((this.actualTypeArguments != null) && (this.actualTypeArguments.length > 0)) {
					sb.append("<");
					boolean first = true;
					for (final Type t : this.actualTypeArguments) {
						if (!first) {
							sb.append(", ");
						}
						sb.append(t);
						first = false;
					}
					sb.append(">");
				}
				return sb.toString();
			}
		};
	}

}
