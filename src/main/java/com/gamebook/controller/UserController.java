package com.gamebook.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gamebook.model.News;
import com.gamebook.model.Order;
import com.gamebook.model.Product;
import com.gamebook.model.User;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {
	@Autowired
	JdbcTemplate jdbcTemplate;

	@RequestMapping("/login")
	public String login() {
		return "web/login";
	}

	@RequestMapping("/register")
	public String register() {
		return "web/register";
	}

	@RequestMapping("/registering")
	public String registering(@RequestParam("email") String email, @RequestParam("password") String password,
			@RequestParam("confirm_password") String confirm_password, RedirectAttributes redirAttrs) {
		if (email.isEmpty() || password.isEmpty() || confirm_password.isEmpty()) {
			redirAttrs.addFlashAttribute("error", "需填入Email、密碼、確認密碼");
			return "redirect:/register";
		}
		if (!password.equals(confirm_password)) {
			redirAttrs.addFlashAttribute("error", "密碼與確認密碼不一致");
			return "redirect:/register";
		}

		String sql = "SELECT id FROM users WHERE email = ?";
		List<Map<String, Object>> user = jdbcTemplate.queryForList(sql, email);

		if (user.size() > 0) {
			redirAttrs.addFlashAttribute("error", "Email已被註冊");
			return "redirect:/register";
		}

		sql = "INSERT INTO users(email, password) VALUES(?, ?)";
		jdbcTemplate.update(sql, email, password);

		return "web/register_success";
	}

	@RequestMapping("/loging")
	public String login(@RequestParam("email") String email, @RequestParam("password") String password,
			RedirectAttributes redirAttrs, HttpSession session) {
		if (email.isEmpty() || password.isEmpty()) {
			redirAttrs.addFlashAttribute("error", "需填入Email、密碼");
			return "redirect:/login";
		}

		String sql = "SELECT id, email, name, mobile, zip, county, district, address FROM users WHERE email = ? AND password = ? LIMIT 1";
		List<Map<String, Object>> user = jdbcTemplate.queryForList(sql, email, password);
		if (user.size() == 0) {
			redirAttrs.addFlashAttribute("error", "Email或密碼錯誤");
			return "redirect:/login";
		}

		session.setAttribute("isLogin", true);
		session.setAttribute("user", user.get(0));

		return "redirect:/";
	}

	@RequestMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/";
	}

	@RequestMapping("/user/orders")
	public String showOrdersPage(HttpSession session, Model model) {
		if (session.getAttribute("isLogin") == null) {
			return "redirect:/login";
		}

		Map<Object, Object> user = (Map<Object, Object>) session.getAttribute("user");
		String sql = "SELECT id, status, order_no, total, shipping, create_time FROM orders WHERE user_id = ?";
		List<Map<String, Object>> orders = jdbcTemplate.queryForList(sql, user.get("id"));

		Map<Object, Object> status = new HashMap<Object, Object>();
		status.put("0", "未付款");
		status.put("1", "已付款");
		status.put("2", "已出貨");
		status.put("3", "已送達(交易完成)");
		status.put("4", "取消訂單");

		model.addAttribute("orders", orders);
		model.addAttribute("status", status);

		return "/web/user_orders";
	}

	@RequestMapping("/user/order/{id}")
	public String showOrderPage(HttpSession session, @PathVariable("id") int id, Model model) {
		if (session.getAttribute("isLogin") == null) {
			return "redirect:/login";
		}

		String sql = "SELECT id, order_no, total, shipping FROM orders WHERE id = ?";
		Order order = jdbcTemplate.queryForObject(sql, new Object[] { id },
				new BeanPropertyRowMapper<Order>(Order.class));

		sql = "SELECT p.id AS product_id, p.folder, p.name, pi.header, od.price, od.quantity FROM order_details AS od LEFT JOIN products AS p ON p.id = od.product_id LEFT JOIN product_images AS pi ON pi.product_id = p.id WHERE od.order_id = ?";
		List<Map<String, Object>> details = jdbcTemplate.queryForList(sql, id);

		model.addAttribute("order", order);
		model.addAttribute("details", details);

		return "/web/user_order";
	}

	@RequestMapping("/user/account")
	public String showAccount(HttpSession session, Model model) {
		if (session.getAttribute("isLogin") == null) {
			return "redirect:/login";
		}

		Map<Object, Object> userData = (Map<Object, Object>) session.getAttribute("user");
		String sql = "SELECT id, email, name, gender, phone, mobile, zip, county, district, address FROM users WHERE id = ?";
		User user = jdbcTemplate.queryForObject(sql, new Object[] { userData.get("id") },
				new BeanPropertyRowMapper<User>(User.class));

		model.addAttribute("user", user);

		return "/web/user_account";
	}

	@RequestMapping("/user/updatePassword")
	public String updatePassword(HttpSession session, @RequestParam("password") String password,
			@RequestParam("new_password") String new_password,
			@RequestParam("confirm_password") String confirm_password, RedirectAttributes redirAttrs) {
		if (password.isEmpty() || new_password.isEmpty() || confirm_password.isEmpty()) {
			redirAttrs.addFlashAttribute("error_password", "需填入舊密碼、新密碼、確認密碼");
			return "redirect:/user/account";
		}
		if (!new_password.equals(confirm_password)) {
			redirAttrs.addFlashAttribute("error_password", "密碼、確認密碼不同");
			return "redirect:/user/account";
		}

		Map<Object, Object> userData = (Map<Object, Object>) session.getAttribute("user");
		int id = (int) userData.get("id");

		String sql = "SELECT password FROM users WHERE id = ? LIMIT 1";
		User user = jdbcTemplate.queryForObject(sql, new Object[] { id }, new BeanPropertyRowMapper<User>(User.class));

		if (!password.equals(user.getPassword())) {
			redirAttrs.addFlashAttribute("error_password", "舊密碼錯誤");
			return "redirect:/user/account";
		}

		sql = "UPDATE users SET password = ? WHERE id = ?";
		jdbcTemplate.update(sql, new_password, id);
		redirAttrs.addFlashAttribute("success_password", "密碼更新成功!");
		return "redirect:/user/account";
	}

	@RequestMapping("/user/update")
	public String updateUser(HttpSession session, @RequestParam("name") String name,
			@RequestParam("gender") String gender, @RequestParam("phone") String phone,
			@RequestParam("mobile") String mobile, @RequestParam("zipcode") String zip,
			@RequestParam("county") String county, @RequestParam("district") String district,
			@RequestParam("address") String address, RedirectAttributes redirAttrs) {

		Map<Object, Object> userData = (Map<Object, Object>) session.getAttribute("user");
		int id = (int) userData.get("id");

		String sql = "UPDATE users SET name = ?, gender = ?, phone = ?, mobile = ?, zip = ?, county = ?, district = ?, address = ? WHERE id = ?";
		jdbcTemplate.update(sql, name, gender, phone, mobile, zip, county, district, address, id);

		redirAttrs.addFlashAttribute("success", "更新成功!");

		return "redirect:/user/account";
	}
}
