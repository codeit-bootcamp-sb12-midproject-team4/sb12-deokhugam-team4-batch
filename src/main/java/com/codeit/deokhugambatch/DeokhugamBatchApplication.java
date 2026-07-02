package com.codeit.deokhugambatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("com.codeit.deokhugamcommon.domain")
public class DeokhugamBatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeokhugamBatchApplication.class, args);
	}

}
