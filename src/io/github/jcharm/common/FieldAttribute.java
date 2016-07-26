/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.common;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;

/**
 * 通过ASM动态生成一个JavaBean类中字段对应的getter、setter方法及相关属性的映射类.
 *
 * @param <T> 声明字段的类
 * @param <F> 字段的数据类型
 */
public interface FieldAttribute<T, F> {

	/**
	 * 返回声明字段的类.
	 *
	 * @return Class
	 */
	public Class<T> getDeclaringClass();

	/**
	 * 返回字段对应的别名.
	 *
	 * @return String
	 */
	public String getFieldAliasName();

	/**
	 * 返回字段对应的默认名称.
	 *
	 * @return String
	 */
	public String getFieldDefaultName();

	/**
	 * 返回字段对应的值, 如果字段不存在getter方法, 这里返回值为null.
	 *
	 * @param obj 指定的对象
	 * @return F
	 */
	public F getFieldValue(T obj);

	/**
	 * 设置字段对应的值, 如果字段不存在setter方法, 这个方法将为空方法.
	 *
	 * @param obj 指定对象
	 * @param value 字段值
	 */
	public void setFieldValue(T obj, F value);

	/**
	 * 返回字段的数据类型.
	 * 
	 * @return Class
	 */
	public Class<? extends F> getFieldType();

