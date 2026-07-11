package com.codeit.deokhugambatch.dashboard.config.step;

import com.codeit.deokhugambatch.dashboard.common.calculator.DashboardCalculator;
import com.codeit.deokhugambatch.dashboard.common.model.DashboardPeriod;
import com.codeit.deokhugambatch.dashboard.common.reader.DashboardReader;
import com.codeit.deokhugambatch.dashboard.common.writer.DashboardWriter;
import com.codeit.deokhugambatch.dashboard.popularreview.model.PopularReviewCandidate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 인기리뷰(Popular Review) 집계 및 랭킹 산출 Step
 *
 * <p>인기리뷰 점수는 전체 후보군을 대상으로 계산해야 하므로
 * Chunk 기반이 아닌 Tasklet 기반으로 구현한다.</p>
 */
@Slf4j
@Configuration
public class PopularReviewStepConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	private final DashboardReader<PopularReviewCandidate> popularReviewReader;
	private final DashboardCalculator<
		PopularReviewCandidate,
		PopularReviewCandidate> popularReviewCalculator;
	private final DashboardWriter<PopularReviewCandidate> popularReviewWriter;

	public PopularReviewStepConfig(
		JobRepository jobRepository,
		PlatformTransactionManager transactionManager,
		@Qualifier("popularReviewReader")
		DashboardReader<PopularReviewCandidate> popularReviewReader,
		@Qualifier("popularReviewCalculator")
		DashboardCalculator<
			PopularReviewCandidate,
			PopularReviewCandidate> popularReviewCalculator,
		@Qualifier("popularReviewWriter")
		DashboardWriter<PopularReviewCandidate> popularReviewWriter) {

		this.jobRepository = jobRepository;
		this.transactionManager = transactionManager;
		this.popularReviewReader = popularReviewReader;
		this.popularReviewCalculator = popularReviewCalculator;
		this.popularReviewWriter = popularReviewWriter;
	}

	@Bean
	public Step popularReviewStep() {

		return new StepBuilder("popularReviewStep", jobRepository)
			.tasklet((contribution, chunkContext) -> {

				// JobExecutionContext에서 공통 파라미터 조회
				Map<String, Object> jobContext =
					chunkContext.getStepContext().getJobExecutionContext();

				Long datasetId = (Long) jobContext.get("datasetId");
				DashboardPeriod period =
					(DashboardPeriod) jobContext.get("period");
				LocalDate batchDate =
					(LocalDate) jobContext.get("batchDate");

				log.info(
					"[PopularReviewStep] 인기리뷰 집계 시작 - datasetId={}, period={}",
					datasetId,
					period
				);

				// 1. Read
				List<PopularReviewCandidate> candidates =
					popularReviewReader.read(batchDate, period);

				if (candidates.isEmpty()) {
					log.error("[PopularReviewStep] 집계 대상 데이터가 존재하지 않습니다.");
					throw new IllegalStateException(
						"PopularReview 집계 대상이 존재하지 않습니다."
					);
				}

				// 2. Calculate
				List<PopularReviewCandidate> rankedCandidates =
					popularReviewCalculator.calculate(candidates);

				// 3. Write
				popularReviewWriter.write(rankedCandidates, datasetId);

				log.info(
					"[PopularReviewStep] 인기리뷰 집계 완료 - {}건 저장",
					rankedCandidates.size()
				);

				return RepeatStatus.FINISHED;

			}, transactionManager)
			.build();
	}
}