/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.source;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;

import io.github.jcharm.common.FieldAttribute;

/**
 * 过滤数据关联构建器.
 */
public class FilterJoinBuild extends FilterBuild {

	private Class joinClass;

	private EntityInfo joinEntity; // 在调用createSQLJoin和isCacheUseable时会注入

	private String[] joinColumns;

	/**
	 * 构造函数.
	 */
	public FilterJoinBuild() {
	}

	/**
	 * 构造函数.
	 *
	 * @param joinClass Class
	 * @param joinColumns String[]
	 * @param column String
	 * @param express FilterExpress
	 * @param itemand boolean
	 * @param value Serializable
	 */
	protected FilterJoinBuild(final Class joinClass, final String[] joinColumns, final String column, FilterExpress express, final boolean itemand, final Serializable value) {
		Objects.requireNonNull(joinClass);
		Objects.requireNonNull(joinColumns);
		if ((express == null) && (value != null)) {
			if (value instanceof FilterRange) {
				express = FilterExpress.BETWEEN;
			} else if (value instanceof Collection) {
				express = FilterExpress.IN;
			} else if (value.getClass().isArray()) {
				express = FilterExpress.IN;
			}
		}
		this.joinClass = joinClass;
		this.joinColumns = joinColumns;
		this.column = column;
		this.express = express == null ? FilterExpress.EQUAL : express;
		this.itemand = itemand;
		this.value = value;
	}

	/**
	 * 构造函数
	 *
	 * @param filterJoinBuild FilterJoinBuild
	 */
	protected FilterJoinBuild(final FilterJoinBuild filterJoinBuild) {
		this(filterJoinBuild.joinClass, filterJoinBuild.joinColumns, filterJoinBuild.column, filterJoinBuild.express, filterJoinBuild.itemand, filterJoinBuild.value);
		this.joinEntity = filterJoinBuild.joinEntity;
		this.or = filterJoinBuild.or;
		this.filterBuilds = filterJoinBuild.filterBuilds;
	}

	/**
	 * 创建FilterJoinBuild对象.
	 *
	 * @param joinClass Class
	 * @param joinColumn String
	 * @param column String
	 * @param value Serializable
	 * @return FilterJoinBuild
	 */
	public static FilterJoinBuild create(final Class joinClass, final String joinColumn, final String column, final Serializable value) {
		return FilterJoinBuild.create(joinClass, new String[] { joinColumn }, column, value);
	}

	/**
	 * 创建FilterJoinBuild对象.
	 *
	 * @param joinClass Class
	 * @param joinColumn String
	 * @param column String
	 * @param express FilterExpress
	 * @param value Serializable
	 * @return FilterJoinBuild
	 */
	public static FilterJoinBuild create(final Class joinClass, final String joinColumn, final String column, final FilterExpress express, final Serializable value) {
		return FilterJoinBuild.create(joinClass, new String[] { joinColumn }, column, express, value);
	}

	/**
	 * 创建FilterJoinBuild对象.
	 *
	 * @param joinClass Class
	 * @param joinColumn String
	 * @param column String
	 * @param express FilterExpress
	 * @param itemand boolean
	 * @param value Serializable
	 * @return FilterJoinBuild
	 */
	public static FilterJoinBuild create(final Class joinClass, final String joinColumn, final String column, final FilterExpress express, final boolean itemand, final Serializable value) {
		return FilterJoinBuild.create(joinClass, new String[] { joinColumn }, column, express, itemand, value);
	}

	/**
	 * 创建FilterJoinBuild对象.
	 *
	 * @param joinClass Class
	 * @param joinColumns String
	 * @param column String
	 * @param value Serializable
	 * @return FilterJoinBuild
	 */
	public static FilterJoinBuild create(final Class joinClass, final String[] joinColumns, final String column, final Serializable value) {
		return FilterJoinBuild.create(joinClass, joinColumns, column, null, value);
	}

	/**
	 * 创建FilterJoinBuild对象.
	 *
	 * @param joinClass Class
	 * @param joinColumns String
	 * @param column String[]
	 * @param express FilterExpress
	 * @param value Serializable
	 * @return FilterJoinBuild
	 */
	public static FilterJoinBuild create(final Class joinClass, final String[] joinColumns, final String column, final FilterExpress express, final Serializable value) {
		return FilterJoinBuild.create(joinClass, joinColumns, column, express, true, value);
	}

	/**
	 * 创建FilterJoinBuild对象.
	 *
	 * @param joinClass Class
	 * @param joinColumns String[]
	 * @param column String
	 * @param express FilterExpress
	 * @param itemand boolean
	 * @param value Serializable
	 * @return FilterJoinBuild
	 */
	public static FilterJoinBuild create(final Class joinClass, final String[] joinColumns, final String column, final FilterExpress express, final boolean itemand, final Serializable value) {
		return new FilterJoinBuild(joinClass, joinColumns, column, express, itemand, value);
	}

