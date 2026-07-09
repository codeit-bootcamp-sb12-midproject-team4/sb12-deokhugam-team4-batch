package com.codeit.deokhugambatch.dashboard.config.step;

import com.codeit.deokhugambatch.dashboard.common.calculator.DashboardCalculator;
import com.codeit.deokhugambatch.dashboard.common.model.DashboardPeriod;
import com.codeit.deokhugambatch.dashboard.common.reader.DashboardReader;
import com.codeit.deokhugambatch.dashboard.common.writer.DashboardWriter;
import com.codeit.deokhugambatch.dashboard.poweruser.model.PowerUserCandidate;
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
 * 파워유저(Power User) 집계 및 랭킹 산출 Step
 *
 * <p>사용자 활동 점수는 전체 후보군을 대상으로 계산해야 하므로
 * Chunk 기반이 아닌 Tasklet 기반으로 구현한다.</p>
 */
@Slf4j
@Configuration
public class PowerUserStepConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	private final DashboardReader<PowerUserCandidate> powerUserReader;
	private final DashboardCalculator<
		PowerUserCandidate,
		PowerUserCandidate> powerUserCalculator;
	private final DashboardWriter<PowerUserCandidate> powerUserWriter;

	public PowerUserStepConfig(
		JobRepository jobRepository,
		PlatformTransactionManager transactionManager,
		@Qualifier("powerUserReader")
		DashboardReader<PowerUserCandidate> powerUserReader,
		@Qualifier("powerUserCalculator")
		DashboardCalculator<
			PowerUserCandidate,
			PowerUserCandidate> powerUserCalculator,
		@Qualifier("powerUserWriter")
		DashboardWriter<PowerUserCandidate> powerUserWriter) {

		this.jobRepository = jobRepository;
		this.transactionManager = transactionManager;
		this.powerUserReader = powerUserReader;
		this.powerUserCalculator = powerUserCalculator;
		this.powerUserWriter = powerUserWriter;
	}

	@Bean
	public Step powerUserStep() {

		return new StepBuilder("powerUserStep", jobRepository)
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
					"[PowerUserStep] 파워유저 집계 시작 - datasetId={}, period={}",
					datasetId,
					period
				);

				// 1. Read
				List<PowerUserCandidate> candidates =
					powerUserReader.read(batchDate, period);

				if (candidates.isEmpty()) {
					log.warn("[PowerUserStep] 집계 대상 데이터가 존재하지 않습니다.");
					return RepeatStatus.FINISHED;
				}

				// 2. Calculate
				List<PowerUserCandidate> rankedCandidates =
					powerUserCalculator.calculate(candidates);

				// 3. Write
				powerUserWriter.write(rankedCandidates, datasetId);

				log.info(
					"[PowerUserStep] 파워유저 집계 완료 - {}건 저장",
					rankedCandidates.size()
				);

				return RepeatStatus.FINISHED;

			}, transactionManager)
			.build();
	}
}