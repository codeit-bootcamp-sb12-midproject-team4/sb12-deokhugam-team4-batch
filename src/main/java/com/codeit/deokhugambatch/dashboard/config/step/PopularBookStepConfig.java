package com.codeit.deokhugambatch.dashboard.config.step;

import com.codeit.deokhugambatch.dashboard.common.calculator.DashboardCalculator;
import com.codeit.deokhugambatch.dashboard.common.model.DashboardPeriod;
import com.codeit.deokhugambatch.dashboard.common.reader.DashboardReader;
import com.codeit.deokhugambatch.dashboard.common.writer.DashboardWriter;
import com.codeit.deokhugambatch.dashboard.popularbook.model.PopularBookCandidate;
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
 * 인기도서(Popular Book) 집계 및 랭킹 산출 Step
 *
 * <p>베이지안 평균 계산은 전체 후보군을 대상으로 수행해야 하므로
 * Chunk 기반이 아닌 Tasklet 기반으로 구현한다.</p>
 */
@Slf4j
@Configuration
public class PopularBookStepConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	private final DashboardReader<PopularBookCandidate> popularBookReader;
	private final DashboardCalculator<
		PopularBookCandidate,
		PopularBookCandidate> popularBookCalculator;
	private final DashboardWriter<PopularBookCandidate> popularBookWriter;

	public PopularBookStepConfig(
		JobRepository jobRepository,
		PlatformTransactionManager transactionManager,
		@Qualifier("popularBookReader")
		DashboardReader<PopularBookCandidate> popularBookReader,
		@Qualifier("popularBookCalculator")
		DashboardCalculator<PopularBookCandidate, PopularBookCandidate> popularBookCalculator,
		@Qualifier("popularBookWriter")
		DashboardWriter<PopularBookCandidate> popularBookWriter) {

		this.jobRepository = jobRepository;
		this.transactionManager = transactionManager;
		this.popularBookReader = popularBookReader;
		this.popularBookCalculator = popularBookCalculator;
		this.popularBookWriter = popularBookWriter;
	}

	@Bean
	public Step popularBookStep() {

		return new StepBuilder("popularBookStep", jobRepository)
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
					"[PopularBookStep] 인기도서 집계 시작 - datasetId={}, period={}",
					datasetId,
					period
				);

				// 1. Read
				List<PopularBookCandidate> candidates =
					popularBookReader.read(batchDate, period);

				if (candidates.isEmpty()) {
					log.warn("[PopularBookStep] 집계 대상 데이터가 존재하지 않습니다.");
					return RepeatStatus.FINISHED;
				}

				// 2. Calculate
				List<PopularBookCandidate> rankedCandidates =
					popularBookCalculator.calculate(candidates);

				// 3. Write
				popularBookWriter.write(rankedCandidates, datasetId);

				log.info(
					"[PopularBookStep] 인기도서 집계 완료 - {}건 저장",
					rankedCandidates.size()
				);

				return RepeatStatus.FINISHED;

			}, transactionManager)
			.build();
	}
}