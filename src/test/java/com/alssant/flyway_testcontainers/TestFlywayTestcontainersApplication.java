package com.alssant.flyway_testcontainers;

import org.springframework.boot.SpringApplication;

public class TestFlywayTestcontainersApplication {

	public static void main(String[] args) {
		SpringApplication.from(FlywayTestcontainersApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
