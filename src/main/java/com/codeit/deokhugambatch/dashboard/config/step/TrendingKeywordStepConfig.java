package com.codeit.deokhugambatch.dashboard.config.step;

import com.codeit.deokhugambatch.dashboard.common.calculator.DashboardCalculator;
import com.codeit.deokhugambatch.dashboard.common.model.DashboardPeriod;
import com.codeit.deokhugambatch.dashboard.common.reader.DashboardReader;
import com.codeit.deokhugambatch.dashboard.common.writer.DashboardWriter;
import com.codeit.deokhugambatch.dashboard.trendingkeyword.model.KeywordFrequency;
import com.codeit.deokhugambatch.dashboard.trendingkeyword.model.TrendingKeywordCandidate;
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
 * 트렌딩 키워드(Trending Keyword) 집계 및 저장 Step
 *
 * <p>
 * Elasticsearch에서 최근 3시간 검색어 Top100을 조회한 뒤,
 * LLM 기반 후처리를 수행하여 Top10을 생성하고 저장합니다.
 * </p>
 *
 * <p>
 * Reader → Calculator → Writer 패턴을 따르며,
 * Chunk 기반이 아닌 Tasklet 기반으로 구현합니다.
 * </p>
 */
@Slf4j
@Configuration
public class TrendingKeywordStepConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	private final DashboardReader<KeywordFrequency> trendingKeywordReader;

	private final DashboardCalculator<
		KeywordFrequency,
		TrendingKeywordCandidate> trendingKeywordCalculator;

	private final DashboardWriter<TrendingKeywordCandidate> trendingKeywordWriter;

	public TrendingKeywordStepConfig(
		JobRepository jobRepository,
		PlatformTransactionManager transactionManager,
		@Qualifier("trendingKeywordReader")
		DashboardReader<KeywordFrequency> trendingKeywordReader,
		@Qualifier("trendingKeywordCalculator")
		DashboardCalculator<
			KeywordFrequency,
			TrendingKeywordCandidate> trendingKeywordCalculator,
		@Qualifier("trendingKeywordWriter")
		DashboardWriter<TrendingKeywordCandidate> trendingKeywordWriter
	) {
		this.jobRepository = jobRepository;
		this.transactionManager = transactionManager;
		this.trendingKeywordReader = trendingKeywordReader;
		this.trendingKeywordCalculator = trendingKeywordCalculator;
		this.trendingKeywordWriter = trendingKeywordWriter;
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

				// 2. Calculate (LLM 후처리 및 Top10 생성)
				List<TrendingKeywordCandidate> rankedKeywords =
					trendingKeywordCalculator.calculate(keywordFrequencies);

				// 3. Write
				trendingKeywordWriter.write(rankedKeywords, datasetId);

				log.info(
					"[TrendingKeywordStep] 트렌딩 키워드 집계 완료 - {}건 저장",
					rankedKeywords.size()
				);

				return RepeatStatus.FINISHED;

			}, transactionManager)
			.build();
	}
}