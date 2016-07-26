/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.source;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import io.github.jcharm.common.FieldAttribute;

/**
 * 过滤数据构建类.
 */
public class FilterBuild {

	/** column. */
	protected String column;

	/** FilterExpress. */
	protected FilterExpress express;

	/** value. */
	protected Serializable value;

	/** itemand. */
	protected boolean itemand;

	/** or. */
	protected boolean or;

	/** FilterBuild[]. */
	protected FilterBuild[] filterBuilds;

	/**
	 * 构造函数.
	 */
	public FilterBuild() {
	}

	/**
	 * 构造函数.
	 *
	 * @param col String
	 * @param exp FilterExpress
	 * @param itemand boolean
	 * @param val Serializable
	 */
	protected FilterBuild(final String col, FilterExpress exp, final boolean itemand, final Serializable val) {
		Objects.requireNonNull(col);
		if (exp == null) {
			if (val instanceof FilterRange) {
				exp = FilterExpress.BETWEEN;
			} else if (val instanceof Collection) {
				if (!((Collection) val).isEmpty()) {
					Object subval = null;
					for (final Object obj : (Collection) val) { // 取第一个值
						subval = obj;
						break;
					}
					if (subval instanceof FilterRange) {
						exp = FilterExpress.BETWEEN;
					} else if (subval instanceof Collection) {
						exp = FilterExpress.IN;
					} else if ((subval != null) && val.getClass().isArray()) {
						exp = FilterExpress.IN;
					}
				} else {// 空集合
					exp = FilterExpress.IN;
				}
			} else if ((val != null) && val.getClass().isArray()) {
				final Class comp = val.getClass().getComponentType();
				if (FilterRange.class.isAssignableFrom(comp)) {
					exp = FilterExpress.BETWEEN;
				} else if (comp.isArray() || Collection.class.isAssignableFrom(comp)) {
					exp = FilterExpress.IN;
				}
			}
		}
		this.column = col;
		this.express = exp == null ? FilterExpress.EQUAL : exp;
		this.itemand = itemand;
		this.value = val;
	}

	/**
	 * Any.
	 *
	 * @param filterBuild FilterBuild
	 * @param signor boolean
	 * @return FilterBuild
	 */
	protected FilterBuild any(final FilterBuild filterBuild, final boolean signor) {
		Objects.requireNonNull(filterBuild);
		if (this.column == null) {
			this.column = filterBuild.column;
			this.express = filterBuild.express;
			this.itemand = filterBuild.itemand;
			this.value = filterBuild.value;
			return this;
		}
		if (this.filterBuilds == null) {
			this.filterBuilds = new FilterBuild[] { filterBuild };
			this.or = signor;
			return this;
		}
		if (this.or == signor) {
			final FilterBuild[] newsiblings = new FilterBuild[this.filterBuilds.length + 1];
			System.arraycopy(this.filterBuilds, 0, newsiblings, 0, this.filterBuilds.length);
			newsiblings[this.filterBuilds.length] = filterBuild;
			this.filterBuilds = newsiblings;
			return this;
		}
		final FilterBuild newnode = new FilterBuild(this.column, this.express, this.itemand, this.value);
		newnode.or = this.or;
		newnode.filterBuilds = this.filterBuilds;
		this.filterBuilds = new FilterBuild[] { newnode, filterBuild };
		this.column = null;
		this.express = null;
		this.itemand = true;
		this.or = signor;
		this.value = null;
		return this;
	}

	/**
	 * And.
	 *
	 * @param filterBuild FilterBuild
	 * @return FilterBuild
	 */
	public final FilterBuild and(final FilterBuild filterBuild) {
		return this.any(filterBuild, false);
	}

	/**
	 * And.
	 *
	 * @param column String
	 * @param value Serializable
	 * @return FilterBuild
	 */
	public final FilterBuild and(final String column, final Serializable value) {
		return this.and(column, null, value);
	}

	/**
	 * And.
	 *
	 * @param column String
	 * @param express FilterExpress
	 * @param value Serializable
	 * @return FilterBuild
	 */
	public final FilterBuild and(final String column, final FilterExpress express, final Serializable value) {
		return this.and(column, express, true, value);
	}

	/**
	 * And.
	 *
	 * @param column String
	 * @param express FilterExpress
	 * @param itemand boolean
	 * @param value Serializable
	 * @return FilterBuild
	 */
	public final FilterBuild and(final String column, final FilterExpress express, final boolean itemand, final Serializable value) {
		return this.and(new FilterBuild(column, express, itemand, value));
	}

	/**
	 * Or.
	 *
	 * @param filterBuild FilterBuild
	 * @return FilterBuild
	 */
	public final FilterBuild or(final FilterBuild filterBuild) {
		return this.any(filterBuild, true);
	}

	/**
	 * Or.
	 *
	 * @param column String
	 * @param value Serializable
	 * @return FilterBuild
	 */
	public final FilterBuild or(final String column, final Serializable value) {
		return this.or(column, null, value);
	}

	/**
	 * Or.
	 *
	 * @param column String
	 * @param express FilterExpress
	 * @param value Serializable
	 * @return FilterBuild
	 */
	public final FilterBuild or(final String column, final FilterExpress express, final Serializable value) {
		return this.or(column, express, true, value);
	}

	/**
	 * Or.
	 *
	 * @param column String
	 * @param express FilterExpress
	 * @param itemand boolean
	 * @param value Serializable
	 * @return FilterBuild
	 */
	public final FilterBuild or(final String column, final FilterExpress express, final boolean itemand, final Serializable value) {
		return this.or(new FilterBuild(column, express, itemand, value));
	}

	/**
	 * 生成SQL的join语句, 该方法需要重载.
	 *
	 * @param <T> Entity类泛型
	 * @param func EntityInfo加载器
	 * @param joinTabalis 关联表集合
	 * @param info Entity类的EntityInfo
	 * @return CharSequence
	 */
	protected <T> CharSequence createSQLJoin(final Function<Class, EntityInfo> func, final Map<Class, String> joinTabalis, final EntityInfo<T> info) {
		if ((joinTabalis == null) || (this.filterBuilds == null)) {
			return null;
		}
		StringBuilder sb = null;
		for (final FilterBuild filterBuild : this.filterBuilds) {
			final CharSequence cs = filterBuild.createSQLJoin(func, joinTabalis, info);
			if (cs == null) {
				continue;
			}
			if (sb == null) {
				sb = new StringBuilder();
			}
			sb.append(cs);
		}
		return sb;
	}

