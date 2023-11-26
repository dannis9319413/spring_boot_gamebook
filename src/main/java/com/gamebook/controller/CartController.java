package com.gamebook.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gamebook.model.Product;

import jakarta.servlet.http.HttpSession;

@Controller
public class CartController {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@RequestMapping("/cart")
	public String cart(HttpSession session, Model model) {
		if (session.getAttribute("cart") != null) {
			List<Map<Object, Object>> cart = (List<Map<Object, Object>>) session.getAttribute("cart");
			List<Object> products = new ArrayList<>();
			Map<Object, Object> quantities = new HashMap<>();
			Map<Object, Object> amount = new HashMap<>();
			int total = 0;

			for (Map<Object, Object> item : cart) {
				String sql = "SELECT p.id, p.folder, p.name, p.price, pi.header FROM products p LEFT JOIN product_images pi ON pi.product_id = p.id WHERE p.id = ?";
				Product product = jdbcTemplate.queryForObject(sql, new Object[] { item.get("id") },
						new BeanPropertyRowMapper<Product>(Product.class));
				products.add(product);
				quantities.put(item.get("id"), item.get("quantity"));
				amount.put(item.get("id"), product.getPrice() * (int) item.get("quantity"));
				total += product.getPrice() * (int) item.get("quantity");
			}

			model.addAttribute("products", products);
			model.addAttribute("quantities", quantities);
			model.addAttribute("amount", amount);
			model.addAttribute("total", total);
		}

		return "/web/cart";
	}

	@RequestMapping("/cart/add/{id}")
	public String addToCart(@PathVariable("id") int id, @RequestParam("quantity") int quantity, HttpSession session) {
		ArrayList<HashMap<Object, Object>> cart = new ArrayList<HashMap<Object, Object>>();
		if (session.getAttribute("cart") != null) {
			cart = (ArrayList<HashMap<Object, Object>>) session.getAttribute("cart");
		}

		String sql = "SELECT price FROM products WHERE id = ?";
		Product product = jdbcTemplate.queryForObject(sql, new Object[] { id },
				new BeanPropertyRowMapper<Product>(Product.class));

		HashMap<Object, Object> item = new HashMap<Object, Object>();
		item.put("id", id);
		item.put("price", product.getPrice());
		item.put("quantity", quantity);
		cart.add(item);

		session.setAttribute("cart", cart);
		session.setAttribute("cartSize", cart.size());

		return "redirect:/cart";
	}

	@RequestMapping("/cart/update")
	public String updateCart(HttpSession session, @RequestParam("quantity[]") List<Integer> quantities) {
		List<Map<Object, Object>> cart = (List<Map<Object, Object>>) session.getAttribute("cart");
		List<Map<Object, Object>> newCart = new ArrayList<Map<Object, Object>>();
		int i = 0;

		for (Map<Object, Object> item : cart) {
			item.put("quantity", quantities.get(i));
			newCart.add(item);
			i++;
		}

		session.setAttribute("cart", newCart);

		return "redirect:/cart";
	}

	@RequestMapping("/cart/delete/{id}")
	public String deleteFromCart(@PathVariable("id") int id, HttpSession session) {
		List<Map<Object, Object>> new_cart = new ArrayList<Map<Object, Object>>();
		List<Map<Object, Object>> cart = (List<Map<Object, Object>>) session.getAttribute("cart");

		for (Map<Object, Object> item : cart) {
			if (item.get("id").equals(id)) {
				continue;
			}
			new_cart.add(item);
		}

		session.setAttribute("cart", new_cart);
		session.setAttribute("cartSize", new_cart.size());

		return "redirect:/cart";
	}

	@RequestMapping("/checkout/receiver")
	public String showReceiverPage(HttpSession session) {
		if (session.getAttribute("isLogin") == null) {
			return "redirect:/login";
		}
		return "/web/checkout_receiver";
	}

