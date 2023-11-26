package com.gamebook.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gamebook.model.Product;

import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import jakarta.servlet.ServletContext;

@Controller
public class AdminProductController {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	ServletContext context;

	@GetMapping("/admin/product/list")
	public String showListPage(Model model) {
		String sql = "SELECT products.id, products.folder, products.name, products.create_time, product_images.header "
				+ "FROM products " + "LEFT JOIN product_images ON product_images.product_id = products.id "
				+ "ORDER BY products.update_time DESC";
		List<Map<String, Object>> products = jdbcTemplate.queryForList(sql);
		model.addAttribute("products", products);
		return "admin/product/list";
	}

	@GetMapping("/admin/product/create")
	public String showCreatePage(Model model) {
		String sql = "SELECT id, category FROM categories";
		List<Map<String, Object>> categories = jdbcTemplate.queryForList(sql);
		model.addAttribute("categories", categories);
		return "admin/product/create";
	}

	@PostMapping("/admin/product/store")
	public String storeNews(@RequestParam(name = "folder") String folder,
			@RequestParam(name = "category_id") int categoryId,
			@RequestParam(name = "header", required = false) MultipartFile header,
			@RequestParam(name = "picture_1", required = false) MultipartFile picture_1,
			@RequestParam(name = "picture_2", required = false) MultipartFile picture_2,
			@RequestParam(name = "picture_3", required = false) MultipartFile picture_3,
			@RequestParam(name = "picture_4", required = false) MultipartFile picture_4,
			@RequestParam("name") String name, @RequestParam("description") String description,
			@RequestParam("price") String price, @RequestParam("is_pre") String is_pre,
			@RequestParam("is_new") String is_new, @RequestParam("is_special") String is_special,
			RedirectAttributes redirAttrs) {
		if (folder.isEmpty() || name.isEmpty() || description.isEmpty() || price.isEmpty()) {
			redirAttrs.addFlashAttribute("error", "請輸入資料夾名稱、遊戲名稱、遊戲描述、價格");
			return "redirect:/admin/product/create";
		}

		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			public java.sql.PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
				int i = 0;
				String sql = "INSERT INTO products(folder, product_category_id, name, description, price, is_pre, is_new, is_special) VALUES(?,?,?,?,?,?,?,?)";
				java.sql.PreparedStatement ps = conn.prepareStatement(sql);
				ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				ps.setString(++i, folder);
				ps.setInt(++i, categoryId);
				ps.setString(++i, name);
				ps.setString(++i, description);
				ps.setString(++i, price);
				ps.setString(++i, is_pre);
				ps.setString(++i, is_new);
				ps.setString(++i, is_special);
				return ps;
			}
		}, keyHolder);

		int productId = keyHolder.getKey().intValue();

		String headerName = "";

		if (!header.isEmpty()) {
			try {
				headerName = header.getOriginalFilename();
				byte[] bytes = header.getBytes();
				Path path = Paths.get(context.getRealPath("uploads/product/" + folder + "/") + headerName);
				Path uploadPath = Paths.get(context.getRealPath("uploads/product/" + folder + "/"));
				if (!Files.exists(uploadPath)) {
					Files.createDirectories(uploadPath);
				}
				Files.write(path, bytes);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		MultipartFile[] files = { picture_1, picture_2, picture_3, picture_4 };
		String[] fileNames = new String[4];
		String pictureName = "";
		for (int i = 0; i < files.length; i++) {
			if (files[i].isEmpty()) {
				fileNames[i] = "";
				continue;
			}
			try {
				pictureName = files[i].getOriginalFilename();
				byte[] bytes = files[i].getBytes();
				Path path = Paths.get(context.getRealPath("uploads/product/" + folder + "/") + pictureName);
				Path uploadPath = Paths.get(context.getRealPath("uploads/product/" + folder + "/"));
				if (!Files.exists(uploadPath)) {
					Files.createDirectories(uploadPath);
				}
				Files.write(path, bytes);
				fileNames[i] = pictureName;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		String sql = "INSERT INTO product_images(product_id, header, picture_1, picture_2, picture_3, picture_4) VALUES("
				+ productId + ", '" + headerName + "'";
		for (String fileName : fileNames) {
			sql += ",'" + fileName + "'";
		}
		sql += ")";

		jdbcTemplate.update(sql);

		return "redirect:/admin/product/list";
	}

	@GetMapping("/admin/product/edit/{id}")
	public String showEditPage(@PathVariable("id") int id, Model model) {
		String sql = "SELECT products.id, products.folder, products.product_category_id, products.name, products.description, products.price, products.is_pre, products.is_new, products.is_special, product_images.header, product_images.picture_1, product_images.picture_2, product_images.picture_3, product_images.picture_4 "
				+ "FROM products LEFT JOIN product_images ON product_images.product_id = products.id "
				+ "WHERE products.id = ? LIMIT 1";
		Product product = jdbcTemplate.queryForObject(sql, new Object[] { id },
				new BeanPropertyRowMapper<Product>(Product.class));

		String[] images = product.getImages();

		model.addAttribute("product", product);
		model.addAttribute("images", images);

		return "admin/product/edit";
	}

	@RequestMapping("/admin/product/update/{id}")
	public String updateNews(@PathVariable("id") int id, @RequestParam(name = "category_id") int categoryId,
			@RequestParam(name = "header", required = false) MultipartFile header,
			@RequestParam(name = "picture_1", required = false) MultipartFile picture_1,
			@RequestParam(name = "picture_2", required = false) MultipartFile picture_2,
			@RequestParam(name = "picture_3", required = false) MultipartFile picture_3,
			@RequestParam(name = "picture_4", required = false) MultipartFile picture_4,
			@RequestParam("name") String name, @RequestParam("description") String description,
			@RequestParam("price") String price, @RequestParam("is_pre") String is_pre,
			@RequestParam("is_new") String is_new, @RequestParam("is_special") String is_special,
			RedirectAttributes redirAttrs) {
		if (name.isEmpty() || description.isEmpty() || price.isEmpty()) {
			redirAttrs.addFlashAttribute("error", "需輸入遊戲名稱、遊戲描述、價格");
			return "redirect:/admin/product/edit/" + id;
		}

		String sql = "SELECT products.id, products.folder, products.product_category_id, products.name, products.description, products.price, products.is_pre, products.is_new, products.is_special, product_images.header, product_images.picture_1, product_images.picture_2, product_images.picture_3, product_images.picture_4 "
				+ "FROM products LEFT JOIN product_images ON product_images.product_id = products.id "
				+ "WHERE products.id = ? LIMIT 1";
		Product product = jdbcTemplate.queryForObject(sql, new Object[] { id },
				new BeanPropertyRowMapper<Product>(Product.class));

		String headerName = product.getHeader();

		if (!header.isEmpty()) {
			try {
				headerName = header.getOriginalFilename();
				byte[] bytes = header.getBytes();
				Path path = Paths.get(context.getRealPath("uploads/product/" + product.getFolder() + "/") + headerName);
				Path uploadPath = Paths.get(context.getRealPath("uploads/product/" + product.getFolder() + "/"));
				if (!Files.exists(uploadPath)) {
					Files.createDirectories(uploadPath);
				}
				Files.write(path, bytes);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		MultipartFile[] files = { picture_1, picture_2, picture_3, picture_4 };
		String[] old_pictures = { product.getPicture_1(), product.getPicture_2(), product.getPicture_3(),
				product.getPicture_4() };
		String[] fileNames = new String[4];
		String pictureName = "";
		for (int i = 0; i < files.length; i++) {
			if (files[i].isEmpty()) {
				fileNames[i] = old_pictures[i];
				continue;
			}
			try {
				pictureName = files[i].getOriginalFilename();
				byte[] bytes = files[i].getBytes();
				Path path = Paths
						.get(context.getRealPath("uploads/product/" + product.getFolder() + "/") + pictureName);
				Path uploadPath = Paths.get(context.getRealPath("uploads/product/" + product.getFolder() + "/"));
				if (!Files.exists(uploadPath)) {
					Files.createDirectories(uploadPath);
				}
				Files.write(path, bytes);
				fileNames[i] = pictureName;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		sql = "UPDATE products SET product_category_id = ?, name = ?, description = ?, price = ?, is_pre = ?, is_new = ?, is_special = ? WHERE id = ?";
		jdbcTemplate.update(sql, categoryId, name, description, price, is_pre, is_new, is_special, id);

		sql = "UPDATE product_images SET header = ?, picture_1 = ?, picture_2 = ?, picture_3 = ?, picture_4 = ? WHERE product_id = ?";
		jdbcTemplate.update(sql, headerName, fileNames[0], fileNames[1], fileNames[2], fileNames[3], id);

		redirAttrs.addFlashAttribute("success", "更新成功!");
		return "redirect:/admin/product/edit/" + id;
	}

	@RequestMapping("/admin/product/delete/{id}")
	public String deleteNews(@PathVariable("id") int id) {
		jdbcTemplate.update("DELETE FROM products WHERE id = ?", id);
		jdbcTemplate.update("DELETE FROM product_images WHERE product_id = ?", id);
		return "redirect:/admin/product/list";
	}
}
