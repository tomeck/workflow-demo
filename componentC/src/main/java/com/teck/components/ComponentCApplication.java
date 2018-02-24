package com.teck.components;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;

@SpringBootApplication(exclude = {EmbeddedServletContainerAutoConfiguration.class,WebMvcAutoConfiguration.class})
@EnableAutoConfiguration
public class ComponentCApplication {

	public static void main(String[] args) {
		SpringApplication.run(ComponentCApplication.class, args);
	}
}
