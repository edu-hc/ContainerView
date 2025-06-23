package com.ftc.containerView;

import com.ftc.containerView.controller.ContainerController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class ContainerViewApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(ContainerViewApplication.class, args);

		ContainerController containerController;
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(ContainerViewApplication.class);
	}
}
