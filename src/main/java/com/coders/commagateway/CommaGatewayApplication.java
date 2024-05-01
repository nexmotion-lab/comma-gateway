package com.coders.commagateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class CommaGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommaGatewayApplication.class, args);
	}

}
