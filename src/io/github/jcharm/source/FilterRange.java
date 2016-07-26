/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.source;

import java.util.function.Predicate;

/**
 * 过滤数据需要通过范围过滤数据使用.
 *
 * @param <E> 数据类型
 */
public interface FilterRange<E extends Comparable> extends java.io.Serializable, Predicate<E> {

	/**
	 * 获取范围最小值.
	 *
	 * @return E
	 */
	public E getMin();

	/**
	 * 获取范围最大值.
	 *
	 * @return E
	 */
	public E getMax();

	/**
	 * Integer范围过滤数据.
	 */
	public static final class FilterIntRange implements FilterRange<Integer> {

		private static final long serialVersionUID = 1L;

		private Integer min = Integer.MIN_VALUE;

		private Integer max = Integer.MAX_VALUE;

		/**
		 * 构造函数.
		 */
		public FilterIntRange() {
		}

		/**
		 * 构造函数.
		 *
		 * @param min Integer
		 * @param max Integer
		 */
		public FilterIntRange(final Integer min, final Integer max) {
			if (min != null) {
				this.min = min;
			}
			if (max != null) {
				this.max = max;
			}
		}

		@Override
		public Integer getMin() {
			return this.min;
		}

		@Override
		public Integer getMax() {
			return this.max;
		}

		/**
		 * 设置最小值.
		 *
		 * @param min Integer
		 */
		public void setMin(final Integer min) {
			if (min != null) {
				this.min = min;
			}
		}

		/**
		 * 设置最大值.
		 *
		 * @param max Integer
		 */
		public void setMax(final Integer max) {
			if (max != null) {
				this.max = max;
			}
		}

		@Override
		public boolean test(final Integer t) {
			return (t >= this.min) && (t <= this.max);
		}

		@Override
		public String toString() {
			return "{min:" + this.min + ", max:" + this.max + "}";
		}

	}

	/**
	 * Long范围过滤数据.
	 */
	public static final class FilterLongRange implements FilterRange<Long> {

		private static final long serialVersionUID = 1L;

		private Long min = Long.MIN_VALUE;

		private Long max = Long.MAX_VALUE;

		/**
		 * 构造函数.
		 */
		public FilterLongRange() {
		}

		/**
		 * 构造函数.
		 *
		 * @param min Long
		 * @param max Long
		 */
		public FilterLongRange(final Long min, final Long max) {
			if (min != null) {
				this.min = min;
			}
			if (max != null) {
				this.max = max;
			}
		}

		@Override
		public Long getMin() {
			return this.min;
		}

		@Override
		public Long getMax() {
			return this.max;
		}

		/**
		 * 设置最小值.
		 *
		 * @param min Long
		 */
		public void setMin(final Long min) {
			if (min != null) {
				this.min = min;
			}
		}

		/**
		 * 设置最大值.
		 *
		 * @param max Long
		 */
		public void setMax(final Long max) {
			if (max != null) {
				this.max = max;
			}
		}

		@Override
		public boolean test(final Long t) {
			return (t >= this.min) && (t <= this.max);
		}

		@Override
		public String toString() {
			return "{min:" + this.min + ", max:" + this.max + "}";
		}
	}

	/**
	 * Float范围过滤数据.
	 */
	public static final class FilterFloatRange implements FilterRange<Float> {

		private static final long serialVersionUID = 1L;

		private Float min = Float.MIN_VALUE;

		private Float max = Float.MAX_VALUE;

		/**
		 * 构造函数.
		 */
		public FilterFloatRange() {
		}

		/**
		 * 构造函数.
		 *
		 * @param min Float
		 * @param max Float
		 */
		public FilterFloatRange(final Float min, final Float max) {
			if (min != null) {
				this.min = min;
			}
			if (max != null) {
				this.max = max;
			}
		}

		@Override
		public Float getMin() {
			return this.min;
		}

		@Override
		public Float getMax() {
			return this.max;
		}

		/**
		 * 设置最小值.
		 *
		 * @param min Float
		 */
		public void setMin(final Float min) {
			if (min != null) {
				this.min = min;
			}
		}