	/**
	 * 判断是否存在关联表, 该方法需要重载.
	 *
	 * @return boolean
	 */
	protected boolean isjoin() {
		if (this.filterBuilds == null) {
			return false;
		}
		for (final FilterBuild filterBuild : this.filterBuilds) {
			if (filterBuild.isjoin()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取关联表集合.
	 *
	 * @return Map
	 */
	protected final Map<Class, String> getJoinTabalis() {
		if (!this.isjoin()) {
			return null;
		}
		final Map<Class, String> map = new HashMap<>();
		this.putJoinTabalis(map);
		return map;
	}

	/**
	 * 向集合中添加关联表.
	 *
	 * @param map Map
	 */
	protected void putJoinTabalis(final Map<Class, String> map) {
		if (this.filterBuilds == null) {
			return;
		}
		for (final FilterBuild filterBuild : this.filterBuilds) {
			filterBuild.putJoinTabalis(map);
		}
	}

	/**
	 * 是否可以使用缓存, 该方法需要重载.
	 *
	 * @param entityApplyer EntityInfo加载器
	 * @return boolean
	 */
	protected boolean isCacheUseable(final Function<Class, EntityInfo> entityApplyer) {
		if (this.filterBuilds == null) {
			return true;
		}
		for (final FilterBuild filterBuild : this.filterBuilds) {
			if (!filterBuild.isCacheUseable(entityApplyer)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 生成SQL的表达式语句, 该方法需要重载.
	 *
	 * @param <T> Entity类泛型
	 * @param info EntityInfo
	 * @param joinTabalis 关联表
	 * @return CharSequence
	 */
	protected <T> CharSequence createSQLExpress(final EntityInfo<T> info, final Map<Class, String> joinTabalis) {
		final CharSequence sb0 = (this.column == null) || (info == null) ? null : this.createElementSQLExpress(info, joinTabalis == null ? null : joinTabalis.get(info.getType()));
		if (this.filterBuilds == null) {
			return sb0;
		}
		final StringBuilder rs = new StringBuilder();
		rs.append('(');
		boolean more = false;
		if ((sb0 != null) && (sb0.length() > 2)) {
			more = true;
			rs.append(sb0);
		}
		for (final FilterBuild filterBuild : this.filterBuilds) {
			final CharSequence f = filterBuild.createSQLExpress(info, joinTabalis);
			if ((f == null) || (f.length() < 3)) {
				continue;
			}
			if (more) {
				rs.append(this.or ? " OR " : " AND ");
			}
			rs.append(f);
			more = true;
		}
		rs.append(')');
		if (rs.length() < 5) {
			return null;
		}
		return rs;
	}

	/**
	 * 创建FilterBuild对象.
	 *
	 * @param column Entity类字段名
	 * @param express FilterExpress
	 * @param itemand boolean
	 * @param value Serializable
	 * @return FilterBuild
	 */
	public static FilterBuild create(final String column, final FilterExpress express, final boolean itemand, final Serializable value) {
		return new FilterBuild(column, express, itemand, value);
	}

	/**
	 * 创建FilterBuild对象.
	 *
	 * @param column Entity类字段名
	 * @param express FilterExpress
	 * @param value Serializable
	 * @return FilterBuild
	 */
	public static FilterBuild create(final String column, final FilterExpress express, final Serializable value) {
		return FilterBuild.create(column, express, true, value);
	}

	/**
	 * 创建FilterBuild对象.
	 *
	 * @param column Entity类字段名
	 * @param value Serializable
	 * @return FilterBuild
	 */
	public static FilterBuild create(final String column, final Serializable value) {
		return FilterBuild.create(column, null, value);
	}

	private boolean needSplit(final Object val0) {
		return FilterBuild.needSplit(this.express, val0);
	}

	private static boolean needSplit(final FilterExpress express, final Object val0) {
		boolean items = (express != FilterExpress.IN) && (express != FilterExpress.NOTIN); // 是否数组集合的表达式
		if (!items) {
			if (val0.getClass().isArray()) {
				// getComponentType : 返回表示数组组件类型的Class, 如果此类不表示数组类, 则此方法返回null
				final Class comp = val0.getClass().getComponentType();
				// isAssignableFrom : 判定此Class对象所表示的类或接口与指定的Class参数所表示的类或接口是否相同, 或是否是其超类或超接口
				if (!(comp.isPrimitive() || CharSequence.class.isAssignableFrom(comp) || Number.class.isAssignableFrom(comp))) {
					items = true;
				}
			} else if (val0 instanceof Collection) {
				for (final Object fv : (Collection) val0) {
					if (fv == null) {
						continue;
					}
					final Class comp = fv.getClass();
					if (!(comp.isPrimitive() || CharSequence.class.isAssignableFrom(comp) || Number.class.isAssignableFrom(comp))) {
						items = true;
					}
					break; // 只需检测第一个值
				}
			}
		}
		return items;
	}

	private <T> CharSequence createElementSQLExpress(final EntityInfo<T> info, String talis, final Object val0) {
		if (this.column == null) {
			return null;
		}
		if (talis == null) {
			talis = "a";
		}
		if ((this.express == FilterExpress.ISNULL) || (this.express == FilterExpress.ISNOTNULL)) {
			return new StringBuilder().append(info.getSQLColumn(talis, this.column)).append(' ').append(this.express.value());
		}
		if (val0 == null) {
			return null;
		}
		if ((this.express == FilterExpress.FV_MOD) || (this.express == FilterExpress.FV_DIV)) {
			final FilterComplex fv = (FilterComplex) val0;
			return new StringBuilder().append(info.getSQLColumn(talis, this.column)).append(' ').append(this.express.value()).append(' ').append(fv.getOptvalue()).append(' ').append(fv.getExpress().value()).append(' ').append(fv.getDestvalue());
		}
		final boolean fk = (val0 instanceof FilterField);
		CharSequence val = fk ? info.getSQLColumn(talis, ((FilterField) val0).getColumn()) : FilterBuild.formatToString(this.express, val0);
		if (val == null) {
			return null;
		}
		final StringBuilder sb = new StringBuilder(32);
		if (this.express == FilterExpress.CONTAIN) {
			return info.containSQL.replace("${column}", info.getSQLColumn(talis, this.column)).replace("${keystr}", val);
		}
		if (this.express == FilterExpress.IGNORECASECONTAIN) {
			return info.containSQL.replace("${column}", "LOWER(" + info.getSQLColumn(talis, this.column) + ")").replace("${keystr}", val);
		}
		if (this.express == FilterExpress.NOTCONTAIN) {
			return info.notcontainSQL.replace("${column}", info.getSQLColumn(talis, this.column)).replace("${keystr}", val);
		}
		if (this.express == FilterExpress.IGNORECASENOTCONTAIN) {
			return info.notcontainSQL.replace("${column}", "LOWER(" + info.getSQLColumn(talis, this.column) + ")").replace("${keystr}", val);
		}

		if ((this.express == FilterExpress.IGNORECASELIKE) || (this.express == FilterExpress.IGNORECASENOTLIKE)) {
			sb.append("LOWER(").append(info.getSQLColumn(talis, this.column)).append(')');
			if (fk) {
				val = "LOWER(" + info.getSQLColumn(talis, ((FilterField) val0).getColumn()) + ')';
			}
		} else {
			sb.append(info.getSQLColumn(talis, this.column));
		}
		sb.append(' ');
		switch (this.express) {
		case OPAND:
		case OPOR:
			sb.append(this.express.value()).append(' ').append(val).append(" > 0");
			break;
		case OPANDNO:
			sb.append(this.express.value()).append(' ').append(val).append(" = 0");
			break;
		default:
			sb.append(this.express.value()).append(' ').append(val);
			break;
		}
		return sb;
	}

	/**
	 * Creates the element sql express.
	 *
	 * @param <T> the generic type
	 * @param info the info
	 * @param talis the talis
	 * @return the char sequence
	 */
	protected final <T> CharSequence createElementSQLExpress(final EntityInfo<T> info, final String talis) {
		final Object val0 = this.getValue();
		if (this.needSplit(val0)) {
			if (val0 instanceof Collection) {
				final StringBuilder sb = new StringBuilder();
				final boolean more = ((Collection) val0).size() > 1;
				if (more) {
					sb.append('(');
				}
				for (final Object fv : (Collection) val0) {
					if (fv == null) {
						continue;
					}
					final CharSequence cs = this.createElementSQLExpress(info, talis, fv);
					if (cs == null) {
						continue;
					}
					if (sb.length() > 2) {
						sb.append(this.itemand ? " AND " : " OR ");
					}
					sb.append(cs);
				}
				if (more) {
					sb.append(')');
				}
				return sb.length() > 3 ? sb : null; // 若sb的值只是()，则不过滤
			} else if (val0.getClass().isArray()) {
				final StringBuilder sb = new StringBuilder();
				final Object[] fvs = (Object[]) val0;
				final boolean more = fvs.length > 1;
				if (more) {
					sb.append('(');
				}
				for (final Object fv : fvs) {
					if (fv == null) {
						continue;
					}
					final CharSequence cs = this.createElementSQLExpress(info, talis, fv);
					if (cs == null) {
						continue;
					}
					if (sb.length() > 2) {
						sb.append(this.itemand ? " AND " : " OR ");
					}
					sb.append(cs);
				}
				if (more) {
					sb.append(')');
				}
				return sb.length() > 3 ? sb : null; // 若sb的值只是()，则不过滤
			}
		}
		return this.createElementSQLExpress(info, talis, val0);
	}

	/**
	 * Creates the predicate.
	 *
	 * @param <T> the generic type
	 * @param <E> the element type
	 * @param cache the cache
	 * @return the predicate
	 */
	protected <T, E> Predicate<T> createPredicate(final EntityCache<T> cache) {
		if ((cache == null) || ((this.column == null) && (this.filterBuilds == null))) {
			return null;
		}
		Predicate<T> filter = this.createElementPredicate(cache, false);
		if (this.filterBuilds == null) {
			return filter;
		}
		for (final FilterBuild filterBuild : this.filterBuilds) {
			final Predicate<T> f = filterBuild.createPredicate(cache);
			if (f == null) {
				continue;
			}
			final Predicate<T> one = filter;
			final Predicate<T> two = f;
			filter = (filter == null) ? f : (this.or ? new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					return one.test(t) || two.test(t);
				}

				@Override
				public String toString() {
					return "(" + one + " OR " + two + ")";
				}
			} : new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					return one.test(t) && two.test(t);
				}

				@Override
				public String toString() {
					return "(" + one + " AND " + two + ")";
				}
			});
		}
		return filter;
	}

	/**
	 * Creates the element predicate.
	 *
	 * @param <T> the generic type
	 * @param cache the cache
	 * @param join the join
	 * @return the predicate
	 */
	protected final <T> Predicate<T> createElementPredicate(final EntityCache<T> cache, final boolean join) {
		if (this.column == null) {
			return null;
		}
		return this.createElementPredicate(cache, join, cache.getAttribute(this.column));
	}

	/**
	 * Creates the element predicate.
	 *
	 * @param <T> the generic type
	 * @param cache the cache
	 * @param join the join
	 * @param attr the attr
	 * @return the predicate
	 */
	protected final <T> Predicate<T> createElementPredicate(final EntityCache<T> cache, final boolean join, final FieldAttribute<T, Serializable> attr) {
		final Object val0 = this.getValue();
		if (this.needSplit(val0)) {
			if (val0 instanceof Collection) {
				Predicate<T> filter = null;
				for (final Object fv : (Collection) val0) {
					if (fv == null) {
						continue;
					}
					final Predicate<T> f = this.createElementPredicate(cache, join, attr, fv);
					if (f == null) {
						continue;
					}
					final Predicate<T> one = filter;
					final Predicate<T> two = f;
					filter = (filter == null) ? f : (!this.itemand ? new Predicate<T>() {

						@Override
						public boolean test(final T t) {
							return one.test(t) || two.test(t);
						}

						@Override
						public String toString() {
							return "(" + one + " OR " + two + ")";
						}
					} : new Predicate<T>() {

						@Override
						public boolean test(final T t) {
							return one.test(t) && two.test(t);
						}

						@Override
						public String toString() {
							return "(" + one + " AND " + two + ")";
						}
					});
				}
				return filter;
			} else if (val0.getClass().isArray()) {
				final Class primtype = val0.getClass();
				Object val2 = val0;
				int ix = -1;
				if (primtype == boolean[].class) {
					final boolean[] bs = (boolean[]) val0;
					final Boolean[] ns = new Boolean[bs.length];
					for (final boolean v : bs) {
						ns[++ix] = v;
					}
					val2 = ns;
				} else if (primtype == byte[].class) {
					final byte[] bs = (byte[]) val0;
					final Byte[] ns = new Byte[bs.length];
					for (final byte v : bs) {
						ns[++ix] = v;
					}
					val2 = ns;
				} else if (primtype == short[].class) {
					final short[] bs = (short[]) val0;
					final Short[] ns = new Short[bs.length];
					for (final short v : bs) {
						ns[++ix] = v;
					}
					val2 = ns;
				} else if (primtype == char[].class) {
					final char[] bs = (char[]) val0;
					final Character[] ns = new Character[bs.length];
					for (final char v : bs) {
						ns[++ix] = v;
					}
					val2 = ns;
				} else if (primtype == int[].class) {
					final int[] bs = (int[]) val0;
					final Integer[] ns = new Integer[bs.length];
					for (final int v : bs) {
						ns[++ix] = v;
					}
					val2 = ns;
				} else if (primtype == float[].class) {
					final float[] bs = (float[]) val0;
					final Float[] ns = new Float[bs.length];
					for (final float v : bs) {
						ns[++ix] = v;
					}
					val2 = ns;
				} else if (primtype == long[].class) {
					final long[] bs = (long[]) val0;
					final Long[] ns = new Long[bs.length];
					for (final long v : bs) {
						ns[++ix] = v;
					}
					val2 = ns;
				} else if (primtype == double[].class) {
					final double[] bs = (double[]) val0;
					final Double[] ns = new Double[bs.length];
					for (final double v : bs) {
						ns[++ix] = v;
					}
					val2 = ns;
				}
				Predicate<T> filter = null;
				for (final Object fv : (Object[]) val2) {
					if (fv == null) {
						continue;
					}
					final Predicate<T> f = this.createElementPredicate(cache, join, attr, fv);
					if (f == null) {
						continue;
					}
					final Predicate<T> one = filter;
					final Predicate<T> two = f;
					filter = (filter == null) ? f : (!this.itemand ? new Predicate<T>() {

						@Override
						public boolean test(final T t) {
							return one.test(t) || two.test(t);
						}

						@Override
						public String toString() {
							return "(" + one + " OR " + two + ")";
						}
					} : new Predicate<T>() {

						@Override
						public boolean test(final T t) {
							return one.test(t) && two.test(t);
						}

						@Override
						public String toString() {
							return "(" + one + " AND " + two + ")";
						}
					});
				}
				return filter;
			}
		}
		return this.createElementPredicate(cache, join, attr, val0);
	}

	/**
	 * Creates the element predicate.
	 *
	 * @param <T> the generic type
	 * @param cache the cache
	 * @param join the join
	 * @param attr the attr
	 * @param val0 the val0
	 * @return the predicate
	 */
	protected final <T> Predicate<T> createElementPredicate(final EntityCache<T> cache, final boolean join, final FieldAttribute<T, Serializable> attr, Object val0) {
		if (attr == null) {
			return null;
		}
		final String field = join ? (cache.getType().getSimpleName() + "." + attr.getFieldDefaultName()) : attr.getFieldDefaultName();
		if (this.express == FilterExpress.ISNULL) {
			return new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					return attr.getFieldValue(t) == null;
				}

				@Override
				public String toString() {
					return field + " = null";
				}
			};
		}
		if (this.express == FilterExpress.ISNOTNULL) {
			return new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					return attr.getFieldValue(t) != null;
				}

				@Override
				public String toString() {
					return field + " != null";
				}
			};
		}
		if (attr == null) {
			return null;
		}
		if (val0 == null) {
			return null;
		}

		final Class atype = attr.getFieldType();
		final Class valtype = val0.getClass();
		if ((atype != valtype) && (val0 instanceof Number)) {
			if ((atype == int.class) || (atype == Integer.class)) {
				val0 = ((Number) val0).intValue();
			} else if ((atype == long.class) || (atype == Long.class)) {
				val0 = ((Number) val0).longValue();
			} else if ((atype == short.class) || (atype == Short.class)) {
				val0 = ((Number) val0).shortValue();
			} else if ((atype == float.class) || (atype == Float.class)) {
				val0 = ((Number) val0).floatValue();
			} else if ((atype == byte.class) || (atype == Byte.class)) {
				val0 = ((Number) val0).byteValue();
			} else if ((atype == double.class) || (atype == Double.class)) {
				val0 = ((Number) val0).doubleValue();
			}
		} else if (valtype.isArray()) {
			final int len = Array.getLength(val0);
			if ((len == 0) && (this.express == FilterExpress.NOTIN)) {
				return null;
			}
			final Class compType = valtype.getComponentType();
			if ((atype != compType) && (len > 0)) {
				if (!compType.isPrimitive() && Number.class.isAssignableFrom(compType)) {
					throw new RuntimeException("param(" + val0 + ") type not match " + atype + " for column " + this.column);
				}
				if ((atype == int.class) || (atype == Integer.class)) {
					final int[] vs = new int[len];
					for (int i = 0; i < len; i++) {
						vs[i] = ((Number) Array.get(val0, i)).intValue();
					}
					val0 = vs;
				} else if ((atype == long.class) || (atype == Long.class)) {
					final long[] vs = new long[len];
					for (int i = 0; i < len; i++) {
						vs[i] = ((Number) Array.get(val0, i)).longValue();
					}
					val0 = vs;
				} else if ((atype == short.class) || (atype == Short.class)) {
					final short[] vs = new short[len];
					for (int i = 0; i < len; i++) {
						vs[i] = ((Number) Array.get(val0, i)).shortValue();
					}
					val0 = vs;
				} else if ((atype == float.class) || (atype == Float.class)) {
					final float[] vs = new float[len];
					for (int i = 0; i < len; i++) {
						vs[i] = ((Number) Array.get(val0, i)).floatValue();
					}
					val0 = vs;
				} else if ((atype == byte.class) || (atype == Byte.class)) {
					final byte[] vs = new byte[len];
					for (int i = 0; i < len; i++) {
						vs[i] = ((Number) Array.get(val0, i)).byteValue();
					}
					val0 = vs;
				} else if ((atype == double.class) || (atype == Double.class)) {
					final double[] vs = new double[len];
					for (int i = 0; i < len; i++) {
						vs[i] = ((Number) Array.get(val0, i)).doubleValue();
					}
					val0 = vs;
				}
			}
		} else if (val0 instanceof Collection) {
			final Collection collection = (Collection) val0;
			if (collection.isEmpty() && (this.express == FilterExpress.NOTIN)) {
				return null;
			}
			if (!collection.isEmpty()) {
				final Iterator it = collection.iterator();
				it.hasNext();
				final Class fs = it.next().getClass();
				Class pfs = fs;
				if (fs == Integer.class) {
					pfs = int.class;
				} else if (fs == Long.class) {
					pfs = long.class;
				} else if (fs == Short.class) {
					pfs = short.class;
				} else if (fs == Float.class) {
					pfs = float.class;
				} else if (fs == Byte.class) {
					pfs = byte.class;
				} else if (fs == Double.class) {
					pfs = double.class;
				}
				if (Number.class.isAssignableFrom(fs) && (atype != fs) && (atype != pfs)) { // 需要转换
					final ArrayList list = new ArrayList(collection.size());
					if ((atype == int.class) || (atype == Integer.class)) {
						for (final Number num : (Collection<Number>) collection) {
							list.add(num.intValue());
						}
					} else if ((atype == long.class) || (atype == Long.class)) {
						for (final Number num : (Collection<Number>) collection) {
							list.add(num.longValue());
						}
					} else if ((atype == short.class) || (atype == Short.class)) {
						for (final Number num : (Collection<Number>) collection) {
							list.add(num.shortValue());
						}
					} else if ((atype == float.class) || (atype == Float.class)) {
						for (final Number num : (Collection<Number>) collection) {
							list.add(num.floatValue());
						}
					} else if ((atype == byte.class) || (atype == Byte.class)) {
						for (final Number num : (Collection<Number>) collection) {
							list.add(num.byteValue());
						}
					} else if ((atype == double.class) || (atype == Double.class)) {
						for (final Number num : (Collection<Number>) collection) {
							list.add(num.doubleValue());
						}
					}
					val0 = list;
				}
			}
		}
		final Serializable val = (Serializable) val0;
		final boolean fk = (val instanceof FilterField);
		final FieldAttribute<T, Serializable> fkattr = fk ? cache.getAttribute(((FilterField) val).getColumn()) : null;
		if (fk && (fkattr == null)) {
			throw new RuntimeException(cache.getType() + " not found column(" + ((FilterField) val).getColumn() + ")");
		}
		switch (this.express) {
		case EQUAL:
			return fk ? new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					return Objects.equals(fkattr.getFieldValue(t), attr.getFieldValue(t));
				}

				@Override
				public String toString() {
					return field + ' ' + FilterBuild.this.express.value() + ' ' + fkattr.getFieldDefaultName();
				}
			} : new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					return val.equals(attr.getFieldValue(t));
				}

				@Override
				public String toString() {
					return field + ' ' + FilterBuild.this.express.value() + ' ' + FilterBuild.formatToString(val);
				}
			};
		case NOTEQUAL:
			return fk ? new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					return !Objects.equals(fkattr.getFieldValue(t), attr.getFieldValue(t));
				}

				@Override
				public String toString() {
					return field + ' ' + FilterBuild.this.express.value() + ' ' + fkattr.getFieldDefaultName();
				}
			} : new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					return !val.equals(attr.getFieldValue(t));
				}

				@Override
				public String toString() {
					return field + ' ' + FilterBuild.this.express.value() + ' ' + FilterBuild.formatToString(val);
				}
			};
		case GREATERTHAN:
			return fk ? new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					return ((Number) attr.getFieldValue(t)).longValue() > ((Number) fkattr.getFieldValue(t)).longValue();
				}

				@Override
				public String toString() {
					return field + ' ' + FilterBuild.this.express.value() + ' ' + fkattr.getFieldDefaultName();
				}
			} : new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					return ((Number) attr.getFieldValue(t)).longValue() > ((Number) val).longValue();
				}

				@Override
				public String toString() {
					return field + ' ' + FilterBuild.this.express.value() + ' ' + val;
				}
			};
		case LESSTHAN:
			return fk ? new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					return ((Number) attr.getFieldValue(t)).longValue() < ((Number) fkattr.getFieldValue(t)).longValue();
				}

				@Override
				public String toString() {
					return field + ' ' + FilterBuild.this.express.value() + ' ' + fkattr.getFieldDefaultName();
				}
			} : new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					return ((Number) attr.getFieldValue(t)).longValue() < ((Number) val).longValue();
				}

				@Override
				public String toString() {
					return field + ' ' + FilterBuild.this.express.value() + ' ' + val;
				}
			};
		case GREATERTHANOREQUALTO:
			return fk ? new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					return ((Number) attr.getFieldValue(t)).longValue() >= ((Number) fkattr.getFieldValue(t)).longValue();
				}

				@Override
				public String toString() {
					return field + ' ' + FilterBuild.this.express.value() + ' ' + fkattr.getFieldDefaultName();
				}
			} : new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					return ((Number) attr.getFieldValue(t)).longValue() >= ((Number) val).longValue();
				}

				@Override
				public String toString() {
					return field + ' ' + FilterBuild.this.express.value() + ' ' + val;
				}
			};
		case LESSTHANOREQUALTO:
			return fk ? new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					return ((Number) attr.getFieldValue(t)).longValue() <= ((Number) fkattr.getFieldValue(t)).longValue();
				}

				@Override
				public String toString() {
					return field + ' ' + FilterBuild.this.express.value() + ' ' + fkattr.getFieldDefaultName();
				}
			} : new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					return ((Number) attr.getFieldValue(t)).longValue() <= ((Number) val).longValue();
				}

				@Override
				public String toString() {
					return field + ' ' + FilterBuild.this.express.value() + ' ' + val;
				}
			};

		case FV_MOD:
			final FilterComplex fv0 = (FilterComplex) val;
			switch (fv0.getExpress()) {
			case EQUAL:
				return new Predicate<T>() {

					@Override
					public boolean test(final T t) {
						return (((Number) attr.getFieldValue(t)).longValue() % fv0.getOptvalue().longValue()) == fv0.getDestvalue().longValue();
					}

					@Override
					public String toString() {
						return field + " " + FilterBuild.this.express.value() + " " + fv0.getOptvalue() + " " + fv0.getExpress().value() + " " + fv0.getDestvalue();
					}
				};
			case NOTEQUAL:
				return new Predicate<T>() {

					@Override
					public boolean test(final T t) {
						return (((Number) attr.getFieldValue(t)).longValue() % fv0.getOptvalue().longValue()) != fv0.getDestvalue().longValue();
					}

					@Override
					public String toString() {
						return field + " " + FilterBuild.this.express.value() + " " + fv0.getOptvalue() + " " + fv0.getExpress().value() + " " + fv0.getDestvalue();
					}
				};
			case GREATERTHAN:
				return new Predicate<T>() {

					@Override
					public boolean test(final T t) {
						return (((Number) attr.getFieldValue(t)).longValue() % fv0.getOptvalue().longValue()) > fv0.getDestvalue().longValue();
					}

					@Override
					public String toString() {
						return field + " " + FilterBuild.this.express.value() + " " + fv0.getOptvalue() + " " + fv0.getExpress().value() + " " + fv0.getDestvalue();
					}
				};
			case LESSTHAN:
				return new Predicate<T>() {

					@Override
					public boolean test(final T t) {
						return (((Number) attr.getFieldValue(t)).longValue() % fv0.getOptvalue().longValue()) < fv0.getDestvalue().longValue();
					}

					@Override
					public String toString() {
						return field + " " + FilterBuild.this.express.value() + " " + fv0.getOptvalue() + " " + fv0.getExpress().value() + " " + fv0.getDestvalue();
					}
				};
			case GREATERTHANOREQUALTO:
				return new Predicate<T>() {

					@Override
					public boolean test(final T t) {
						return (((Number) attr.getFieldValue(t)).longValue() % fv0.getOptvalue().longValue()) >= fv0.getDestvalue().longValue();
					}

					@Override
					public String toString() {
						return field + " " + FilterBuild.this.express.value() + " " + fv0.getOptvalue() + " " + fv0.getExpress().value() + " " + fv0.getDestvalue();
					}
				};
			case LESSTHANOREQUALTO:
				return new Predicate<T>() {

					@Override
					public boolean test(final T t) {
						return (((Number) attr.getFieldValue(t)).longValue() % fv0.getOptvalue().longValue()) <= fv0.getDestvalue().longValue();
					}

					@Override
					public String toString() {
						return field + " " + FilterBuild.this.express.value() + " " + fv0.getOptvalue() + " " + fv0.getExpress().value() + " " + fv0.getDestvalue();
					}
				};
			default:
				throw new RuntimeException("(" + fv0 + ")'s express illegal, must be =, !=, <, >, <=, >=");
			}
		case FV_DIV:
			final FilterComplex fv1 = (FilterComplex) val;
			switch (fv1.getExpress()) {
			case EQUAL:
				return new Predicate<T>() {

					@Override
					public boolean test(final T t) {
						return (((Number) attr.getFieldValue(t)).longValue() / fv1.getOptvalue().longValue()) == fv1.getDestvalue().longValue();
					}

					@Override
					public String toString() {
						return field + " " + FilterBuild.this.express.value() + " " + fv1.getOptvalue() + " " + fv1.getExpress().value() + " " + fv1.getDestvalue();
					}
				};
			case NOTEQUAL:
				return new Predicate<T>() {

					@Override
					public boolean test(final T t) {
						return (((Number) attr.getFieldValue(t)).longValue() / fv1.getOptvalue().longValue()) != fv1.getDestvalue().longValue();
					}

					@Override
					public String toString() {
						return field + " " + FilterBuild.this.express.value() + " " + fv1.getOptvalue() + " " + fv1.getExpress().value() + " " + fv1.getDestvalue();
					}
				};
			case GREATERTHAN:
				return new Predicate<T>() {

					@Override
					public boolean test(final T t) {
						return (((Number) attr.getFieldValue(t)).longValue() / fv1.getOptvalue().longValue()) > fv1.getDestvalue().longValue();
					}

					@Override
					public String toString() {
						return field + " " + FilterBuild.this.express.value() + " " + fv1.getOptvalue() + " " + fv1.getExpress().value() + " " + fv1.getDestvalue();
					}
				};
			case LESSTHAN:
				return new Predicate<T>() {

					@Override
					public boolean test(final T t) {
						return (((Number) attr.getFieldValue(t)).longValue() / fv1.getOptvalue().longValue()) < fv1.getDestvalue().longValue();
					}

					@Override
					public String toString() {
						return field + " " + FilterBuild.this.express.value() + " " + fv1.getOptvalue() + " " + fv1.getExpress().value() + " " + fv1.getDestvalue();
					}
				};
			case GREATERTHANOREQUALTO:
				return new Predicate<T>() {

					@Override
					public boolean test(final T t) {
						return (((Number) attr.getFieldValue(t)).longValue() / fv1.getOptvalue().longValue()) >= fv1.getDestvalue().longValue();
					}

					@Override
					public String toString() {
						return field + " " + FilterBuild.this.express.value() + " " + fv1.getOptvalue() + " " + fv1.getExpress().value() + " " + fv1.getDestvalue();
					}
				};
			case LESSTHANOREQUALTO:
				return new Predicate<T>() {

					@Override
					public boolean test(final T t) {
						return (((Number) attr.getFieldValue(t)).longValue() / fv1.getOptvalue().longValue()) <= fv1.getDestvalue().longValue();
					}

					@Override
					public String toString() {
						return field + " " + FilterBuild.this.express.value() + " " + fv1.getOptvalue() + " " + fv1.getExpress().value() + " " + fv1.getDestvalue();
					}
				};
			default:
				throw new RuntimeException("(" + fv1 + ")'s express illegal, must be =, !=, <, >, <=, >=");
			}
		case OPAND:
			return fk ? new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					return (((Number) attr.getFieldValue(t)).longValue() & ((Number) fkattr.getFieldValue(t)).longValue()) > 0;
				}

				@Override
				public String toString() {
					return field + " & " + fkattr.getFieldDefaultName() + " > 0";
				}
			} : new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					return (((Number) attr.getFieldValue(t)).longValue() & ((Number) val).longValue()) > 0;
				}

				@Override
				public String toString() {
					return field + " & " + val + " > 0";
				}
			};
		case OPOR:
			return fk ? new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					return (((Number) attr.getFieldValue(t)).longValue() | ((Number) fkattr.getFieldValue(t)).longValue()) > 0;
				}

				@Override
				public String toString() {
					return field + " | " + fkattr.getFieldDefaultName() + " > 0";
				}
			} : new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					return (((Number) attr.getFieldValue(t)).longValue() | ((Number) val).longValue()) > 0;
				}

				@Override
				public String toString() {
					return field + " | " + val + " > 0";
				}
			};
		case OPANDNO:
			return fk ? new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					return (((Number) attr.getFieldValue(t)).longValue() & ((Number) fkattr.getFieldValue(t)).longValue()) == 0;
				}

				@Override
				public String toString() {
					return field + " & " + fkattr.getFieldDefaultName() + " = 0";
				}
			} : new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					return (((Number) attr.getFieldValue(t)).longValue() & ((Number) val).longValue()) == 0;
				}

				@Override
				public String toString() {
					return field + " & " + val + " = 0";
				}
			};
		case LIKE:
			return fk ? new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					final Object rs = attr.getFieldValue(t);
					final Object rs2 = fkattr.getFieldValue(t);
					return (rs != null) && (rs2 != null) && rs.toString().contains(rs2.toString());
				}

				@Override
				public String toString() {
					return field + ' ' + FilterBuild.this.express.value() + ' ' + fkattr.getFieldDefaultName();
				}
			} : new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					final Object rs = attr.getFieldValue(t);
					return (rs != null) && rs.toString().contains(val.toString());
				}

				@Override
				public String toString() {
					return field + ' ' + FilterBuild.this.express.value() + ' ' + FilterBuild.formatToString(val);
				}
			};
		case STARTSWITH:
			return fk ? new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					final Object rs = attr.getFieldValue(t);
					final Object rs2 = fkattr.getFieldValue(t);
					return (rs != null) && (rs2 != null) && rs.toString().startsWith(rs2.toString());
				}

				@Override
				public String toString() {
					return field + " STARTSWITH " + fkattr.getFieldDefaultName();
				}
			} : new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					final Object rs = attr.getFieldValue(t);
					return (rs != null) && rs.toString().startsWith(val.toString());
				}

				@Override
				public String toString() {
					return field + " STARTSWITH " + FilterBuild.formatToString(val);
				}
			};
		case ENDSWITH:
			return fk ? new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					final Object rs = attr.getFieldValue(t);
					final Object rs2 = fkattr.getFieldValue(t);
					return (rs != null) && (rs2 != null) && rs.toString().endsWith(rs2.toString());
				}

				@Override
				public String toString() {
					return field + " ENDSWITH " + fkattr.getFieldDefaultName();
				}
			} : new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					final Object rs = attr.getFieldValue(t);
					return (rs != null) && rs.toString().endsWith(val.toString());
				}

				@Override
				public String toString() {
					return field + " ENDSWITH " + FilterBuild.formatToString(val);
				}
			};
		case IGNORECASELIKE:
			if (fk) {
				return new Predicate<T>() {

					@Override
					public boolean test(final T t) {
						final Object rs = attr.getFieldValue(t);
						final Object rs2 = fkattr.getFieldValue(t);
						return (rs != null) && (rs2 != null) && rs.toString().toLowerCase().contains(rs2.toString().toLowerCase());
					}

					@Override
					public String toString() {
						return "LOWER(" + field + ") " + FilterBuild.this.express.value() + " LOWER(" + fkattr.getFieldDefaultName() + ')';
					}
				};
			}
			final String valstr = val.toString().toLowerCase();
			return new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					final Object rs = attr.getFieldValue(t);
					return (rs != null) && rs.toString().toLowerCase().contains(valstr);
				}

				@Override
				public String toString() {
					return "LOWER(" + field + ") " + FilterBuild.this.express.value() + ' ' + FilterBuild.formatToString(valstr);
				}
			};
		case NOTSTARTSWITH:
			return fk ? new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					final Object rs = attr.getFieldValue(t);
					final Object rs2 = fkattr.getFieldValue(t);
					return (rs == null) || (rs2 == null) || !rs.toString().startsWith(rs2.toString());
				}

				@Override
				public String toString() {
					return field + " NOT STARTSWITH " + fkattr.getFieldDefaultName();
				}
			} : new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					final Object rs = attr.getFieldValue(t);
					return (rs == null) || !rs.toString().startsWith(val.toString());
				}

				@Override
				public String toString() {
					return field + " NOT STARTSWITH " + FilterBuild.formatToString(val);
				}
			};
		case NOTENDSWITH:
			return fk ? new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					final Object rs = attr.getFieldValue(t);
					final Object rs2 = fkattr.getFieldValue(t);
					return (rs == null) || (rs2 == null) || !rs.toString().endsWith(rs2.toString());
				}

				@Override
				public String toString() {
					return field + " NOT ENDSWITH " + fkattr.getFieldDefaultName();
				}
			} : new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					final Object rs = attr.getFieldValue(t);
					return (rs == null) || !rs.toString().endsWith(val.toString());
				}

				@Override
				public String toString() {
					return field + " NOT ENDSWITH " + FilterBuild.formatToString(val);
				}
			};
		case IGNORECASENOTLIKE:
			if (fk) {
				return new Predicate<T>() {

					@Override
					public boolean test(final T t) {
						final Object rs = attr.getFieldValue(t);
						final Object rs2 = fkattr.getFieldValue(t);
						return (rs == null) || (rs2 == null) || !rs.toString().toLowerCase().contains(rs2.toString().toLowerCase());
					}

					@Override
					public String toString() {
						return "LOWER(" + field + ") " + FilterBuild.this.express.value() + " LOWER(" + fkattr.getFieldDefaultName() + ')';
					}
				};
			}
			final String valstr2 = val.toString().toLowerCase();
			return new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					final Object rs = attr.getFieldValue(t);
					return (rs == null) || !rs.toString().toLowerCase().contains(valstr2);
				}

				@Override
				public String toString() {
					return "LOWER(" + field + ") " + FilterBuild.this.express.value() + ' ' + FilterBuild.formatToString(valstr2);
				}
			};
		case CONTAIN:
			return fk ? new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					final Object rs = attr.getFieldValue(t);
					final Object rs2 = fkattr.getFieldValue(t);
					return (rs != null) && (rs2 != null) && rs2.toString().contains(rs.toString());
				}

				@Override
				public String toString() {
					return fkattr.getFieldDefaultName() + ' ' + FilterBuild.this.express.value() + ' ' + field;
				}
			} : new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					final Object rs = attr.getFieldValue(t);
					return (rs != null) && val.toString().contains(rs.toString());
				}

				@Override
				public String toString() {
					return "" + FilterBuild.formatToString(val) + ' ' + FilterBuild.this.express.value() + ' ' + field;
				}
			};
		case IGNORECASECONTAIN:
			if (fk) {
				return new Predicate<T>() {

					@Override
					public boolean test(final T t) {
						final Object rs = attr.getFieldValue(t);
						final Object rs2 = fkattr.getFieldValue(t);
						return (rs != null) && (rs2 != null) && rs2.toString().toLowerCase().contains(rs.toString().toLowerCase());
					}

					@Override
					public String toString() {
						return " LOWER(" + fkattr.getFieldDefaultName() + ") " + FilterBuild.this.express.value() + ' ' + "LOWER(" + field + ") ";
					}
				};
			}
			final String valstr3 = val.toString().toLowerCase();
			return new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					final Object rs = attr.getFieldValue(t);
					return (rs != null) && valstr3.contains(rs.toString().toLowerCase());
				}

				@Override
				public String toString() {
					return "" + FilterBuild.formatToString(valstr3) + FilterBuild.this.express.value() + ' ' + "LOWER(" + field + ") ";
				}
			};
		case NOTCONTAIN:
			return fk ? new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					final Object rs = attr.getFieldValue(t);
					final Object rs2 = fkattr.getFieldValue(t);
					return (rs == null) || (rs2 == null) || !rs2.toString().contains(rs.toString());
				}

				@Override
				public String toString() {
					return fkattr.getFieldDefaultName() + ' ' + FilterBuild.this.express.value() + ' ' + field;
				}
			} : new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					final Object rs = attr.getFieldValue(t);
					return (rs == null) || !val.toString().contains(rs.toString());
				}

				@Override
				public String toString() {
					return "" + FilterBuild.formatToString(val) + ' ' + FilterBuild.this.express.value() + ' ' + field;
				}
			};
		case IGNORECASENOTCONTAIN:
			if (fk) {
				return new Predicate<T>() {

					@Override
					public boolean test(final T t) {
						final Object rs = attr.getFieldValue(t);
						final Object rs2 = fkattr.getFieldValue(t);
						return (rs == null) || (rs2 == null) || !rs2.toString().toLowerCase().contains(rs.toString().toLowerCase());
					}

					@Override
					public String toString() {
						return " LOWER(" + fkattr.getFieldDefaultName() + ") " + FilterBuild.this.express.value() + ' ' + "LOWER(" + field + ") ";
					}
				};
			}
			final String valstr4 = val.toString().toLowerCase();
			return new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					final Object rs = attr.getFieldValue(t);
					return (rs == null) || !valstr4.contains(rs.toString().toLowerCase());
				}

				@Override
				public String toString() {
					return "" + FilterBuild.formatToString(valstr4) + FilterBuild.this.express.value() + ' ' + "LOWER(" + field + ") ";
				}
			};
		case BETWEEN:
		case NOTBETWEEN:
			final FilterRange range = (FilterRange) val;
			final Comparable min = range.getMin();
			final Comparable max = range.getMax();
			if (this.express == FilterExpress.BETWEEN) {
				return new Predicate<T>() {

					@Override
					public boolean test(final T t) {
						final Comparable rs = (Comparable) attr.getFieldValue(t);
						if (rs == null) {
							return false;
						}
						if ((min != null) && (min.compareTo(rs) >= 0)) {
							return false;
						}
						return !((max != null) && (max.compareTo(rs) <= 0));
					}

					@Override
					public String toString() {
						return field + " BETWEEN " + min + " AND " + max;
					}
				};
			}
			if (this.express == FilterExpress.NOTBETWEEN) {
				return new Predicate<T>() {

					@Override
					public boolean test(final T t) {
						final Comparable rs = (Comparable) attr.getFieldValue(t);
						if (rs == null) {
							return true;
						}
						if ((min != null) && (min.compareTo(rs) >= 0)) {
							return true;
						}
						return ((max != null) && (max.compareTo(rs) <= 0));
					}

					@Override
					public String toString() {
						return field + " NOT BETWEEN " + min + " AND " + max;
					}
				};
			}
			return null;
		case IN:
		case NOTIN:
			Predicate<T> filter;
			if (val instanceof Collection) {
				final Collection array = (Collection) val;
				if (array.isEmpty()) { // express 只会是 IN
					filter = new Predicate<T>() {

						@Override
						public boolean test(final T t) {
							return false;
						}

						@Override
						public String toString() {
							return field + ' ' + FilterBuild.this.express.value() + " []";
						}
					};
				} else {
					filter = new Predicate<T>() {

						@Override
						public boolean test(final T t) {
							final Object rs = attr.getFieldValue(t);
							return (rs != null) && array.contains(rs);
						}

						@Override
						public String toString() {
							return field + ' ' + FilterBuild.this.express.value() + ' ' + val;
						}
					};
				}
			} else {
				final Class type = val.getClass();
				if (Array.getLength(val) == 0) {// express 只会是 IN
					filter = new Predicate<T>() {

						@Override
						public boolean test(final T t) {
							return false;
						}

						@Override
						public String toString() {
							return field + ' ' + FilterBuild.this.express.value() + " []";
						}
					};
				} else if (type == int[].class) {
					filter = new Predicate<T>() {

						@Override
						public boolean test(final T t) {
							final Object rs = attr.getFieldValue(t);
							if (rs == null) {
								return false;
							}
							final int k = (int) rs;
							for (final int v : (int[]) val) {
								if (v == k) {
									return true;
								}
							}
							return false;
						}

						@Override
						public String toString() {
							return field + ' ' + FilterBuild.this.express.value() + ' ' + Arrays.toString((int[]) val);
						}
					};
				} else if (type == short[].class) {
					filter = new Predicate<T>() {

						@Override
						public boolean test(final T t) {
							final Object rs = attr.getFieldValue(t);
							if (rs == null) {
								return false;
							}
							final short k = (short) rs;
							for (final short v : (short[]) val) {
								if (v == k) {
									return true;
								}
							}
							return false;
						}

						@Override
						public String toString() {
							return field + ' ' + FilterBuild.this.express.value() + ' ' + Arrays.toString((short[]) val);
						}
					};
				} else if (type == long[].class) {
					filter = new Predicate<T>() {

						@Override
						public boolean test(final T t) {
							final Object rs = attr.getFieldValue(t);
							if (rs == null) {
								return false;
							}
							final long k = (long) rs;
							for (final long v : (long[]) val) {
								if (v == k) {
									return true;
								}
							}
							return false;
						}

						@Override
						public String toString() {
							return field + ' ' + FilterBuild.this.express.value() + ' ' + Arrays.toString((long[]) val);
						}
					};
				} else if (type == float[].class) {
					filter = new Predicate<T>() {

						@Override
						public boolean test(final T t) {
							final Object rs = attr.getFieldValue(t);
							if (rs == null) {
								return false;
							}
							final float k = (float) rs;
							for (final float v : (float[]) val) {
								if (v == k) {
									return true;
								}
							}
							return false;
						}

						@Override
						public String toString() {
							return field + ' ' + FilterBuild.this.express.value() + ' ' + Arrays.toString((float[]) val);
						}
					};
				} else if (type == double[].class) {
					filter = new Predicate<T>() {

						@Override
						public boolean test(final T t) {
							final Object rs = attr.getFieldValue(t);
							if (rs == null) {
								return false;
							}
							final double k = (double) rs;
							for (final double v : (double[]) val) {
								if (v == k) {
									return true;
								}
							}
							return false;
						}

						@Override
						public String toString() {
							return field + ' ' + FilterBuild.this.express.value() + ' ' + Arrays.toString((double[]) val);
						}
					};
				} else {
					filter = new Predicate<T>() {

						@Override
						public boolean test(final T t) {
							final Object rs = attr.getFieldValue(t);
							if (rs == null) {
								return false;
							}
							for (final Object v : (Object[]) val) {
								if (rs.equals(v)) {
									return true;
								}
							}
							return false;
						}

						@Override
						public String toString() {
							return field + ' ' + FilterBuild.this.express.value() + ' ' + Arrays.toString((Object[]) val);
						}
					};
				}
			}
			if (this.express == FilterExpress.NOTIN) {
				final Predicate<T> filter2 = filter;
				filter = new Predicate<T>() {

					@Override
					public boolean test(final T t) {
						return !filter2.test(t);
					}

					@Override
					public String toString() {
						return filter2.toString();
					}
				};
			}
			return filter;
		}
		return null;
	}

	@Override
	public String toString() {
		return this.toString(null).toString();
	}

	/**
	 * To string.
	 *
	 * @param prefix the prefix
	 * @return the string builder
	 */
	protected StringBuilder toString(final String prefix) {
		final StringBuilder sb = new StringBuilder();
		final StringBuilder element = this.toElementString(prefix);
		final boolean more = (element.length() > 0) && (this.filterBuilds != null);
		if (more) {
			sb.append('(');
		}
		sb.append(element);
		if (this.filterBuilds != null) {
			for (final FilterBuild filterBuild : this.filterBuilds) {
				final String s = filterBuild.toString();
				if (s.length() < 1) {
					continue;
				}
				if (sb.length() > 1) {
					sb.append(this.or ? " OR " : " AND ");
				}
				sb.append(s);
			}
		}
		if (more) {
			sb.append(')');
		}
		return sb;
	}

	/**
	 * To element string.
	 *
	 * @param prefix the prefix
	 * @return the string builder
	 */
	protected final StringBuilder toElementString(final String prefix) {
		final Serializable val0 = this.getValue();
		if (this.needSplit(val0)) {
			if (val0 instanceof Collection) {
				final StringBuilder sb = new StringBuilder();
				final boolean more = ((Collection) val0).size() > 1;
				if (more) {
					sb.append('(');
				}
				for (final Object fv : (Collection) val0) {
					if (fv == null) {
						continue;
					}
					final CharSequence cs = this.toElementString(prefix, fv);
					if (cs == null) {
						continue;
					}
					if (sb.length() > 2) {
						sb.append(this.itemand ? " AND " : " OR ");
					}
					sb.append(cs);
				}
				if (more) {
					sb.append(')');
				}
				return sb.length() > 3 ? sb : null; // 若sb的值只是()，则不过滤
			} else if (val0.getClass().isArray()) {
				final StringBuilder sb = new StringBuilder();
				final Object[] fvs = (Object[]) val0;
				final boolean more = fvs.length > 1;
				if (more) {
					sb.append('(');
				}
				for (final Object fv : fvs) {
					if (fv == null) {
						continue;
					}
					final CharSequence cs = this.toElementString(prefix, fv);
					if (cs == null) {
						continue;
					}
					if (sb.length() > 2) {
						sb.append(this.itemand ? " AND " : " OR ");
					}
					sb.append(cs);
				}
				if (more) {
					sb.append(')');
				}
				return sb.length() > 3 ? sb : null; // 若sb的值只是()，则不过滤
			}
		}
		return this.toElementString(prefix, val0);
	}

	/**
	 * To element string.
	 *
	 * @param prefix the prefix
	 * @param ev the ev
	 * @return the string builder
	 */
	protected final StringBuilder toElementString(final String prefix, final Object ev) {
		final StringBuilder sb = new StringBuilder();
		if (this.column != null) {
			final String col = prefix == null ? this.column : (prefix + "." + this.column);
			if ((this.express == FilterExpress.ISNULL) || (this.express == FilterExpress.ISNOTNULL)) {
				sb.append(col).append(' ').append(this.express.value());
			} else if (ev != null) {
				final boolean lower = ((this.express == FilterExpress.IGNORECASELIKE) || (this.express == FilterExpress.IGNORECASENOTLIKE) || (this.express == FilterExpress.IGNORECASECONTAIN) || (this.express == FilterExpress.IGNORECASENOTCONTAIN));
				sb.append(lower ? ("LOWER(" + col + ')') : col).append(' ').append(this.express.value()).append(' ').append(FilterBuild.formatToString(this.express, ev));
			}
		}
		return sb;
	}

	/**
	 * Format to string.
	 *
	 * @param value the value
	 * @return the char sequence
	 */
	protected static CharSequence formatToString(final Object value) {
		final CharSequence sb = FilterBuild.formatToString(null, value);
		return sb == null ? null : sb.toString();
	}

	private static CharSequence formatToString(final FilterExpress express, Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Number) {
			return String.valueOf(value);
		}
		if (value instanceof CharSequence) {
			if ((express == FilterExpress.LIKE) || (express == FilterExpress.NOTLIKE)) {
				value = "%" + value + '%';
			} else if ((express == FilterExpress.STARTSWITH) || (express == FilterExpress.NOTSTARTSWITH)) {
				value = value + "%";
			} else if ((express == FilterExpress.ENDSWITH) || (express == FilterExpress.NOTENDSWITH)) {
				value = "%" + value;
			} else if ((express == FilterExpress.IGNORECASELIKE) || (express == FilterExpress.IGNORECASENOTLIKE)) {
				value = "%" + value.toString().toLowerCase() + '%';
			} else if ((express == FilterExpress.IGNORECASECONTAIN) || (express == FilterExpress.IGNORECASENOTCONTAIN)) {
				value = value.toString().toLowerCase();
			}
			return new StringBuilder().append('\'').append(value.toString().replace("'", "\\'")).append('\'');
		} else if (value instanceof FilterRange) {
			final FilterRange range = (FilterRange) value;
			final boolean rangestring = range.getClass() == FilterRange.FilterStringRange.class;
			final StringBuilder sb = new StringBuilder();
			if (rangestring) {
				sb.append('\'').append(range.getMin().toString().replace("'", "\\'")).append('\'');
			} else {
				sb.append(range.getMin());
			}
			sb.append(" AND ");
			if (rangestring) {
				sb.append('\'').append(range.getMax().toString().replace("'", "\\'")).append('\'');
			} else {
				sb.append(range.getMax());
			}
			return sb;
		} else if (value.getClass().isArray()) {
			final int len = Array.getLength(value);
			if (len == 0) {
				return express == FilterExpress.NOTIN ? null : new StringBuilder("(NULL)");
			}
			if (len == 1) {
				final Object firstval = Array.get(value, 0);
				if ((firstval != null) && firstval.getClass().isArray()) {
					return FilterBuild.formatToString(express, firstval);
				}
			}
			final StringBuilder sb = new StringBuilder();
			sb.append('(');
			for (int i = 0; i < len; i++) {
				final Object o = Array.get(value, i);
				if (sb.length() > 1) {
					sb.append(',');
				}
				if (o instanceof CharSequence) {
					sb.append('\'').append(o.toString().replace("'", "\\'")).append('\'');
				} else {
					sb.append(o);
				}
			}
			return sb.append(')');
		} else if (value instanceof Collection) {
			final Collection c = (Collection) value;
			if (c.isEmpty()) {
				return express == FilterExpress.NOTIN ? null : new StringBuilder("(NULL)");
			}
			final StringBuilder sb = new StringBuilder();
			sb.append('(');
			for (final Object o : c) {
				if (sb.length() > 1) {
					sb.append(',');
				}
				if (o instanceof CharSequence) {
					sb.append('\'').append(o.toString().replace("'", "\\'")).append('\'');
				} else {
					sb.append(o);
				}
			}
			return sb.append(')');
		}
		return String.valueOf(value);
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public final Serializable getValue() {
		return this.value;
	}

	/**
	 * Sets the value.
	 *
	 * @param value the new value
	 */
	public final void setValue(final Serializable value) {
		this.value = value;
	}

	/**
	 * Checks if is or.
	 *
	 * @return true, if is or
	 */
	public final boolean isOr() {
		return this.or;
	}

	/**
	 * Sets the or.
	 *
	 * @param or the new or
	 */
	public final void setOr(final boolean or) {
		this.or = or;
	}

	/**
	 * Gets the column.
	 *
	 * @return the column
	 */
	public final String getColumn() {
		return this.column;
	}

	/**
	 * Sets the column.
	 *
	 * @param column the new column
	 */
	public final void setColumn(final String column) {
		this.column = column;
	}

	/**
	 * Gets the express.
	 *
	 * @return the express
	 */
	public final FilterExpress getExpress() {
		return this.express;
	}

	/**
	 * Sets the express.
	 *
	 * @param express the new express
	 */
	public final void setExpress(final FilterExpress express) {
		this.express = express;
	}

	/**
	 * Checks if is itemand.
	 *
	 * @return true, if is itemand
	 */
	public final boolean isItemand() {
		return this.itemand;
	}

	/**
	 * Sets the itemand.
	 *
	 * @param itemand the new itemand
	 */
	public final void setItemand(final boolean itemand) {
		this.itemand = itemand;
	}

	/**
	 * Gets the filter builds.
	 *
	 * @return the filter builds
	 */
	public final FilterBuild[] getFilterBuilds() {
		return this.filterBuilds;
	}

	/**
	 * Sets the filter builds.
	 *
	 * @param filterBuilds the new filter builds
	 */
	public final void setFilterBuilds(final FilterBuild[] filterBuilds) {
		this.filterBuilds = filterBuilds;
	}

}
