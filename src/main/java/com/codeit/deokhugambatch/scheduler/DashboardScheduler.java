package com.codeit.deokhugambatch.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class DashboardScheduler {

	private final JobLauncher jobLauncher;

	private final Job dailyDashboardJob;

	/**
	 * 매일 오전 3시 Dashboard 통계 배치를 실행한다.
	 */
	@Scheduled(cron = "0 0 3 * * *")
	public void runDailyDashboardJob() {

		try {

			JobParameters jobParameters = new JobParametersBuilder()
				.addString("period", "DAILY")
				.addString("batchDate", LocalDate.now().toString())
				.addLocalDateTime("requestedAt", LocalDateTime.now())
				.toJobParameters();

			log.info(
				"[DashboardScheduler] Dashboard DAILY 배치 실행 시작 - batchDate={}",
				LocalDate.now()
			);

			jobLauncher.run(dailyDashboardJob, jobParameters);

			log.info("[DashboardScheduler] Dashboard DAILY 배치 실행 요청 완료");

		} catch (Exception exception) {

			log.error(
				"[DashboardScheduler] Dashboard DAILY 배치 실행 실패",
				exception
			);
		}
	}
}
