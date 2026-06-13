package com.shopwise.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
public class ShopwiseUserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopwiseUserServiceApplication.class, args);
	}

}
