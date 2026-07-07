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

/**
 * Dashboard 배치의 전체 워크플로우 및 스텝 실행 순서를 관장하는 메인 설정 클래스입니다.
 * 개별 Step 설정은 하위 step 패키지에서 정의하고 이 클래스에서 조립합니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DashboardJobConfig {

	private final JobRepository jobRepository;
	private final DatasetIdGenerator datasetIdGenerator;

	/**
	 * 오전 3시 정기 통계 배치를 구동하는 Job 정의입니다.
	 */
	@Bean
	public Job dailyDashboardJob(
		Step popularBookStep,
		Step popularReviewStep,
		Step powerUserStep,
		Step metadataUpdateStep
	) {
		return new JobBuilder("dailyDashboardJob", jobRepository)
			.listener(dashboardJobListener())
			.start(popularBookStep)
			.next(popularReviewStep)
			.next(powerUserStep)
			.next(metadataUpdateStep)
			.build();
	}

	/**
	 * 배치 실행 전 공통 파라미터를 JobExecutionContext에 저장합니다.
	 */
	@Bean
	public JobExecutionListener dashboardJobListener() {
		return new JobExecutionListener() {

			@Override
			public void beforeJob(JobExecution jobExecution) {

				DashboardPeriod period = DashboardPeriod.valueOf(
					jobExecution.getJobParameters()
						.getString("period", DashboardPeriod.DAILY.name())
						.toUpperCase()
				);

				LocalDate batchDate = LocalDate.parse(
					jobExecution.getJobParameters()
						.getString("batchDate", LocalDate.now().toString())
				);

				Long datasetId = datasetIdGenerator.generate();

				log.info("=== Dashboard 통계 배치 초기화 ===");
				log.info("집계 주기(Period): {}", period);
				log.info("배치 기준일(BatchDate): {}", batchDate);
				log.info("발급된 데이터셋 ID(DatasetId): {}", datasetId);
				log.info("=================================");

				jobExecution.getExecutionContext().put("datasetId", datasetId);
				jobExecution.getExecutionContext().put("period", period);
				jobExecution.getExecutionContext().put("batchDate", batchDate);
			}

			@Override
			public void afterJob(JobExecution jobExecution) {
				if (jobExecution.getStatus().isUnsuccessful()) {
					log.error(
						"Dashboard 통계 배치가 실패하였습니다. 메타데이터가 갱신되지 않아 API 레이어는 이전 데이터셋을 유지합니다."
					);
				} else {
					log.info("Dashboard 통계 배치가 성공적으로 완료되어 데이터 스위칭이 완료되었습니다.");
				}
			}
		};
	}
}