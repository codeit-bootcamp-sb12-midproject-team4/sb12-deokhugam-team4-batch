package com.codeit.deokhugambatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan({
	"com.codeit.deokhugamcommon.domain",
	"com.codeit.deokhugambatch.user.entity"
})
@EnableScheduling
public class DeokhugamBatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeokhugamBatchApplication.class, args);
	}

}
