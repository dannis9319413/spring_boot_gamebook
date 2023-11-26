package com.gamebook.model;

import org.springframework.stereotype.Component;

@Component
public class Product {
	private int id;
	private String folder;
	private int product_category_id;
	private String name;
	private String description;
	private int price;
	private int is_pre;
	private int is_new;
	private int is_special;
	private String header;
	private String picture_1;
	private String picture_2;
	private String picture_3;
	private String picture_4;

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return this.id;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public String getFolder() {
		return this.folder;
	}

	public void setProductCategoryId(int product_category_id) {
		this.product_category_id = product_category_id;
	}

	public int getProductCategoryId() {
		return this.product_category_id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getPrice() {
		return this.price;
	}

	public void setIsPre(int is_pre) {
		this.is_pre = is_pre;
	}

	public int getIsPre() {
		return this.is_pre;
	}

	public void setIsNew(int is_new) {
		this.is_new = is_new;
	}

	public int getIsNew() {
		return this.is_new;
	}

	public void setIsSpecial(int is_special) {
		this.is_special = is_special;
	}

	public int getIsSpecial() {
		return this.is_special;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getHeader() {
		return this.header;
	}

	public void setPicture_1(String picture_1) {
		this.picture_1 = picture_1;
	}

	public String getPicture_1() {
		return this.picture_1;
	}

	public void setPicture_2(String picture_2) {
		this.picture_2 = picture_2;
	}

	public String getPicture_2() {
		return this.picture_2;
	}

	public void setPicture_3(String picture_3) {
		this.picture_3 = picture_3;
	}

	public String getPicture_3() {
		return this.picture_3;
	}

	public void setPicture_4(String picture_4) {
		this.picture_4 = picture_4;
	}

	public String getPicture_4() {
		return this.picture_4;
	}

	public String[] getImages() {
		return new String[] { this.picture_1, this.picture_2, this.picture_3, this.picture_4 };
	}
}
