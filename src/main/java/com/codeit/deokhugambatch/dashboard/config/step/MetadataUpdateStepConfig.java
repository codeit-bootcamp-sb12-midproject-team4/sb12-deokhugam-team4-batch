package com.codeit.deokhugambatch.dashboard.config.step;

import com.codeit.deokhugambatch.dashboard.common.support.MetadataUpdateTasklet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 모든 통계 데이터 적재가 완료된 후, 최신 데이터셋 ID로 포인터를 전환하는
 * 메타데이터 업데이트 스텝 설정 클래스입니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MetadataUpdateStepConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;
	private final MetadataUpdateTasklet metadataUpdateTasklet;

	@Bean
	public Step metadataUpdateStep() {
		log.info("[MetadataUpdateStepConfig] 메타데이터 업데이트 스텝 빌드 완료");
		return new StepBuilder("metadataUpdateStep", jobRepository)
			.tasklet(metadataUpdateTasklet, transactionManager) // 앞서 작성한 전용 Tasklet 바인딩
			.build();
	}
}