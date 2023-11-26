package com.gamebook.model;

import org.springframework.stereotype.Component;

@Component
public class Order {
	private int id;
	private String status;
	private String order_no;
	private String user_id;
	private String total;
	private String shipping;
	private String name;
	private String mobile;
	private String zip;
	private String county;
	private String district;
	private String address;
	private String createTime;
	private String updateTime;

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return this.id;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return this.status;
	}

	public void setOrderNo(String order_no) {
		this.order_no = order_no;
	}

	public String getOrderNo() {
		return this.order_no;
	}

	public void setUserId(String user_id) {
		this.user_id = user_id;
	}

	public String getUserId() {
		return this.user_id;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public String getTotal() {
		return this.total;
	}

	public void setShipping(String shipping) {
		this.shipping = shipping;
	}

	public String getShipping() {
		return this.shipping;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getMobile() {
		return this.mobile;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getZip() {
		return this.zip;
	}

	public void setCounty(String county) {
		this.county = county;
	}

	public String getCounty() {
		return this.county;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getDistrict() {
		return this.district;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddress() {
		return this.address;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getCreateTime() {
		return this.createTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getUpdateTime() {
		return this.updateTime;
	}
}