	/**
	 * 获取Class指定名称的gettter的Method.
	 *
	 * @param clazz 声明字段的类
	 * @param getMethodName getter方法名称
	 * @return Method
	 */
	static Method getterMethod(final Class clazz, final String getMethodName) {
		try {
			return clazz.getMethod(getMethodName);
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * 获取Class指定名称和参数类型的setter的Method.
	 *
	 * @param clazz 声明字段的类
	 * @param setMethodName setter方法名称
	 * @param fieldType 参数类型
	 * @return Method
	 */
	static Method setterMethod(final Class clazz, final String setMethodName, final Class fieldType) {
		try {
			return clazz.getMethod(setMethodName, fieldType);
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * 获取Field的getter方法名称.
	 *
	 * @param field 字段
	 * @return String
	 */
	static String getterMethodName(final Field field) {
		final Class fieldType = field.getType();
		final char[] fnChars = field.getName().toCharArray();
		fnChars[0] = Character.toUpperCase(fnChars[0]);
		final String fuFieldName = new String(fnChars);
		final String getPrefix = ((fieldType == boolean.class) || (fieldType == Boolean.class)) ? "is" : "get";
		return getPrefix + fuFieldName;
	}

	/**
	 * 获取Field的setter方法名称.
	 *
	 * @param field 字段
	 * @return String
	 */
	static String setterMethodName(final Field field) {
		final char[] fnChars = field.getName().toCharArray();
		fnChars[0] = Character.toUpperCase(fnChars[0]);
		final String fuFieldName = new String(fnChars);
		final String setPrefix = "set";
		return setPrefix + fuFieldName;
	}

	/**
	 * 根据Class、字段别名、Field、getter、setter生成FieldAttribute.
	 *
	 * @param <T> 声明字段的类
	 * @param <F> 字段的数据类型
	 * @param clazz 声明字段的类
	 * @param fieldAlias 字段别名
	 * @param field 字段
	 * @param getter getter方法
	 * @param setter setter方法
	 * @return FieldAttribute
	 */
	public static <T, F> FieldAttribute<T, F> create(final Class<T> clazz, String fieldAlias, final Field field, final Method getter, final Method setter) {
		Class fieldType = field.getType();
		final String getterMethodName = FieldAttribute.getterMethodName(field);
		final String setterMethodName = FieldAttribute.setterMethodName(field);
		Method getterMethod = getter;
		Method setterMethod = setter;
		if (getterMethod == null) {
			getterMethod = FieldAttribute.getterMethod(clazz, getterMethodName);
		}
		if (setterMethod == null) {
			setterMethod = FieldAttribute.setterMethod(clazz, setterMethodName, fieldType);
		}
		if ((getterMethod == null) && (setterMethod == null)) {// 字段不存在getter和setter方法
			return null;
		}
		if ((fieldAlias == null) || fieldAlias.equals("")) {
			fieldAlias = field.getName();
		}
		final Class defaultFieldType = fieldType;
		if (fieldType.isPrimitive()) {// 如果字段是基本数据类型, 将其包装在对象中.
			fieldType = Array.get(Array.newInstance(fieldType, 1), 0).getClass();
		}
		final String superClassName = FieldAttribute.class.getName().replace('.', '/');
		final String declaringClassName = clazz.getName().replace('.', '/');
		final String fieldTypeClassName = fieldType.getName().replace('.', '/');
		final String declaringClassNameASM = Type.getDescriptor(clazz);// ASM中Java类型描述符
		final String fieldTypeClassNameASM = Type.getDescriptor(fieldType);
		ClassLoader classLoader = FieldAttribute.class.getClassLoader();
		String dynClassName = superClassName + "_Dyn_" + clazz.getSimpleName() + "_" + fieldAlias.substring(fieldAlias.indexOf('.') + 1) + "_" + fieldType.getSimpleName().replace("[]", "Array");
		if (String.class.getClassLoader() != clazz.getClassLoader()) {
			classLoader = clazz.getClassLoader();
			dynClassName = declaringClassName + "_Dyn_" + FieldAttribute.class.getSimpleName() + "_" + fieldAlias.substring(fieldAlias.indexOf('.') + 1) + "_" + fieldType.getSimpleName().replace("[]", "Array");
		}
		try {
			return (FieldAttribute) Class.forName(dynClassName.replace('/', '.')).newInstance();
		} catch (final Exception e) {
		}
		// ===========================================ASM代码================================================
		final ClassWriter cw = new ClassWriter(0);
		MethodVisitor mv;
		cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_SUPER, dynClassName, "Ljava/lang/Object;L" + superClassName + "<" + declaringClassNameASM + fieldTypeClassNameASM + ">;", "java/lang/Object", new String[] { superClassName });
		{
			// ASM编写构造函数
			mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			// ASM编写getDeclaringClass方法
			mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "getDeclaringClass", "()Ljava/lang/Class;", "()Ljava/lang/Class<" + declaringClassNameASM + ">;", null);
			mv.visitLdcInsn(Type.getType(declaringClassNameASM));
			mv.visitInsn(Opcodes.ARETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			// ASM编写getFieldAliasName方法
			mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "getFieldAliasName", "()Ljava/lang/String;", null, null);
			mv.visitLdcInsn(fieldAlias);
			mv.visitInsn(Opcodes.ARETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			// ASM编写getFieldDefaultName方法
			mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "getFieldDefaultName", "()Ljava/lang/String;", null, null);
			mv.visitLdcInsn(field.getName());
			mv.visitInsn(Opcodes.ARETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			// ASM编写getFieldValue方法
			mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "getFieldValue", "(" + declaringClassNameASM + ")" + fieldTypeClassNameASM, null, null);
			if (getterMethod == null) {
				mv.visitInsn(Opcodes.ACONST_NULL);
				mv.visitInsn(Opcodes.ARETURN);
				mv.visitMaxs(1, 2);
				mv.visitEnd();
			} else {
				if (defaultFieldType == fieldType) {
					mv.visitVarInsn(Opcodes.ALOAD, 1);
					mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, declaringClassName, getterMethodName, "()" + fieldTypeClassNameASM, false);
					mv.visitInsn(Opcodes.ARETURN);
					mv.visitMaxs(1, 2);
					mv.visitEnd();
				} else {
					mv.visitVarInsn(Opcodes.ALOAD, 1);
					try {
						final Method test = clazz.getDeclaredMethod(getterMethodName);
						Type.getMethodDescriptor(test);
						final Method test_2 = fieldType.getDeclaredMethod("valueOf", defaultFieldType);
						Type.getMethodDescriptor(test_2);
						mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, declaringClassName, getterMethodName, Type.getMethodDescriptor(clazz.getDeclaredMethod(getterMethodName)), false);
						mv.visitMethodInsn(Opcodes.INVOKESTATIC, fieldTypeClassName, "valueOf", Type.getMethodDescriptor(fieldType.getDeclaredMethod("valueOf", defaultFieldType)), false);
						mv.visitInsn(Opcodes.ARETURN);
						mv.visitMaxs(2, 2);
						mv.visitEnd();
					} catch (final Exception e) {
					}
				}
			}
		}
		{
			// ASM编写setFieldValue方法
			mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "setFieldValue", "(" + declaringClassNameASM + fieldTypeClassNameASM + ")V", null, null);
			if (setterMethod == null) {
				mv.visitInsn(Opcodes.RETURN);
				mv.visitMaxs(0, 3);
				mv.visitEnd();
			} else {
				if (defaultFieldType == fieldType) {
					mv.visitVarInsn(Opcodes.ALOAD, 1);
					mv.visitVarInsn(Opcodes.ALOAD, 2);
					mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, declaringClassName, setterMethodName, "(" + fieldTypeClassNameASM + ")V", false);
					mv.visitInsn(Opcodes.RETURN);
					mv.visitMaxs(2, 3);
					mv.visitEnd();
				} else {
					mv.visitVarInsn(Opcodes.ALOAD, 1);
					mv.visitVarInsn(Opcodes.ALOAD, 2);
					try {
						final Method defaultFieldTypeMothed = fieldType.getMethod(defaultFieldType.getName() + "Value");
						mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, fieldTypeClassName, defaultFieldType.getName() + "Value", Type.getMethodDescriptor(defaultFieldTypeMothed), false);
						mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, declaringClassName, setterMethodName, Type.getMethodDescriptor(setterMethod), false);
						mv.visitInsn(Opcodes.RETURN);
						mv.visitMaxs(3, 3);
						mv.visitEnd();
					} catch (final Exception e) {
					}
				}

			}
		}
		{
			// ASM编写getFieldType方法
			mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "getFieldType", "()Ljava/lang/Class;", "()Ljava/lang/Class<" + fieldTypeClassNameASM + ">;", null);
			mv.visitLdcInsn(Type.getType(fieldTypeClassNameASM));
			mv.visitInsn(Opcodes.ARETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			// ASM编写toString方法
			mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null);
			mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
			mv.visitInsn(Opcodes.DUP);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getSimpleName", "()Ljava/lang/String;", false);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;", false);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
			mv.visitLdcInsn("{getDeclaringClass=");
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, dynClassName, "getDeclaringClass", "()Ljava/lang/Class;", false);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false);
			mv.visitLdcInsn(", getFieldDefaultName=");
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, dynClassName, "getFieldDefaultName", "()Ljava/lang/String;", false);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
			mv.visitLdcInsn(", getFieldAliasName=");
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, dynClassName, "getFieldAliasName", "()Ljava/lang/String;", false);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
			mv.visitLdcInsn(", getFieldType=");
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, dynClassName, "getFieldType", "()Ljava/lang/Class;", false);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false);
			mv.visitLdcInsn("}");
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
			mv.visitInsn(Opcodes.ARETURN);
			mv.visitMaxs(3, 1);
			mv.visitEnd();
		}
		{
			// ASM编写虚拟getFieldValue方法
			mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_BRIDGE + Opcodes.ACC_SYNTHETIC, "getFieldValue", "(Ljava/lang/Object;)Ljava/lang/Object;", null, null);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitTypeInsn(Opcodes.CHECKCAST, declaringClassName);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, dynClassName, "getFieldValue", "(" + declaringClassNameASM + ")" + fieldTypeClassNameASM, false);
			mv.visitInsn(Opcodes.ARETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		{
			// ASM编写虚拟setFieldValue方法
			mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_BRIDGE + Opcodes.ACC_SYNTHETIC, "setFieldValue", "(Ljava/lang/Object;Ljava/lang/Object;)V", null, null);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitTypeInsn(Opcodes.CHECKCAST, declaringClassName);
			mv.visitVarInsn(Opcodes.ALOAD, 2);
			mv.visitTypeInsn(Opcodes.CHECKCAST, fieldTypeClassName);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, dynClassName, "setFieldValue", "(" + declaringClassNameASM + fieldTypeClassNameASM + ")V", false);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitMaxs(3, 3);
			mv.visitEnd();
		}
		cw.visitEnd();
		final byte[] bytes = cw.toByteArray();
		final Class<FieldAttribute> creatorClass = (Class<FieldAttribute>) new ClassLoader(classLoader) {

			public final Class<?> loadClass(final String name, final byte[] b) {
				return this.defineClass(name, b, 0, b.length);
			}
		}.loadClass(dynClassName.replace('/', '.'), bytes);
		try {
			return creatorClass.newInstance();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 根据Field生成FieldAttribute.
	 *
	 * @param <T> 声明字段的类
	 * @param <F> 字段数据类型
	 * @param field 字段
	 * @return FieldAttribute
	 */
	public static <T, F> FieldAttribute<T, F> create(final Field field) {
		return FieldAttribute.create((Class<T>) field.getDeclaringClass(), field.getName(), field, null, null);
	}

	/**
	 * 根据Class、Field生成FieldAttribute.
	 *
	 * @param <T> 声明字段的类
	 * @param <F> 字段的数据类型
	 * @param clazz 声明字段的类
	 * @param field 字段
	 * @return FieldAttribute
	 */
	public static <T, F> FieldAttribute<T, F> create(final Class<T> clazz, final Field field) {
		return FieldAttribute.create(clazz, field.getName(), field, null, null);
	}

	/**
	 * 根据Class和字段真实名称生成FieldAttribute.
	 *
	 * @param <T> 声明字段的类
	 * @param <F> 字段的数据类型
	 * @param clazz 声明字段的类
	 * @param fieldName 字段名称
	 * @return FieldAttribute
	 */
	public static <T, F> FieldAttribute<T, F> create(final Class<T> clazz, final String fieldName) {
		try {
			return FieldAttribute.create(clazz, fieldName, clazz.getDeclaredField(fieldName), null, null);
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * 根据字段别名、Field生成FieldAttribute.
	 *
	 * @param <T> 声明字段的类
	 * @param <F> 字段的数据类型
	 * @param fieldalias 字段别名
	 * @param field 字段
	 * @return FieldAttribute
	 */
	public static <T, F> FieldAttribute<T, F> create(final String fieldalias, final Field field) {
		return FieldAttribute.create((Class<T>) field.getDeclaringClass(), fieldalias, field, null, null);
	}

	/**
	 * 根据Class生成字段对应的FieldAttribute对象数组.
	 *
	 * @param <T> 声明字段的类
	 * @param clazz 声明字段的类
	 * @return FieldAttribute[]
	 */
	public static <T> FieldAttribute<T, ?>[] create(final Class<T> clazz) {
		final List<FieldAttribute<T, ?>> list = new ArrayList<>();
		for (final Field field : clazz.getDeclaredFields()) {
			final FieldAttribute fieldAttribute = FieldAttribute.create(clazz, field);
			if (fieldAttribute != null) {
				list.add(fieldAttribute);
			}
		}
		return list.toArray(new FieldAttribute[list.size()]);
	}

	/**
	 * 根据Class生成字段包含getter方法对应的FieldAttribute对象数组.
	 *
	 * @param <T> 声明字段的类
	 * @param clazz 声明字段的类
	 * @return FieldAttribute[]
	 */
	public static <T> FieldAttribute<T, ?>[] createGetter(final Class<T> clazz) {
		final List<FieldAttribute<T, ?>> list = new ArrayList<>();
		for (final Field field : clazz.getDeclaredFields()) {
			final String getterMethodName = FieldAttribute.getterMethodName(field);
			final Method getterMethod = FieldAttribute.getterMethod(clazz, getterMethodName);
			if (getterMethod != null) {
				final FieldAttribute fieldAttribute = FieldAttribute.create(clazz, field.getName(), field, getterMethod, null);
				if (fieldAttribute != null) {
					list.add(fieldAttribute);
				}
			}
		}
		return list.toArray(new FieldAttribute[list.size()]);
	}

	/**
	 * 根据Class生成字段包含setter方法对应的FieldAttribute对象数组.
	 *
	 * @param <T> 声明字段的类
	 * @param clazz 声明字段的类
	 * @return FieldAttribute[]
	 */
	public static <T> FieldAttribute<T, ?>[] createSetter(final Class<T> clazz) {
		final List<FieldAttribute<T, ?>> list = new ArrayList<>();
		for (final Field field : clazz.getDeclaredFields()) {
			final String setterMethodName = FieldAttribute.setterMethodName(field);
			final Method setterMethod = FieldAttribute.setterMethod(clazz, setterMethodName, field.getType());
			if (setterMethod != null) {
				final FieldAttribute<T, ?> fieldAttribute = FieldAttribute.create(clazz, field.getName(), field, null, setterMethod);
				if (fieldAttribute != null) {
					list.add(fieldAttribute);
				}
			}
		}
		return list.toArray(new FieldAttribute[list.size()]);
	}

	/**
	 * 根据Class、字段名、字段类型、getter和setter方法生成FieldAttribute.
	 *
	 * @param <T> 声明字段的类
	 * @param <F> 字段的数据类型
	 * @param clazz 声明字段的类
	 * @param fieldname 字段名称
	 * @param fieldtype 字段类型
	 * @param getter Function
	 * @param setter BiConsumer
	 * @return FieldAttribute
	 */
	public static <T, F> FieldAttribute<T, F> create(final Class<T> clazz, final String fieldname, final Class<F> fieldtype, final Function<T, F> getter, final BiConsumer<T, F> setter) {
		Objects.requireNonNull(clazz);
		Objects.requireNonNull(fieldname);
		Objects.requireNonNull(fieldtype);
		return new FieldAttribute<T, F>() {

			@Override
			public Class<F> getFieldType() {
				return fieldtype;
			}

			@Override
			public Class<T> getDeclaringClass() {
				return clazz;
			}

			@Override
			public String getFieldAliasName() {
				return fieldname;
			}

			@Override
			public String getFieldDefaultName() {
				return fieldname;
			}

			@Override
			public F getFieldValue(final T obj) {
				return getter == null ? null : getter.apply(obj);
			}

			@Override
			public void setFieldValue(final T obj, final F value) {
				if (setter != null) {
					setter.accept(obj, value);
				}
			}
		};
	}

}
