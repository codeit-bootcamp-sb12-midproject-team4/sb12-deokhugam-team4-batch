package com.codeit.deokhugambatch.user.job;

import com.codeit.deokhugambatch.user.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class UserConfig {

  private final UserRepository userRepository;
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  @Bean
  public Job deleteUserJob() {
    return new JobBuilder("deleteUserJob", jobRepository)
        .start(deleteUserStep())
        .build();
  }

  @Bean
  public Step deleteUserStep() {
    return new StepBuilder("deleteUserStep", jobRepository)
        .tasklet(deleteUserTasklet(), transactionManager).build();
  }

  @Bean
  public Tasklet deleteUserTasklet() {
    return (contribution, chunkContext) -> {
      Instant oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS);
      userRepository.deleteExpiredUsers(oneDayAgo);
      log.info("탈퇴 후 1일 경과한 사용자 물리 삭제 완료");
      return RepeatStatus.FINISHED;
    };
  }

}
