package com.gamebook.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;

@Controller
public class AdminController {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@GetMapping("/admin/login")
	public String login(HttpSession session, Model model) {
		if (Boolean.TRUE.equals(session.getAttribute("isAdminLogin"))) {
			return "redirect:/admin/news/list";
		}
		return "admin/login";
	}

	@PostMapping("/admin/loging")
	public String loging(@RequestParam("account") String account, @RequestParam("password") String password,
			HttpSession session) {
		String sql = "SELECT account FROM admins WHERE account = ? AND password = ? LIMIT 1";
		List<Map<String, Object>> admin = jdbcTemplate.queryForList(sql, account, password);
		if (admin.size() > 0) {
			session.setAttribute("isAdminLogin", true);
			session.setAttribute("name", admin.get(0).get("account"));
			return "redirect:/admin/news/list";
		}
		return "redirect:/admin/login";
	}

	@RequestMapping("admin/logout")
	public String logout(HttpSession session) {
		if (session != null) {
			session.invalidate();
		}
		return "redirect:/admin/login";
	}

}
