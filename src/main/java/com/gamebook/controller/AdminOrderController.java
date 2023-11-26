package com.gamebook.controller;

import java.util.HashMap;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gamebook.model.Order;

import jakarta.servlet.ServletContext;

@Controller
public class AdminOrderController {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	ServletContext context;

	@GetMapping("/admin/order/list")
	public String showListPage(Model model) {
		String sql = "SELECT id, status, order_no, create_time, update_time FROM orders ORDER BY create_time DESC";
		List<Map<String, Object>> orders = jdbcTemplate.queryForList(sql);
		HashMap<String, String> status = new HashMap<String, String>();
		status.put("0", "未付款");
		status.put("1", "已付款");
		status.put("2", "已出貨");
		status.put("3", "已送達(交易完成)");
		status.put("4", "取消訂單");
		model.addAttribute("orders", orders);
		model.addAttribute("status", status);
		return "admin/order/list";
	}

	@GetMapping("/admin/order/edit/{id}")
	public String showEditPage(@PathVariable("id") int id, Model model) {
		String sql = "SELECT id, status, order_no, total, shipping, name, mobile, zip, county, district, address FROM orders WHERE id = ? LIMIT 1";
		Order order = jdbcTemplate.queryForObject(sql, new Object[] { id },
				new BeanPropertyRowMapper<Order>(Order.class));
		model.addAttribute("order", order);
		return "admin/order/edit";
	}

	@RequestMapping("/admin/order/update/{id}")
	public String updateNews(@PathVariable("id") int id, @RequestParam("status") String status,
			@RequestParam("total") String total, @RequestParam("shipping") String shipping,
			@RequestParam("name") String name, @RequestParam("mobile") String mobile,
			@RequestParam("zipcode") String zip, @RequestParam("county") String county,
			@RequestParam("district") String district, @RequestParam("address") String address,
			RedirectAttributes redirAttrs) {
		if (total.isEmpty() || shipping.isEmpty()) {
			redirAttrs.addFlashAttribute("error", "需填入商品總額、運費");
			return "redirect:/admin/order/edit/" + id;
		}

		jdbcTemplate.update(
				"UPDATE orders SET status = ?, total = ?, shipping = ?, name = ?, mobile = ?, zip = ?, county = ?, district = ?, address = ? WHERE id = ?",
				status, total, shipping, name, mobile, zip, county, district, address, id);
		redirAttrs.addFlashAttribute("success", "更新成功!");
		return "redirect:/admin/order/edit/" + id;
	}

	@RequestMapping("/admin/order/delete/{id}")
	public String deleteNews(@PathVariable("id") int id) {
		jdbcTemplate.update("DELETE FROM orders WHERE id = ?", id);
		return "redirect:/admin/order/list";
	}
}
