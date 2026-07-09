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
import org.springframework.lang.NonNull;

/**
 * 앞선 모든 통계 집계 스텝이 성공했을 때,
 * batch_metadata 테이블의 최신 dataset_id 포인터를 갱신하는 Tasklet입니다.
 *
 * <p>
 * 갱신 대상 MetadataType은 생성자를 통해 주입받으며,
 * Daily Dashboard / Trending Keyword Job 모두 동일한 Tasklet을 재사용합니다.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class MetadataUpdateTasklet implements Tasklet {

	private final JdbcTemplate jdbcTemplate;

	/**
	 * 이번 Step에서 갱신할 Metadata Type 목록
	 */
	private final List<BatchMetadataType> metadataTypes;

	@Override
	public RepeatStatus execute(
		@NonNull StepContribution contribution,
		@NonNull ChunkContext chunkContext
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

		DashboardPeriod period =
			(DashboardPeriod) context.get("period");

		LocalDate batchDate =
			(LocalDate) context.get("batchDate");

		log.info(
			"Dashboard 메타데이터 갱신 시작 - metadataTypes={}, period={}, datasetId={}",
			metadataTypes,
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

		for (BatchMetadataType metadataType : metadataTypes) {

			jdbcTemplate.update(
				sql,
				metadataType.name(),
				period.name(),
				datasetId,
				Date.valueOf(batchDate)
			);

			log.debug(
				"batch_metadata 갱신 완료 - metadataType={}",
				metadataType
			);
		}

		log.info("Dashboard 메타데이터 갱신 완료");

		return RepeatStatus.FINISHED;
	}
}