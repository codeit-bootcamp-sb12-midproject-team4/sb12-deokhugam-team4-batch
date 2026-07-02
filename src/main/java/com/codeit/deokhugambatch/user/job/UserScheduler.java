package com.codeit.deokhugambatch.user.job;

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
public class UserScheduler {

  private final JobLauncher jobLauncher;
  private final Job deleteUserJob;

  //@Scheduled(cron = "0 0 0 * * *")
  @Scheduled(fixedDelay = 10000)
  public void scheduled() throws Exception {
    JobParameters jobParameters = new JobParametersBuilder()
        .addLong("time", System.currentTimeMillis())
        .toJobParameters();

    jobLauncher.run(deleteUserJob, jobParameters);
    log.info("하드 삭제 배치 실행 완료");
  }

}
