package com.codeit.deokhugambatch.batch.notification.tasklet;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.codeit.deokhugambatch.batch.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationDeleteTasklet implements Tasklet {

	private final NotificationRepository notificationRepository;

	@Nullable
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		Instant threshold = Instant.now().minus(7, ChronoUnit.DAYS);

		int deletedCount = notificationRepository.deleteConfirmedNotifications(threshold);

		contribution.incrementWriteCount(deletedCount);

		return RepeatStatus.FINISHED;
	}

}