	@RequestMapping("/checkout/delivery")
	public String showDeliveryPage(@RequestParam("email") String email, @RequestParam("name") String name,
			@RequestParam("mobile") String mobile, @RequestParam("zipcode") String zipcode,
			@RequestParam("county") String county, @RequestParam("district") String district,
			@RequestParam("address") String address, HttpSession session, Model model, RedirectAttributes redirAttrs) {
		if (session.getAttribute("isLogin") == null) {
			return "redirect:/login";
		}
		if (email.isEmpty() || name.isEmpty() || mobile.isEmpty() || zipcode.isEmpty() || county.isEmpty()
				|| district.isEmpty() || address.isEmpty()) {
			redirAttrs.addFlashAttribute("error", "需完整填入收件人資料");
			return "redirect:/checkout/receiver";
		}

		Map<Object, Object> receiver = new HashMap<>();
		receiver.put("email", email);
		receiver.put("name", name);
		receiver.put("mobile", mobile);
		receiver.put("zip", zipcode);
		receiver.put("county", county);
		receiver.put("district", district);
		receiver.put("address", address);
		session.setAttribute("receiver", receiver);

		List<Map<Object, Object>> cart = (List<Map<Object, Object>>) session.getAttribute("cart");
		int total = 0;

		for (Map<Object, Object> item : cart) {
			String sql = "SELECT price FROM products WHERE id = ?";
			Product product = jdbcTemplate.queryForObject(sql, new Object[] { item.get("id") },
					new BeanPropertyRowMapper<Product>(Product.class));
			total += product.getPrice() * (int) item.get("quantity");
		}

		model.addAttribute("total", total);

		return "/web/checkout_delivery";
	}

	@RequestMapping("/checkout/confirm")
	public String showConfirmPage(HttpSession session, @RequestParam("delivery") String delivery, Model model) {
		if (session.getAttribute("isLogin") == null) {
			return "redirect:/login";
		}

		List<Map<Object, Object>> cart = (List<Map<Object, Object>>) session.getAttribute("cart");
		List<Object> products = new ArrayList<>();
		Map<Object, Object> quantities = new HashMap<>();
		int total = 0;
		int shipping = 0;

		for (Map<Object, Object> item : cart) {
			String sql = "SELECT p.id, p.folder, p.name, p.price, pi.header FROM products p LEFT JOIN product_images pi ON pi.product_id = p.id WHERE p.id = ?";
			Product product = jdbcTemplate.queryForObject(sql, new Object[] { item.get("id") },
					new BeanPropertyRowMapper<Product>(Product.class));
			products.add(product);
			quantities.put(item.get("id"), item.get("quantity"));
			total += product.getPrice() * (int) item.get("quantity");
		}

		if (total < 3000) {
			shipping = (delivery.equals("shop")) ? 80 : 150;
		}

		session.setAttribute("products", products);
		session.setAttribute("quantities", quantities);
		session.setAttribute("total", total);
		session.setAttribute("delivery", delivery);
		session.setAttribute("shipping", shipping);

		return "/web/checkout_confirm";
	}

	@RequestMapping("/checkout/create_order")
	public String createOrder(HttpSession session) {
		if (session.getAttribute("isLogin") == null) {
			return "redirect:/login";
		}

		Map<Object, Object> user = (Map<Object, Object>) session.getAttribute("user");
		Map<Object, Object> receiver = (Map<Object, Object>) session.getAttribute("receiver");

		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			public java.sql.PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
				int i = 0;
				String sql = "INSERT INTO orders(order_no, user_id, total, shipping, name, mobile, zip, county, district, address) VALUES(?,?,?,?,?,?,?,?,?,?)";
				java.sql.PreparedStatement ps = conn.prepareStatement(sql);
				ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				ps.setString(++i, this.getOrderNo());
				ps.setInt(++i, (int) user.get("id"));
				ps.setInt(++i, (int) session.getAttribute("total"));
				ps.setInt(++i, (int) session.getAttribute("shipping"));
				ps.setString(++i, (String) receiver.get("name"));
				ps.setString(++i, (String) receiver.get("mobile"));
				ps.setString(++i, (String) receiver.get("zip"));
				ps.setString(++i, (String) receiver.get("county"));
				ps.setString(++i, (String) receiver.get("district"));
				ps.setString(++i, (String) receiver.get("address"));
				return ps;
			}

			private String getOrderNo() {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				Date date = new Date();
				String orderNo = "GB" + sdf.format(date);
				return orderNo;
			}
		}, keyHolder);

		int orderId = keyHolder.getKey().intValue();

		List<Map<Object, Object>> cart = (List<Map<Object, Object>>) session.getAttribute("cart");
		List<Object[]> details = new ArrayList<>();

		String sql = "INSERT INTO order_details (order_id, product_id, price, quantity) VALUES (?, ?, ?, ?)";

		for (Map<Object, Object> item : cart) {
			details.add(new Object[] { orderId, item.get("id"), item.get("price"), item.get("quantity") });
		}

		jdbcTemplate.batchUpdate(sql, details);

		session.removeAttribute("cart");
		session.removeAttribute("cartSize");
		session.removeAttribute("products");
		session.removeAttribute("quantities");
		session.removeAttribute("total");
		session.removeAttribute("receiver");
		session.removeAttribute("delivery");
		session.removeAttribute("shipping");

		return "/web/checkout_result";
	}

}
