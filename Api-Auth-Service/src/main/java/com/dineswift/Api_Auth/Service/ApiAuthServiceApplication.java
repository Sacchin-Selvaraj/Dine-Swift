package com.dineswift.Api_Auth.Service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiAuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiAuthServiceApplication.class, args);
	}

}
