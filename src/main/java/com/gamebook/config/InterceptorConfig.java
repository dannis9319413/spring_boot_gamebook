package com.gamebook.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.gamebook.interceptor.WebInterceptor;

@Configuration
@EnableWebMvc
public class InterceptorConfig implements WebMvcConfigurer {
	@Autowired
	private WebInterceptor webInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(webInterceptor).addPathPatterns("/**");
	}
}
