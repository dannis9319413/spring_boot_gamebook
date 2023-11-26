package com.gamebook.controller;

import java.util.ArrayList;
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

import com.gamebook.model.News;
import com.gamebook.model.Product;

import jakarta.servlet.http.HttpSession;

@Controller
public class WebController {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@RequestMapping("/")
	public String index(Model model, HttpSession session) {
		String sql_pre = "SELECT p.id, p.folder, p.name, pi.header FROM products p LEFT JOIN product_images pi ON pi.product_id = p.id WHERE p.is_pre = 1 ORDER BY p.create_time DESC";
		String sql_new = "SELECT p.id, p.folder, p.name, p.price, pi.header FROM products p LEFT JOIN product_images pi ON pi.product_id = p.id WHERE p.is_new = 1 ORDER BY p.create_time DESC";
		String sql_special = "SELECT p.id, p.folder, p.name, p.price, FLOOR(p.price*0.5) AS dicount_price, pi.header FROM products p LEFT JOIN product_images pi ON pi.product_id = p.id WHERE p.is_special = 1 ORDER BY p.create_time DESC LIMIT 6";
		String sql_news = "SELECT id, picture, title, SUBSTR(content, 0, 130) AS content FROM news ORDER BY create_time DESC LIMIT 5";
		List<Map<String, Object>> pres = jdbcTemplate.queryForList(sql_pre);
		List<Map<String, Object>> news = jdbcTemplate.queryForList(sql_new);
		List<Map<String, Object>> specials = jdbcTemplate.queryForList(sql_special);
		List<Map<String, Object>> all_news = jdbcTemplate.queryForList(sql_news);
		model.addAttribute("pres", pres);
		model.addAttribute("news", news);
		model.addAttribute("specials", specials);
		model.addAttribute("all_news", all_news);
		return "index";
	}

	@RequestMapping("/about")
	public String about() {
		return "/web/about";
	}

	@RequestMapping("/contact")
	public String contact() {
		return "/web/contact";
	}

	@RequestMapping("/news")
	public String news(Model model) {
		String sql = "SELECT id, picture, title, SUBSTR(content, 0, 130) AS content FROM news ORDER BY create_time DESC LIMIT 5";
		List<Map<String, Object>> all_news = jdbcTemplate.queryForList(sql);
		model.addAttribute("all_news", all_news);
		return "/web/news_list";
	}

	@RequestMapping("/news/{id}")
	public String individual_news(@PathVariable("id") int id, Model model) {
		String sql = "SELECT id, picture, title, content, create_time FROM news WHERE id = ? LIMIT 1";
		News news = jdbcTemplate.queryForObject(sql, new Object[] { id }, new BeanPropertyRowMapper<News>(News.class));
		model.addAttribute("news", news);
		return "/web/news";
	}

	@RequestMapping("/product")
	public String product(@RequestParam("category") String category, Model model) {
		String sql = "SELECT p.id, p.folder, p.name, p.price, pi.header FROM products p LEFT JOIN product_images pi ON pi.product_id = p.id ";
		List<Object> params = new ArrayList<Object>();
		if (!category.trim().isEmpty()) {
			sql += "WHERE p.product_category_id = ?";
			params.add(category);
		}
		sql += "LIMIT 6";
		List<Map<String, Object>> products = jdbcTemplate.queryForList(sql, params.toArray());
		model.addAttribute("products", products);
		return "/web/product_list";
	}

	@RequestMapping("/product/{id}")
	public String individual_product(@PathVariable("id") int id, HttpSession session, Model model) {
		String sql = "SELECT p.id, p.folder, p.name, p.price, pi.header, pi.picture_1, pi.picture_2, pi.picture_3, pi.picture_4 FROM products p LEFT JOIN product_images pi ON pi.product_id = p.id WHERE p.id = ? LIMIT 1";
		Product product = jdbcTemplate.queryForObject(sql, new Object[] { id },
				new BeanPropertyRowMapper<Product>(Product.class));
		String[] oring_images = product.getImages();
		ArrayList<String> images = new ArrayList<String>();
		Boolean isInCart = false;

		for (String image : oring_images) {
			if (image.isEmpty()) {
				continue;
			}
			images.add(image);
		}

		if (session.getAttribute("cart") != null) {
			List<Map<Object, Object>> cart = (List<Map<Object, Object>>) session.getAttribute("cart");
			for (Map<Object, Object> item : cart) {
				if (item.get("id").equals(id)) {
					isInCart = true;
					break;
				}
			}
		}

		model.addAttribute("product", product);
		model.addAttribute("images", images);
		model.addAttribute("isInCart", isInCart);
		return "/web/product";
	}

}
