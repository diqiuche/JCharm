/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.
 */
package io.github.jcharm.test.convert;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import io.github.jcharm.convert.annotation.ConvertColumn;

/**
 * The Class SimpleBean.
 */
public class SimpleBean {

	@ConvertColumn(name = "realName")
	private String name;

	private int age;

	private boolean sex;

	private LocalDate birthDate;

	private List<String> hobbies;

	private Map<String, Integer> books;

	private SimpleBook simpleBook;

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Gets the age.
	 *
	 * @return the age
	 */
	public int getAge() {
		return this.age;
	}

	/**
	 * Sets the age.
	 *
	 * @param age the new age
	 */
	public void setAge(final int age) {
		this.age = age;
	}

	/**
	 * Checks if is sex.
	 *
	 * @return true, if is sex
	 */
	public boolean isSex() {
		return this.sex;
	}

	/**
	 * Sets the sex.
	 *
	 * @param sex the new sex
	 */
	public void setSex(final boolean sex) {
		this.sex = sex;
	}

	/**
	 * Gets the birth date.
	 *
	 * @return the birth date
	 */
	public LocalDate getBirthDate() {
		return this.birthDate;
	}

	/**
	 * Sets the birth date.
	 *
	 * @param birthDate the new birth date
	 */
	public void setBirthDate(final LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	/**
	 * Gets the hobbies.
	 *
	 * @return the hobbies
	 */
	public List<String> getHobbies() {
		return this.hobbies;
	}

	/**
	 * Sets the hobbies.
	 *
	 * @param hobbies the new hobbies
	 */
	public void setHobbies(final List<String> hobbies) {
		this.hobbies = hobbies;
	}

	/**
	 * Gets the books.
	 *
	 * @return the books
	 */
	public Map<String, Integer> getBooks() {
		return this.books;
	}

	/**
	 * Sets the books.
	 *
	 * @param books the books
	 */
	public void setBooks(final Map<String, Integer> books) {
		this.books = books;
	}

	public SimpleBook getSimpleBook() {
		return this.simpleBook;
	}

	public void setSimpleBook(final SimpleBook simpleBook) {
		this.simpleBook = simpleBook;
	}

}
