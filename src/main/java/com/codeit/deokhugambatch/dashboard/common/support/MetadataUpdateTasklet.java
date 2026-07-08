package com.codeit.deokhugambatch.dashboard.common.support;

import com.codeit.deokhugambatch.dashboard.common.model.BatchMetadataType;
import com.codeit.deokhugambatch.dashboard.common.model.DashboardPeriod;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 앞선 모든 통계 집계 스텝이 성공했을 때,
 * batch_metadata 테이블의 포인터를 최신 데이터셋으로 전환하는 Tasklet입니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetadataUpdateTasklet implements Tasklet {

	private final JdbcTemplate jdbcTemplate;

	/**
	 * 갱신 대상 메타데이터 타입
	 */
	private static final List<BatchMetadataType> METADATA_TYPES = List.of(
		BatchMetadataType.POPULAR_BOOK,
		BatchMetadataType.POPULAR_REVIEW,
		BatchMetadataType.POWER_USER
	);

	@Override
	public RepeatStatus execute(
		StepContribution contribution,
		ChunkContext chunkContext
	) {
		ExecutionContext context = contribution.getStepExecution()
			.getJobExecution()
			.getExecutionContext();

		if (!context.containsKey("datasetId")) {
			throw new IllegalStateException("ExecutionContext에 datasetId가 존재하지 않습니다.");
		}

		if (!context.containsKey("period")) {
			throw new IllegalStateException("ExecutionContext에 period가 존재하지 않습니다.");
		}

		if (!context.containsKey("batchDate")) {
			throw new IllegalStateException("ExecutionContext에 batchDate가 존재하지 않습니다.");
		}

		Long datasetId = context.getLong("datasetId");
		DashboardPeriod period = (DashboardPeriod) context.get("period");

		LocalDate batchDate = (LocalDate) context.get("batchDate");

		log.info(
			"Dashboard 배치 최종 메타데이터 갱신 시작 - 대상 Period: {}, New Dataset ID: {}",
			period,
			datasetId
		);

		String sql = """
                INSERT INTO batch_metadata
                    (metadata_type, period, dataset_id, batch_date, updated_at)
                VALUES (?, ?, ?, ?, NOW(6))
                ON DUPLICATE KEY UPDATE
                    dataset_id = VALUES(dataset_id),
                    batch_date = VALUES(batch_date),
                    updated_at = NOW(6)
                """;

		for (BatchMetadataType metadataType : METADATA_TYPES) {
			jdbcTemplate.update(
				sql,
				metadataType.name(),
				period.name(),
				datasetId,
				Date.valueOf(batchDate)
			);
		}

		log.info("Dashboard 배치 최종 메타데이터 갱신 완료 - API 레이어 캐시 로테이션 준비 완료");

		return RepeatStatus.FINISHED;
	}
}