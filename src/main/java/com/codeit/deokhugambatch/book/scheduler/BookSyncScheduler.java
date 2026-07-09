package com.codeit.deokhugambatch.book.scheduler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookSyncScheduler {

	private final JobLauncher jobLauncher;
	private final Job bookSyncJob;

	// 매 5분마다 실행
	@Scheduled(cron = "0 */5 * * * *")
	public void runBookSyncJob() {
		try {
			// 네트워크 지연이나 배치 실행 시간의 미세한 오차를 고려해 6분 전 데이터부터 조회 (중복 업데이트는 ES가 알아서 처리)
			Instant fromDate = Instant.now().minus(6, ChronoUnit.MINUTES);

			JobParameters jobParameters = new JobParametersBuilder()
				.addString("fromDate", fromDate.toString())
				.addLong("run.id", System.currentTimeMillis()) // 매번 새로운 JobInstance로 실행되도록 식별자 추가
				.toJobParameters();

			jobLauncher.run(bookSyncJob, jobParameters);
		} catch (Exception e) {
			log.error("도서 정보 정보 동기화 배치 작업 중 에러 발생: ", e);
		}
	}
}
