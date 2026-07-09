package com.codeit.deokhugambatch.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrendingKeywordScheduler {

	private final JobLauncher jobLauncher;
	private final Job trendingKeywordJob;

	// 10분마다 Trending Keyword Job 실행
	@Scheduled(cron = "0 */10 * * * *")
	public void runTrendingKeywordJob() {

		try {
			LocalDate batchDate = LocalDate.now();

			JobParameters jobParameters =
				new JobParametersBuilder()
					.addString(
						"period",
						"REALTIME"
					)
					.addString(
						"batchDate",
						batchDate.toString()
					)
					.addLocalDateTime(
						"requestedAt",
						LocalDateTime.now()
					)
					.toJobParameters();

			log.info(
				"[TrendingKeywordScheduler] REALTIME 배치 실행 시작 - batchDate={}",
				batchDate
			);

			jobLauncher.run(
				trendingKeywordJob,
				jobParameters
			);

			log.info(
				"[TrendingKeywordScheduler] REALTIME 배치 실행 요청 완료"
			);

		} catch (Exception exception) {
			log.error(
				"[TrendingKeywordScheduler] REALTIME 배치 실행 실패",
				exception
			);
		}
	}
}