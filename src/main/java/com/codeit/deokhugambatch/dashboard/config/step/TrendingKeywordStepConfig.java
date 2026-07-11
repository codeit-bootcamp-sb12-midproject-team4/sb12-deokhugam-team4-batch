package com.codeit.deokhugambatch.dashboard.config.step;

import com.codeit.deokhugambatch.dashboard.common.model.DashboardPeriod;
import com.codeit.deokhugambatch.dashboard.common.reader.DashboardReader;
import com.codeit.deokhugambatch.dashboard.common.writer.DashboardWriter;
import com.codeit.deokhugambatch.dashboard.trendingkeyword.calculator.TrendingKeywordCalculator;
import com.codeit.deokhugambatch.dashboard.trendingkeyword.model.KeywordFrequency;
import com.codeit.deokhugambatch.dashboard.trendingkeyword.model.LlmKeywordResult;
import com.codeit.deokhugambatch.dashboard.trendingkeyword.model.TrendingKeywordCandidate;
import com.codeit.deokhugambatch.dashboard.trendingkeyword.service.LlmKeywordService;
import com.codeit.deokhugambatch.dashboard.trendingkeyword.writer.TrendingKeywordSnapshotWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class TrendingKeywordStepConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;
	private final DashboardReader<KeywordFrequency> trendingKeywordReader;
	private final TrendingKeywordCalculator trendingKeywordCalculator;
	private final DashboardWriter<TrendingKeywordCandidate> trendingKeywordWriter;
	private final TrendingKeywordSnapshotWriter trendingKeywordSnapshotWriter;
	private final LlmKeywordService llmKeywordService;

	public TrendingKeywordStepConfig(
		JobRepository jobRepository,
		PlatformTransactionManager transactionManager,
		@Qualifier("trendingKeywordReader")
		DashboardReader<KeywordFrequency> trendingKeywordReader,
		TrendingKeywordCalculator trendingKeywordCalculator,
		@Qualifier("trendingKeywordWriter")
		DashboardWriter<TrendingKeywordCandidate> trendingKeywordWriter,
		TrendingKeywordSnapshotWriter trendingKeywordSnapshotWriter,
		LlmKeywordService llmKeywordService
	) {
		this.jobRepository = jobRepository;
		this.transactionManager = transactionManager;
		this.trendingKeywordReader = trendingKeywordReader;
		this.trendingKeywordCalculator = trendingKeywordCalculator;
		this.trendingKeywordWriter = trendingKeywordWriter;
		this.trendingKeywordSnapshotWriter = trendingKeywordSnapshotWriter;
		this.llmKeywordService = llmKeywordService;
	}

	@Bean
	public Step trendingKeywordStep() {

		return new StepBuilder("trendingKeywordStep", jobRepository)
			.tasklet((contribution, chunkContext) -> {

				// JobExecutionContext에서 공통 파라미터 조회
				Map<String, Object> jobContext =
					chunkContext.getStepContext().getJobExecutionContext();

				Long datasetId = (Long) jobContext.get("datasetId");

				DashboardPeriod period =
					(DashboardPeriod) jobContext.get("period");

				LocalDate batchDate =
					(LocalDate) jobContext.get("batchDate");

				if (period != DashboardPeriod.REALTIME) {
					throw new IllegalStateException(
						"TrendingKeywordStep는 REALTIME Period에서만 실행할 수 있습니다."
					);
				}

				log.info(
					"[TrendingKeywordStep] 트렌딩 키워드 집계 시작 - datasetId={}, period={}",
					datasetId,
					period
				);

				// 1. Read (ES Aggregation Top100 조회)
				List<KeywordFrequency> keywordFrequencies =
					trendingKeywordReader.read(batchDate, period);

				if (keywordFrequencies.isEmpty()) {
					log.warn("[TrendingKeywordStep] 집계 대상 키워드가 존재하지 않습니다.");
					return RepeatStatus.FINISHED;
				}

				// 2. LLM 후처리
				List<LlmKeywordResult> refinedKeywords =
					llmKeywordService.refineKeywords(keywordFrequencies);

				// 3. Ranking 계산
				List<TrendingKeywordCandidate> rankedKeywords =
					trendingKeywordCalculator.calculate(
						refinedKeywords
					);

				// 4. Snapshot 생성
				Long snapshotDatasetId =
					trendingKeywordSnapshotWriter.createSnapshot();

				// 5. TrendingKeyword 저장
				trendingKeywordWriter.write(rankedKeywords, snapshotDatasetId);

				log.info(
					"[TrendingKeywordStep] 트렌딩 키워드 집계 완료 - {}건 저장",
					rankedKeywords.size()
				);

				return RepeatStatus.FINISHED;

			}, transactionManager)
			.build();
	}
}