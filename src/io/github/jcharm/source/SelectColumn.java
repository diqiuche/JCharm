/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.source;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * 选择列.
 */
public class SelectColumn implements Predicate<String> {

	private Pattern[] patterns;

	private String[] columns;

	private boolean excludable;

	/**
	 * 构造函数.
	 */
	public SelectColumn() {
	}

	/**
	 * 构造函数.
	 *
	 * @param columns0 字段名集合
	 * @param excludable 是否排除
	 */
	protected SelectColumn(final String[] columns0, final boolean excludable) {
		this.excludable = excludable;
		final int len = columns0.length;
		if (len < 1) {
			return;
		}
		Pattern[] regs = null;
		String[] cols = null;
		int regcount = 0;
		int colcount = 0;
		for (final String col : columns0) {
			boolean reg = false;
			for (int i = 0; i < col.length(); i++) {
				final char ch = col.charAt(i);
				if ((ch == '^') || (ch == '$') || (ch == '*') || (ch == '?') || (ch == '+') || (ch == '[') || (ch == '(')) {
					reg = true;
					break;
				}
			}
			if (reg) {
				if (regs == null) {
					regs = new Pattern[len];
				}
				regs[regcount++] = Pattern.compile(col);
			} else {
				if (cols == null) {
					cols = new String[len];
				}
				cols[colcount++] = col;
			}
		}
		if (regs != null) {
			if (regcount == len) {
				this.patterns = regs;
			} else {
				this.patterns = Arrays.copyOf(regs, regcount);
			}
		}
		if (cols != null) {
			if (colcount == len) {
				this.columns = cols;
			} else {
				this.columns = Arrays.copyOf(cols, colcount);
			}
		}
	}

	/**
	 * 包含的字段名集合选择列.
	 *
	 * @param columns 包含的字段名集合
	 * @return SelectColumn
	 */
	public static SelectColumn createIncludes(final String... columns) {
		return new SelectColumn(columns, false);
	}

	/**
	 * 排除的字段名集合选择列.
	 *
	 * @param columns 排除的字段名集合
	 * @return SelectColumn
	 */
	public static SelectColumn createExcludes(final String... columns) {
		return new SelectColumn(columns, true);
	}

	@Override
	public boolean test(final String column) {
		if (this.columns != null) {
			for (final String col : this.columns) {
				if (col.equalsIgnoreCase(column)) {
					return !this.excludable;
				}
			}
		}
		if (this.patterns != null) {
			for (final Pattern reg : this.patterns) {
				if (reg.matcher(column).find()) {
					return !this.excludable;
				}
			}
		}
		return this.excludable;
	}

	/**
	 * 获取字段名集合.
	 *
	 * @return String[]
	 */
	public String[] getColumns() {
		return this.columns;
	}

	/**
	 * 设置字段名集合.
	 *
	 * @param columns 字段名的集合
	 */
	public void setColumns(final String[] columns) {
		this.columns = columns;
	}

	/**
	 * 是否为排除的选择列.
	 *
	 * @return boolean
	 */
	public boolean isExcludable() {
		return this.excludable;
	}

	/**
	 * 设置为是否排除的选择列.
	 *
	 * @param excludable the new excludable
	 */
	public void setExcludable(final boolean excludable) {
		this.excludable = excludable;
	}

	/**
	 * 获取字段名正则表达式集合.
	 *
	 * @return Pattern[]
	 */
	public Pattern[] getPatterns() {
		return this.patterns;
	}

	/**
	 * 设置字段名正则表达式集合.
	 *
	 * @param patterns Pattern[]
	 */
	public void setPatterns(final Pattern[] patterns) {
		this.patterns = patterns;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName()).append("{excludable=").append(this.excludable);
		if (this.columns != null) {
			sb.append(", columns=").append(Arrays.toString(this.columns));
		}
		if (this.patterns != null) {
			sb.append(", patterns=").append(Arrays.toString(this.patterns));
		}
		return sb.append('}').toString();
	}

}
