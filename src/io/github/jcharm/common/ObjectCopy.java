/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.common;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;

/**
 * 对象拷贝类.
 *
 * @param <D> 目标对象数据类型
 * @param <S> 源对象数据类型
 */
public interface ObjectCopy<D, S> {

	/**
	 * 拷贝对象.
	 *
	 * @param dest 目标对象
	 * @param src 源对象
	 * @return 目标对象
	 */
	public D copy(D dest, S src);

	/**
	 * 根据源类和目标类生成ObjectCopy.
	 *
	 * @param <D> 目标对象数据类型
	 * @param <S> 源对象数据类型
	 * @param destClass 目标类
	 * @param srcClass 源类
	 * @return ObjectCopy
	 */
	public static <D, S> ObjectCopy<D, S> create(final Class<D> destClass, final Class<S> srcClass) {
		return ObjectCopy.create(destClass, srcClass, null);
	}

	/**
	 * 根据源类和目标类生成ObjectCopy.
	 *
	 * @param <D> 目标对象数据类型
	 * @param <S> 源对象数据类型
	 * @param destClass 目标类
	 * @param srcClass 源类
	 * @param columnPredicate Predicate
	 * @return ObjectCopy
	 */
	public static <D, S> ObjectCopy<D, S> create(final Class<D> destClass, final Class<S> srcClass, final Predicate<String> columnPredicate) {
		final String supDynName = ObjectCopy.class.getName().replace('.', '/');
		final String destName = destClass.getName().replace('.', '/');
		final String srcName = srcClass.getName().replace('.', '/');
		final String destDesc = Type.getDescriptor(destClass);
		final String srcDesc = Type.getDescriptor(srcClass);
		String newDynName = supDynName + "Dyn_" + destClass.getSimpleName() + "_" + srcClass.getSimpleName();
		ClassLoader loader = ObjectCopy.class.getClassLoader();
		if (String.class.getClassLoader() != destClass.getClassLoader()) {
			loader = destClass.getClassLoader();
			newDynName = destName + "_Dyn" + ObjectCopy.class.getSimpleName() + "_" + srcClass.getSimpleName();
		}
		try {
			return (ObjectCopy) Class.forName(newDynName.replace('/', '.')).newInstance();
		} catch (final Exception ex) {
		}
		// ===========================================ASM代码================================================
		final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		MethodVisitor mv;
		cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_SUPER, newDynName, "Ljava/lang/Object;L" + supDynName + "<" + destDesc + srcDesc + ">;", "java/lang/Object", new String[] { supDynName });
		{
			// ASM编写构造函数
			mv = (cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null));
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			// ASM编写copy方法
			mv = (cw.visitMethod(Opcodes.ACC_PUBLIC, "copy", "(" + destDesc + srcDesc + ")" + destDesc, null, null));
			for (final java.lang.reflect.Field field : srcClass.getFields()) {
				if (Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				if (Modifier.isFinal(field.getModifiers())) {
					continue;
				}
				if (!Modifier.isPublic(field.getModifiers())) {
					continue;
				}
				final String fname = field.getName();
				try {
					if (!field.getType().equals(destClass.getField(fname).getType())) {
						continue;
					}
					if (!columnPredicate.test(fname)) {
						continue;
					}
				} catch (final Exception e) {
					continue;
				}
				mv.visitVarInsn(Opcodes.ALOAD, 1);
				mv.visitVarInsn(Opcodes.ALOAD, 2);
				final String td = Type.getDescriptor(field.getType());
				mv.visitFieldInsn(Opcodes.GETFIELD, srcName, fname, td);
				mv.visitFieldInsn(Opcodes.PUTFIELD, destName, fname, td);
			}
			for (final Method getter : srcClass.getMethods()) {
				if (Modifier.isStatic(getter.getModifiers())) {
					continue;
				}
				if (getter.getParameterTypes().length > 0) {
					continue;
				}
				if ("getClass".equals(getter.getName())) {
					continue;
				}
				if (!getter.getName().startsWith("get") && !getter.getName().startsWith("is")) {
					continue;
				}
				Method setter;
				final boolean is = getter.getName().startsWith("is");
				try {
					setter = destClass.getMethod(getter.getName().replaceFirst(is ? "is" : "get", "set"), getter.getReturnType());
					if (columnPredicate != null) {
						String col = setter.getName().substring(3);
						if ((col.length() < 2) || Character.isLowerCase(col.charAt(1))) {
							final char[] cs = col.toCharArray();
							cs[0] = Character.toLowerCase(cs[0]);
							col = new String(cs);
						}
						if (!columnPredicate.test(col)) {
							continue;
						}
					}
				} catch (final Exception e) {
					continue;
				}
				mv.visitVarInsn(Opcodes.ALOAD, 1);
				mv.visitVarInsn(Opcodes.ALOAD, 2);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, srcName, getter.getName(), Type.getMethodDescriptor(getter), false);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, destName, setter.getName(), Type.getMethodDescriptor(setter), false);
			}
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitInsn(Opcodes.ARETURN);
			mv.visitMaxs(3, 3);
			mv.visitEnd();
		}
		{
			// ASM编写虚拟copy方法
			mv = (cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_BRIDGE + Opcodes.ACC_SYNTHETIC, "copy", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", null, null));
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitTypeInsn(Opcodes.CHECKCAST, destName);
			mv.visitVarInsn(Opcodes.ALOAD, 2);
			mv.visitTypeInsn(Opcodes.CHECKCAST, srcName);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, newDynName, "copy", "(" + destDesc + srcDesc + ")" + destDesc, false);
			mv.visitInsn(Opcodes.ARETURN);
			mv.visitMaxs(3, 3);
			mv.visitEnd();
		}
		cw.visitEnd();
		final byte[] bytes = cw.toByteArray();
		final Class<?> creatorClazz = new ClassLoader(loader) {

			public final Class<?> loadClass(final String name, final byte[] b) {
				return this.defineClass(name, b, 0, b.length);
			}
		}.loadClass(newDynName.replace('/', '.'), bytes);
		try {
			return (ObjectCopy) creatorClazz.newInstance();
		} catch (final Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
