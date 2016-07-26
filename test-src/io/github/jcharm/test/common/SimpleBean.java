/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.test.common;

/**
 * 测试使用的简单JavaBean.
 */
public class SimpleBean {

	private String testName;

	private int testAge;

	private boolean testXieBie;

	private double testDouble;

	/**
	 * Instantiates a new simple bean.
	 *
	 * @param testName the test name
	 * @param testAge the test age
	 * @param testXieBie the test xie bie
	 */
	public SimpleBean(final String testName, final int testAge, final boolean testXieBie) {
		this.testName = testName;
		this.testAge = testAge;
		this.testXieBie = testXieBie;
	}

	/**
	 * Gets the test name.
	 *
	 * @return the test name
	 */
	public String getTestName() {
		return this.testName;
	}

	/**
	 * Sets the test name.
	 *
	 * @param testName the new test name
	 */
	public void setTestName(final String testName) {
		this.testName = testName;
	}

	/**
	 * Gets the test age.
	 *
	 * @return the test age
	 */
	public int getTestAge() {
		return this.testAge;
	}

	/**
	 * Sets the test age.
	 *
	 * @param testAge the new test age
	 */
	public void setTestAge(final int testAge) {
		this.testAge = testAge;
	}

	/**
	 * Checks if is test xie bie.
	 *
	 * @return true, if is test xie bie
	 */
	public boolean isTestXieBie() {
		return this.testXieBie;
	}

	/**
	 * Sets the test xie bie.
	 *
	 * @param testXieBie the new test xie bie
	 */
	public void setTestXieBie(final boolean testXieBie) {
		this.testXieBie = testXieBie;
	}

	public double getTestDouble() {
		return this.testDouble;
	}

	public void setTestDouble(final double testDouble) {
		this.testDouble = testDouble;
	}

}
