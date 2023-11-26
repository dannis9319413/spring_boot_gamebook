package com.gamebook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.gamebook","com.gamebook.model","com.gamebook.controller","com.gamebook.service","com.gamebook.repository"})
@EntityScan({"com.gamebook","com.gamebook.model","com.gamebook.controller","com.gamebook.service","com.gamebook.repository"})
public class GamebookApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(GamebookApplication.class, args);
	}

}