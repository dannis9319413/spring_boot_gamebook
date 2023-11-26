package com.gamebook.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gamebook.model.News;
import com.gamebook.model.User;

import jakarta.servlet.ServletContext;

@Controller
public class AdminUserController {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	ServletContext context;

	@GetMapping("/admin/user/list")
	public String showListPage(Model model) {
		String sql = "SELECT id, email, create_time FROM users ORDER BY create_time DESC";
		List<Map<String, Object>> users = jdbcTemplate.queryForList(sql);
		model.addAttribute("users", users);
		return "admin/user/list";
	}

	@GetMapping("/admin/user/create")
	public String showCreatePage() {
		return "admin/user/create";
	}

	@PostMapping("/admin/user/store")
	public String storeNews(@RequestParam("email") String email, @RequestParam("password") String password,
			@RequestParam("confirm_password") String confirm_password, @RequestParam("name") String name,
			@RequestParam("gender") String gender, @RequestParam("phone") String phone,
			@RequestParam("mobile") String mobile, @RequestParam("zipcode") String zip,
			@RequestParam("county") String county, @RequestParam("district") String district,
			@RequestParam("address") String address, RedirectAttributes redirAttrs) {
		if (email.isEmpty() || password.isEmpty() || confirm_password.isEmpty()) {
			redirAttrs.addFlashAttribute("error", "需填入Email、密碼、確認密碼");
			return "redirect:/admin/user/create";
		}
		if (!password.equals(confirm_password)) {
			redirAttrs.addFlashAttribute("error", "密碼、確認密碼不同");
			return "redirect:/admin/user/create";
		}

		String sql = "INSERT INTO users(email, password, name, gender, phone, mobile, zip, county, district, address) VALUES(?,?,?,?,?,?,?,?,?,?)";
		jdbcTemplate.update(sql, email, password, name, gender, phone, mobile, zip, county, district, address);

		redirAttrs.addFlashAttribute("success", "新增成功");

		return "redirect:/admin/user/list";
	}

	@GetMapping("/admin/user/edit/{id}")
	public String showEditPage(@PathVariable("id") int id, Model model) {
		String sql = "SELECT id, email, password, name, gender, phone, mobile, zip, county, district, address FROM users WHERE id = ? LIMIT 1";
		User user = jdbcTemplate.queryForObject(sql, new Object[] { id }, new BeanPropertyRowMapper<User>(User.class));
		model.addAttribute("user", user);
		return "admin/user/edit";
	}

	@RequestMapping("/admin/user/updatePassword/{id}")
	public String updatePassword(@PathVariable("id") int id, @RequestParam("password") String password,
			@RequestParam("new_password") String new_password,
			@RequestParam("confirm_password") String confirm_password, RedirectAttributes redirAttrs) {
		if (password.isEmpty() || new_password.isEmpty() || confirm_password.isEmpty()) {
			redirAttrs.addFlashAttribute("error_password", "需填入舊密碼、新密碼、確認密碼");
			return "redirect:/admin/user/edit/" + id;
		}
		if (!new_password.equals(confirm_password)) {
			redirAttrs.addFlashAttribute("error_password", "密碼、確認密碼不同");
			return "redirect:/admin/user/edit/" + id;
		}

		String sql = "SELECT password FROM users WHERE id = ? LIMIT 1";
		User user = jdbcTemplate.queryForObject(sql, new Object[] { id }, new BeanPropertyRowMapper<User>(User.class));

		if (!password.equals(user.getPassword())) {
			redirAttrs.addFlashAttribute("error_password", "舊密碼錯誤");
			return "redirect:/admin/user/edit/" + id;
		}

		sql = "UPDATE users SET password = ? WHERE id = ?";
		jdbcTemplate.update(sql, new_password, id);
		redirAttrs.addFlashAttribute("success_password", "密碼更新成功!");
		return "redirect:/admin/user/edit/" + id;
	}

	@RequestMapping("/admin/user/update/{id}")
	public String updateNews(@PathVariable("id") int id, @RequestParam("email") String email,
			@RequestParam("name") String name, @RequestParam("gender") String gender,
			@RequestParam("phone") String phone, @RequestParam("mobile") String mobile,
			@RequestParam("zipcode") String zip, @RequestParam("county") String county,
			@RequestParam("district") String district, @RequestParam("address") String address,
			RedirectAttributes redirAttrs) {
		if (email.isEmpty()) {
			redirAttrs.addFlashAttribute("error", "需填入Email");
			return "redirect:/admin/user/edit/" + id;
		}

		String sql = "UPDATE users SET email = ?, name = ?, gender = ?, phone = ?, mobile = ?, zip = ?, county = ?, district = ?, address = ? WHERE id = ?";
		jdbcTemplate.update(sql, email, name, gender, phone, mobile, zip, county, district, address, id);

		redirAttrs.addFlashAttribute("success", "更新成功!");

		return "redirect:/admin/user/edit/" + id;
	}

	@RequestMapping("/admin/user/delete/{id}")
	public String deleteNews(@PathVariable("id") int id) {
		jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
		return "redirect:/admin/user/list";
	}
}
