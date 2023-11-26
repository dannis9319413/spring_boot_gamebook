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

import jakarta.servlet.ServletContext;

@Controller
public class AdminNewsController {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	ServletContext context;

	@GetMapping("/admin/news/list")
	public String showListPage(Model model) {
		String sql = "SELECT id, picture, title, update_time FROM news ORDER BY update_time DESC";
		List<Map<String, Object>> all_news = jdbcTemplate.queryForList(sql);
		model.addAttribute("all_news", all_news);
		return "admin/news/list";
	}

	@GetMapping("/admin/news/create")
	public String showCreatePage() {
		return "admin/news/create";
	}

	@PostMapping("/admin/news/store")
	public String storeNews(@RequestParam(name = "picture", required = false) MultipartFile picture,
			@RequestParam("title") String title, @RequestParam("content") String content,
			RedirectAttributes redirAttrs) {
		if (title.isEmpty() || content.isEmpty()) {
			redirAttrs.addFlashAttribute("error", "需填入 標題、內容");
			return "redirect:/admin/news/create";
		}
		String pictureName = "";
		if (!picture.isEmpty()) {
			try {
				pictureName = picture.getOriginalFilename();
				byte[] bytes = picture.getBytes();
				Path path = Paths.get(context.getRealPath("uploads/news/") + pictureName);
				Path uploadPath = Paths.get(context.getRealPath("uploads/news/"));
				if (!Files.exists(uploadPath)) {
					Files.createDirectories(uploadPath);
				}
				Files.write(path, bytes);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		jdbcTemplate.update("INSERT INTO news(picture, title, content) VALUES(?, ?, ?)", pictureName, title, content);

		return "redirect:/admin/news/list";
	}

	@GetMapping("/admin/news/edit/{id}")
	public String showEditPage(@PathVariable("id") int id, Model model) {
		String sql = "SELECT id, picture, title, content, create_time, update_time FROM news WHERE id = ? LIMIT 1";
		News news = jdbcTemplate.queryForObject(sql, new Object[] { id }, new BeanPropertyRowMapper<News>(News.class));
		model.addAttribute("news", news);
		return "admin/news/edit";
	}

	@RequestMapping("/admin/news/update/{id}")
	public String updateNews(@PathVariable("id") int id,
			@RequestParam(name = "new_picture", required = false) MultipartFile new_picture,
			@RequestParam("picture") String picture, @RequestParam("title") String title,
			@RequestParam("content") String content, RedirectAttributes redirAttrs) {
		if (title.isEmpty() || content.isEmpty()) {
			redirAttrs.addFlashAttribute("error", "需填入 標題、內容");
			return "redirect:/admin/news/edit/" + id;
		}
		String pictureName = picture;
		if (new_picture != null && !new_picture.isEmpty()) {
			try {
				pictureName = new_picture.getOriginalFilename();
				byte[] bytes = new_picture.getBytes();
				Path path = Paths.get(context.getRealPath("uploads/news/") + pictureName);
				Path uploadPath = Paths.get(context.getRealPath("uploads/news/"));
				if (!Files.exists(uploadPath)) {
					Files.createDirectories(uploadPath);
				}
				Files.write(path, bytes);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		jdbcTemplate.update("UPDATE news SET picture = ?, title = ?, content = ? WHERE id = ?", pictureName, title,
				content, id);
		redirAttrs.addFlashAttribute("success", "更新成功!");
		return "redirect:/admin/news/edit/" + id;
	}

	@RequestMapping("/admin/news/delete/{id}")
	public String deleteNews(@PathVariable("id") int id) {
		jdbcTemplate.update("DELETE FROM news WHERE id = ?", id);
		return "redirect:/admin/news/list";
	}
}
