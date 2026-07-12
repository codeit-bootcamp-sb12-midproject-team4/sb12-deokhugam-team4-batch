package com.codeit.deokhugambatch.batch.notification.client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ApiClientConfig {

	@Bean
	public RestClient restClient(
		@Value("${deokhugam-batch.api.base-url}") String baseUrl
	) {
		return RestClient.builder()
			.baseUrl(baseUrl)
			.build();
	}
}
