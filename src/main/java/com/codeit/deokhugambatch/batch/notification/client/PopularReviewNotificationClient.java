package com.codeit.deokhugambatch.batch.notification.client;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.codeit.deokhugambatch.dashboard.common.model.DashboardPeriod;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PopularReviewNotificationClient {

	private final RestClient restClient;

	public void publish(List<UUID> reviewIds, DashboardPeriod period) {
		restClient.post()
			.uri("/api/notifications//internal/popular-reviews")
			.body(new PopularReviewNotificationRequest(
				reviewIds,
				period.name()
			))
			.retrieve()
			.toBodilessEntity();
	}

	public record PopularReviewNotificationRequest(
		List<UUID> reviewIds,
		String period
	) {
	}
}