	@Override
	protected FilterBuild any(final FilterBuild filterBuild, final boolean signor) {
		Objects.requireNonNull(filterBuild);
		if (!(filterBuild instanceof FilterJoinBuild)) {
			throw new IllegalArgumentException(this + (signor ? " or " : " and ") + " a node but " + String.valueOf(filterBuild) + "is not a " + FilterJoinBuild.class.getSimpleName());
		}
		final FilterJoinBuild filterJoinBuild = (FilterJoinBuild) filterBuild;
		if (this.filterBuilds == null) {
			this.filterBuilds = new FilterBuild[] { filterJoinBuild };
			this.or = signor;
			return this;
		}
		if ((this.or == signor) || (this.column == null)) {
			final FilterBuild[] newsiblings = new FilterBuild[this.filterBuilds.length + 1];
			System.arraycopy(this.filterBuilds, 0, newsiblings, 0, this.filterBuilds.length);
			newsiblings[this.filterBuilds.length] = filterJoinBuild;
			this.filterBuilds = newsiblings;
			if (this.column == null) {
				this.or = signor;
			}
			return this;
		}
		this.filterBuilds = new FilterBuild[] { new FilterJoinBuild(filterJoinBuild), filterJoinBuild };
		this.column = null;
		this.express = null;
		this.itemand = true;
		this.value = null;
		this.joinClass = null;
		this.joinEntity = null;
		this.joinColumns = null;
		this.or = signor;
		return this;
	}

	@Override
	protected <T> CharSequence createSQLExpress(final EntityInfo<T> info, final Map<Class, String> joinTabalis) {
		return super.createSQLExpress(this.joinEntity == null ? info : this.joinEntity, joinTabalis);
	}