		/**
		 * 设置最大值.
		 *
		 * @param max Float
		 */
		public void setMax(final Float max) {
			if (max != null) {
				this.max = max;
			}
		}

		@Override
		public boolean test(final Float t) {
			return (t >= this.min) && (t <= this.max);
		}

		@Override
		public String toString() {
			return "{min:" + this.min + ", max:" + this.max + "}";
		}
	}

	/**
	 * Double范围过滤数据.
	 */
	public static final class FilterDoubleRange implements FilterRange<Double> {

		private static final long serialVersionUID = 1L;

		private Double min = Double.MIN_VALUE;

		private Double max = Double.MAX_VALUE;

		/**
		 * 构造函数.
		 */
		public FilterDoubleRange() {
		}

		/**
		 * 构造函数.
		 *
		 * @param min Double
		 * @param max Double
		 */
		public FilterDoubleRange(final Double min, final Double max) {
			if (min != null) {
				this.min = min;
			}
			if (max != null) {
				this.max = max;
			}
		}

		@Override
		public Double getMin() {
			return this.min;
		}

		@Override
		public Double getMax() {
			return this.max;
		}

		/**
		 * 设置最小值.
		 *
		 * @param min Double
		 */
		public void setMin(final Double min) {
			if (min != null) {
				this.min = min;
			}
		}

		/**
		 * 设置最大值.
		 *
		 * @param max Double
		 */
		public void setMax(final Double max) {
			if (max != null) {
				this.max = max;
			}
		}

		@Override
		public boolean test(final Double t) {
			return (t >= this.min) && (t <= this.max);
		}

		@Override
		public String toString() {
			return "{min:" + this.min + ", max:" + this.max + "}";
		}
	}

	/**
	 * String范围数据过滤.
	 */
	public static final class FilterStringRange implements FilterRange<String> {

		private static final long serialVersionUID = 1L;

		private String min = "";

		private String max = "";

		/**
		 * 构造函数.
		 */
		public FilterStringRange() {
		}

		/**
		 * 构造函数.
		 *
		 * @param min String
		 * @param max String
		 */
		public FilterStringRange(final String min, final String max) {
			this.min = min;
			this.max = max;
		}

		@Override
		public String getMin() {
			return this.min;
		}

		@Override
		public String getMax() {
			return this.max;
		}

		/**
		 * 设置最小值.
		 *
		 * @param min String
		 */
		public void setMin(final String min) {
			if (min != null) {
				this.min = min;
			}
		}

		/**
		 * 设置最大值.
		 *
		 * @param max String
		 */
		public void setMax(final String max) {
			if (max != null) {
				this.max = max;
			}
		}

		@Override
		public boolean test(final String t) {
			return (t.compareTo(this.min) >= 0) && (t.compareTo(this.max) <= 0);
		}

		@Override
		public String toString() {
			return "{min:'" + this.min + "', max:'" + this.max + "'}";
		}
	}

	/**
	 * Byte范围数据过滤.
	 */
	public static final class FilterByteRange implements FilterRange<Byte> {

		private static final long serialVersionUID = 1L;

		private Byte min = Byte.MIN_VALUE;

		private Byte max = Byte.MAX_VALUE;

		/**
		 * 构造函数.
		 */
		public FilterByteRange() {
		}

		/**
		 * 构造函数.
		 *
		 * @param min Byte
		 * @param max Byte
		 */
		public FilterByteRange(final Byte min, final Byte max) {
			if (min != null) {
				this.min = min;
			}
			if (max != null) {
				this.max = max;
			}
		}

		@Override
		public Byte getMin() {
			return this.min;
		}

		@Override
		public Byte getMax() {
			return this.max;
		}

		/**
		 * 设置最小值.
		 *
		 * @param min Byte
		 */
		public void setMin(final Byte min) {
			if (min != null) {
				this.min = min;
			}
		}

		/**
		 * 设置最大值.
		 *
		 * @param max Byte
		 */
		public void setMax(final Byte max) {
			if (max != null) {
				this.max = max;
			}
		}

		@Override
		public boolean test(final Byte t) {
			return (t >= this.min) && (t <= this.max);
		}

		@Override
		public String toString() {
			return "{min:" + this.min + ", max:" + this.max + "}";
		}

	}

}
