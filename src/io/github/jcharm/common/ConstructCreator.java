/**
 * Copyright (T) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.common;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;

/**
 * 通过ASM动态映射实现一个类的构造方法, 代替低效的反射实现.
 *
 * @param <T> 构建对象的数据类型
 */
public interface ConstructCreator<T> {

	/**
	 * 构建对象.
	 *
	 * @param params 构建函数的参数
	 * @return 构建的对象
	 */
	public T construct(Object... params);

	/**
	 * 该注解为内部使用注解, 只使用在Construct.construct方法上.
	 */
	@Documented
	@Target({ ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface ConstructorParameters {

		/**
		 * 构造函数参数名称.
		 *
		 * @return String[]
		 */
		String[] value();
	}

	/**
	 * 根据Class生成ConstructorObject映射类.
	 *
	 * @param <T> 构建对象的数据类型
	 * @param clazz 构建类
	 * @return ConstructCreator
	 */
	public static <T> ConstructCreator<T> create(Class<T> clazz) {
		if (clazz.isAssignableFrom(ArrayList.class)) {
			clazz = (Class<T>) ArrayList.class;
		} else if (clazz.isAssignableFrom(HashMap.class)) {
			clazz = (Class<T>) HashMap.class;
		} else if (clazz.isAssignableFrom(HashSet.class)) {
			clazz = (Class<T>) HashSet.class;
		}
		if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
			throw new RuntimeException("[" + clazz + "] is a interface or abstract class, cannot create it's ConstructCreator.");
		}
		final String superClassName = ConstructCreator.class.getName().replace('.', '/');
		final String declaringClassName = clazz.getName().replace('.', '/');
		final String declaringClassNameASM = Type.getDescriptor(clazz);// ASM中Java类型描述符
		ClassLoader classLoader = ConstructCreator.class.getClassLoader();
		String dynClassName = superClassName + "_" + clazz.getSimpleName() + "_" + (System.currentTimeMillis() % 10000);
		if (String.class.getClassLoader() != clazz.getClassLoader()) {
			classLoader = clazz.getClassLoader();
			dynClassName = declaringClassName + "_Dyn_" + ConstructCreator.class.getSimpleName();
		}
		try {
			return (ConstructCreator) Class.forName(dynClassName.replace('/', '.')).newInstance();
		} catch (final Exception e) {
		}
		// ===========================================ASM代码================================================
		final ClassWriter cw = new ClassWriter(0);
		MethodVisitor mv;
		AnnotationVisitor av0;
		cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_SUPER, dynClassName, "Ljava/lang/Object;L" + superClassName + "<" + declaringClassNameASM + ">;", "java/lang/Object", new String[] { superClassName });
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
			// ASM编写construct方法
			mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_VARARGS, "construct", "([Ljava/lang/Object;)" + declaringClassNameASM, null, null);
			if (clazz.getConstructors().length > 0) {// 类存在公共的构造方法
				Constructor constructor = null;
				for (final Constructor cst : clazz.getConstructors()) {// 查找无参构造函数
					if (cst.getParameterCount() == 0) {
						constructor = cst;
						break;
					}
				}
				if (constructor != null) {
					mv.visitTypeInsn(Opcodes.NEW, declaringClassName);
					mv.visitInsn(Opcodes.DUP);
					mv.visitMethodInsn(Opcodes.INVOKESPECIAL, declaringClassName, "<init>", Type.getConstructorDescriptor(constructor), false);
					mv.visitInsn(Opcodes.ARETURN);
					mv.visitMaxs(2, 2);
					mv.visitEnd();
				} else {
					for (final Constructor cst : clazz.getConstructors()) {// 查找符合条件的带参构造函数
						final Parameter[] parameters = cst.getParameters();
						if (parameters.length == 0) {
							continue;
						}
						boolean flag = true;
						for (final Parameter parameter : parameters) {
							try {
								clazz.getDeclaredField(parameter.getName());
							} catch (final Exception e) {
								flag = false;
								break;
							}
						}
						if (flag) {
							constructor = cst;
							break;
						}
					}
					if (constructor != null) {
						av0 = mv.visitAnnotation(Type.getDescriptor(ConstructorParameters.class), true);
						final AnnotationVisitor av1 = av0.visitArray("value");
						for (final Parameter parameter : constructor.getParameters()) {
							av1.visit(null, parameter.getName());
						}
						av1.visitEnd();
						av0.visitEnd();
						mv.visitTypeInsn(Opcodes.NEW, declaringClassName);
						mv.visitInsn(Opcodes.DUP);
						final Parameter[] parameters = constructor.getParameters();
						for (int i = 0; i < parameters.length; i++) {
							Class paramType = parameters[i].getType();
							final Class defaultParamType = paramType;
							if (paramType.isPrimitive()) {
								paramType = Array.get(Array.newInstance(paramType, 1), 0).getClass();
							}
							mv.visitVarInsn(Opcodes.ALOAD, 1);
							mv.visitIntInsn(Opcodes.BIPUSH, i);
							mv.visitInsn(Opcodes.AALOAD);
							if (defaultParamType == paramType) {
								mv.visitTypeInsn(Opcodes.CHECKCAST, paramType.getName().replace('.', '/'));
							} else {
								try {
									final Method defaultParamTypeMothed = paramType.getMethod(defaultParamType.getName() + "Value");
									mv.visitTypeInsn(Opcodes.CHECKCAST, paramType.getName().replace('.', '/'));
									mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, paramType.getName().replace('.', '/'), defaultParamType.getName() + "Value", Type.getMethodDescriptor(defaultParamTypeMothed), false);
								} catch (final Exception e) {
									throw new RuntimeException(e);
								}
							}
						}
						mv.visitMethodInsn(Opcodes.INVOKESPECIAL, declaringClassName, "<init>", Type.getConstructorDescriptor(constructor), false);
						mv.visitInsn(Opcodes.ARETURN);
						mv.visitMaxs(parameters.length + 3, 2);
						mv.visitEnd();
					} else {
						throw new RuntimeException("[" + clazz + "]  public constructor  parameter errors .");
					}
				}
			} else {
				throw new RuntimeException("[" + clazz + "] have no public constructor.");
			}
		}
		{
			// ASM编写虚拟construct方法
			mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_BRIDGE + Opcodes.ACC_VARARGS + Opcodes.ACC_SYNTHETIC, "construct", "([Ljava/lang/Object;)Ljava/lang/Object;", null, null);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, dynClassName, "construct", "([Ljava/lang/Object;)" + declaringClassNameASM, false);
			mv.visitInsn(Opcodes.ARETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		cw.visitEnd();
		final byte[] bytes = cw.toByteArray();
		final Class<ConstructCreator> creatorClass = (Class<ConstructCreator>) new ClassLoader(classLoader) {

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
}
