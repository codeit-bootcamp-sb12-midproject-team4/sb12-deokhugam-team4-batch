package com.codeit.deokhugambatch.batch.notification.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.codeit.deokhugambatch.batch.notification.tasklet.NotificationDeleteTasklet;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class NotificationDeleteJobConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;
	private final NotificationDeleteTasklet notificationDeleteTasklet;

	@Bean
	public Step notificationDeleteStep() {
		return new StepBuilder("notificationDeleteStep", jobRepository)
			.tasklet(notificationDeleteTasklet, transactionManager)
			.build();
	}

	@Bean
	public Job notificationDeleteJob() {
		return new JobBuilder("notificationDeleteJob", jobRepository)
			.start(notificationDeleteStep())
			.build();
	}
}