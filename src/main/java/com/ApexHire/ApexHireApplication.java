package com.ApexHire;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class ApexHireApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApexHireApplication.class, args);
	}

}
