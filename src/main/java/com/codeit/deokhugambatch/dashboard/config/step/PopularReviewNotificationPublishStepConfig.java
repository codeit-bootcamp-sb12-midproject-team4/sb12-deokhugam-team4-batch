package com.codeit.deokhugambatch.dashboard.config.step;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import com.codeit.deokhugambatch.batch.notification.client.PopularReviewNotificationClient;
import com.codeit.deokhugambatch.dashboard.common.model.DashboardPeriod;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// dashboard에서 사용하는 스타일에 맞게 return stepBuilder 사용
@Slf4j
@Configuration
@RequiredArgsConstructor
public class PopularReviewNotificationPublishStepConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;
	private final JdbcTemplate jdbcTemplate;
	private final PopularReviewNotificationClient popularReviewNotificationClient;

	@Bean
	public Step popularReviewNotificationPublishStep() {

		return new StepBuilder("popularReviewNotificationPublishStep", jobRepository)
			.tasklet((contribution, chunkContext) -> {

				Map<String, Object> jobContext =
					chunkContext.getStepContext().getJobExecutionContext();

				Long datasetId = (Long)jobContext.get("datasetId");
				DashboardPeriod period =
					(DashboardPeriod)jobContext.get("period");

				List<UUID> reviewIds = jdbcTemplate.query(
					"""
						SELECT review_id
						FROM popular_review
						WHERE dataset_id = ?
						  AND period = ?
						ORDER BY ranking
						""",
					(rs, rowNum) -> bytesToUuid(rs.getBytes("review_id")),
					datasetId,
					period.name()
				);

				if (reviewIds.isEmpty()) {
					log.info(
						"[PopularReviewNotificationPublishStep] no popular reviews to publish - datasetId={}, period={}",
						datasetId,
						period
					);
					return RepeatStatus.FINISHED;
				}

				popularReviewNotificationClient.publish(reviewIds, period);

				log.info(
					"[PopularReviewNotificationPublishStep] popular review notification published - datasetId={}, period={}, count={}",
					datasetId,
					period,
					reviewIds.size()
				);

				return RepeatStatus.FINISHED;

			}, transactionManager)
			.build();
	}

	private UUID bytesToUuid(byte[] bytes) {

		if (bytes == null || bytes.length != 16) {
			throw new IllegalArgumentException("review_id must be BINARY(16).");
		}

		ByteBuffer buffer = ByteBuffer.wrap(bytes);

		return new UUID(
			buffer.getLong(),
			buffer.getLong()
		);
	}
}