	@Override
	protected <T, E> Predicate<T> createPredicate(final EntityCache<T> cache) {
		if ((this.column == null) && (this.filterBuilds == null)) {
			return null;
		}
		final EntityCache<E> joinCache = this.joinEntity.getCache();
		final AtomicBoolean more = new AtomicBoolean();
		final Predicate<E> filter = this.createJoinPredicate(more);
		Predicate<T> rs = null;
		if ((filter == null) && !more.get()) {
			return rs;
		}
		if (filter != null) {
			final Predicate<E> inner = filter;
			rs = new Predicate<T>() {

				@Override
				public boolean test(final T t) {
					Predicate<E> joinPredicate = null;
					for (final String joinColumn : FilterJoinBuild.this.joinColumns) {
						final Serializable key = cache.getAttribute(joinColumn).getFieldValue(t);
						final FieldAttribute<E, Serializable> joinAttr = joinCache.getAttribute(joinColumn);
						final Predicate<E> p = (final E e) -> key.equals(joinAttr.getFieldValue(e));
						joinPredicate = joinPredicate == null ? p : joinPredicate.and(p);
					}
					return joinCache.exists(inner.and(joinPredicate));
				}

				@Override
				public String toString() {
					final StringBuilder sb = new StringBuilder();
					sb.append(" #-- ON ").append(FilterJoinBuild.this.joinColumns[0]).append("=").append(FilterJoinBuild.this.joinClass == null ? "null" : FilterJoinBuild.this.joinClass.getSimpleName()).append(".").append(FilterJoinBuild.this.joinColumns[0]);
					for (int i = 1; i < FilterJoinBuild.this.joinColumns.length; i++) {
						sb.append(" AND ").append(FilterJoinBuild.this.joinColumns[i]).append("=").append(FilterJoinBuild.this.joinClass == null ? "null" : FilterJoinBuild.this.joinClass.getSimpleName()).append(".").append(FilterJoinBuild.this.joinColumns[i]);
					}
					sb.append(" --# ").append(inner.toString());
					return sb.toString();
				}
			};
		}
		if (more.get()) { // 存在不同Class的关联表
			if (this.filterBuilds != null) {
				for (final FilterBuild node : this.filterBuilds) {
					if (((FilterJoinBuild) node).joinClass == this.joinClass) {
						continue;
					}
					final Predicate<T> f = node.createPredicate(cache);
					if (f == null) {
						continue;
					}
					final Predicate<T> one = rs;
					final Predicate<T> two = f;
					rs = (rs == null) ? f : (this.or ? new Predicate<T>() {

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
			}
		}
		return rs;
	}

	private <E> Predicate<E> createJoinPredicate(final AtomicBoolean more) {
		if ((this.column == null) && (this.filterBuilds == null)) {
			return null;
		}
		final EntityCache<E> joinCache = this.joinEntity.getCache();
		Predicate<E> filter = this.createElementPredicate(joinCache, true);
		if (this.filterBuilds != null) {
			for (final FilterBuild node : this.filterBuilds) {
				if (((FilterJoinBuild) node).joinClass != this.joinClass) {
					more.set(true);
					continue;
				}
				final Predicate<E> f = ((FilterJoinBuild) node).createJoinPredicate(more);
				if (f == null) {
					continue;
				}
				final Predicate<E> one = filter;
				final Predicate<E> two = f;
				filter = (filter == null) ? f : (this.or ? new Predicate<E>() {

					@Override
					public boolean test(final E t) {
						return one.test(t) || two.test(t);
					}

					@Override
					public String toString() {
						return "(" + one + " OR " + two + ")";
					}
				} : new Predicate<E>() {

					@Override
					public boolean test(final E t) {
						return one.test(t) && two.test(t);
					}

					@Override
					public String toString() {
						return "(" + one + " AND " + two + ")";
					}
				});
			}
		}
		return filter;
	}

	@Override
	protected <T> CharSequence createSQLJoin(final Function<Class, EntityInfo> func, final Map<Class, String> joinTabalis, final EntityInfo<T> info) {
		boolean morejoin = false;
		if (this.joinEntity == null) {
			if (this.joinClass != null) {
				this.joinEntity = func.apply(this.joinClass);
			}
			if (this.filterBuilds != null) {
				for (final FilterBuild node : this.filterBuilds) {
					if (node instanceof FilterJoinBuild) {
						final FilterJoinBuild joinNode = ((FilterJoinBuild) node);
						if (joinNode.joinClass != null) {
							joinNode.joinEntity = func.apply(joinNode.joinClass);
							if ((this.joinClass != null) && (this.joinClass != joinNode.joinClass)) {
								morejoin = true;
							}
						}
					}
				}
			}
		}
		final StringBuilder sb = new StringBuilder();
		if (this.joinClass != null) {
			sb.append(FilterJoinBuild.createElementSQLJoin(joinTabalis, info, this));
		}
		if (morejoin) {
			final Set<Class> set = new HashSet<>();
			if (this.joinClass != null) {
				set.add(this.joinClass);
			}
			for (final FilterBuild node : this.filterBuilds) {
				if (node instanceof FilterJoinBuild) {
					final FilterJoinBuild joinNode = ((FilterJoinBuild) node);
					if (!set.contains(joinNode.joinClass)) {
						final CharSequence cs = FilterJoinBuild.createElementSQLJoin(joinTabalis, info, joinNode);
						if (cs != null) {
							sb.append(cs);
							set.add(joinNode.joinClass);
						}
					}
				}
			}
		}
		return sb;
	}

	private static CharSequence createElementSQLJoin(final Map<Class, String> joinTabalis, final EntityInfo info, final FilterJoinBuild filterJoinBuild) {
		if (filterJoinBuild.joinClass == null) {
			return null;
		}
		final StringBuilder sb = new StringBuilder();
		final String[] joinColumns = filterJoinBuild.joinColumns;
		sb.append(" INNER JOIN ").append(filterJoinBuild.joinEntity.getTable()).append(" ").append(joinTabalis.get(filterJoinBuild.joinClass)).append(" ON ").append(info.getSQLColumn("a", joinColumns[0])).append(" = ")
				.append(filterJoinBuild.joinEntity.getSQLColumn(joinTabalis.get(filterJoinBuild.joinClass), joinColumns[0]));
		for (int i = 1; i < joinColumns.length; i++) {
			sb.append(" AND ").append(info.getSQLColumn("a", joinColumns[i])).append(" = ").append(filterJoinBuild.joinEntity.getSQLColumn(joinTabalis.get(filterJoinBuild.joinClass), joinColumns[i]));
		}
		return sb;
	}

	@Override
	protected boolean isCacheUseable(final Function<Class, EntityInfo> entityApplyer) {
		if (this.joinEntity == null) {
			this.joinEntity = entityApplyer.apply(this.joinClass);
		}
		if (!this.joinEntity.isCacheFullLoaded()) {
			return false;
		}
		if (this.filterBuilds == null) {
			return true;
		}
		for (final FilterBuild node : this.filterBuilds) {
			if (!node.isCacheUseable(entityApplyer)) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void putJoinTabalis(final Map<Class, String> map) {
		if ((this.joinClass != null) && !map.containsKey(this.joinClass)) {
			map.put(this.joinClass, String.valueOf((char) ('b' + map.size())));
		}
		if (this.filterBuilds == null) {
			return;
		}
		for (final FilterBuild node : this.filterBuilds) {
			node.putJoinTabalis(map);
		}
	}

	@Override
	protected final boolean isjoin() {
		return true;
	}

	@Override
	public String toString() {
		return this.toString(this.joinClass == null ? null : this.joinClass.getSimpleName()).toString();
	}

	/**
	 * Gets the join class.
	 *
	 * @return the join class
	 */
	public Class getJoinClass() {
		return this.joinClass;
	}

	/**
	 * Sets the join class.
	 *
	 * @param joinClass the new join class
	 */
	public void setJoinClass(final Class joinClass) {
		this.joinClass = joinClass;
	}

	/**
	 * Gets the join columns.
	 *
	 * @return the join columns
	 */
	public String[] getJoinColumns() {
		return this.joinColumns;
	}

	/**
	 * Sets the join columns.
	 *
	 * @param joinColumns the new join columns
	 */
	public void setJoinColumns(final String[] joinColumns) {
		this.joinColumns = joinColumns;
	}

}
