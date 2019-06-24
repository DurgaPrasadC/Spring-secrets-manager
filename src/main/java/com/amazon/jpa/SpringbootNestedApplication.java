package com.amazon.jpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaAuditing
@ComponentScan(basePackages = {"com.amazon.jpa.model","com.amazon.jpa.controller","com.amazon.jpa.execption","com.amazon.jpa.aop","com.amazon.jpa.config"})
@EnableJpaRepositories(basePackages = "com.amazon.jpa.repository")
public class SpringbootNestedApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootNestedApplication.class, args);
	}
}
