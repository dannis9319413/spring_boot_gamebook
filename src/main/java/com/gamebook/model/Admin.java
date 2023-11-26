package com.gamebook.model;

import org.springframework.stereotype.Component;

@Component
public class Admin {
	private int id;
	private String account;
	private String password;

	public void setAdminId(int id) {
		this.id = id;
	}

	public int getAdminId() {
		return this.id;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getAccount() {
		return this.account;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return this.password;
	}

}
