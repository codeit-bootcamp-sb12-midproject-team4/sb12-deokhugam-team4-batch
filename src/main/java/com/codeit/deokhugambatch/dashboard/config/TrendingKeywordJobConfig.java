package com.codeit.deokhugambatch.dashboard.config;

import com.codeit.deokhugambatch.dashboard.common.model.DashboardPeriod;
import com.codeit.deokhugambatch.dashboard.common.support.DatasetIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TrendingKeywordJobConfig {

	private final JobRepository jobRepository;
	private final DatasetIdGenerator datasetIdGenerator;

	/**
	 * 트렌딩 키워드 전용 Job
	 */
	@Bean
	public Job trendingKeywordJob(
		Step trendingKeywordStep,
		Step metadataUpdateStep
	) {

		return new JobBuilder("trendingKeywordJob", jobRepository)
			.listener(trendingKeywordJobListener())
			.start(trendingKeywordStep)
			.next(metadataUpdateStep)
			.build();
	}

	/**
	 * Job 실행 전 공통 파라미터 초기화
	 */
	@Bean
	public JobExecutionListener trendingKeywordJobListener() {

		return new JobExecutionListener() {

			@Override
			public void beforeJob(JobExecution jobExecution) {

				DashboardPeriod period = DashboardPeriod.REALTIME;

				LocalDate batchDate = LocalDate.now();

				Long datasetId = datasetIdGenerator.generate();

				log.info("=== Trending Keyword Batch 초기화 ===");
				log.info("Period      : {}", period);
				log.info("Batch Date  : {}", batchDate);
				log.info("Dataset ID  : {}", datasetId);
				log.info("=====================================");

				jobExecution.getExecutionContext().put("datasetId", datasetId);
				jobExecution.getExecutionContext().put("period", period);
				jobExecution.getExecutionContext().put("batchDate", batchDate);
			}

			@Override
			public void afterJob(JobExecution jobExecution) {

				if (jobExecution.getStatus().isUnsuccessful()) {
					log.error(
						"Trending Keyword Batch 실패 - 이전 트렌딩 데이터를 계속 사용합니다."
					);
				} else {
					log.info(
						"Trending Keyword Batch 완료 - 최신 트렌딩 데이터로 전환되었습니다."
					);
				}
			}
		};
	}
}