package com.example.camel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.camel")
public class ApacheCamelMqExample {

	public static void main(String[] args) {
		SpringApplication.run(ApacheCamelMqExample.class, args);
	}

}
