package com.codeit.deokhugambatch.dashboard.config.step;

import com.codeit.deokhugambatch.dashboard.common.model.BatchMetadataType;
import com.codeit.deokhugambatch.dashboard.common.support.MetadataUpdateTasklet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 모든 통계 데이터 적재가 완료된 후,
 * 최신 dataset_id로 batch_metadata 포인터를 갱신하는 Step 설정입니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MetadataUpdateStepConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;
	private final JdbcTemplate jdbcTemplate;

	/**
	 * Daily Dashboard용 MetadataUpdateTasklet
	 */
	@Bean
	public MetadataUpdateTasklet metadataUpdateTasklet() {
		return new MetadataUpdateTasklet(
			jdbcTemplate,
			List.of(
				BatchMetadataType.POPULAR_BOOK,
				BatchMetadataType.POPULAR_REVIEW,
				BatchMetadataType.POWER_USER
			)
		);
	}

	@Bean
	public Step metadataUpdateStep() {

		log.info("[MetadataUpdateStepConfig] 메타데이터 업데이트 스텝 빌드 완료");

		return new StepBuilder("metadataUpdateStep", jobRepository)
			.tasklet(metadataUpdateTasklet(), transactionManager)
			.build();
	}
}