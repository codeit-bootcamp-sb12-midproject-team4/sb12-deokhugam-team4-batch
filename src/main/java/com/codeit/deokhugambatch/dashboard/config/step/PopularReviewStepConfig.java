package com.codeit.deokhugambatch.dashboard.config.step;

import com.codeit.deokhugambatch.dashboard.common.calculator.DashboardCalculator;
import com.codeit.deokhugambatch.dashboard.common.model.DashboardPeriod;
import com.codeit.deokhugambatch.dashboard.common.reader.DashboardReader;
import com.codeit.deokhugambatch.dashboard.common.writer.DashboardWriter;
import com.codeit.deokhugambatch.dashboard.popularreview.model.PopularReviewCandidate;
import lombok.RequiredArgsConstructor;
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
 * 인기리뷰(Popular Review) 집계 및 랭킹 산출 스텝을 정의하는 설정 클래스입니다.
 * '좋아요 최소 1개 이상' 제약조건 필터링 및 선형 결합 스코어링을 지원합니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class PopularReviewStepConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	@Qualifier("popularReviewReader")
	private final DashboardReader<PopularReviewCandidate> popularReviewReader;
	@Qualifier("popularReviewCalculator")
	private final DashboardCalculator<PopularReviewCandidate, PopularReviewCandidate> popularReviewCalculator;
	@Qualifier("popularReviewWriter")
	private final DashboardWriter<PopularReviewCandidate> popularReviewWriter;

	@Bean
	public Step popularReviewStep() {
		return new StepBuilder("popularReviewStep", jobRepository)
			.tasklet((contribution, chunkContext) -> {
				// 1. JobExecutionContext에서 공유 파라미터 획득
				Map<String, Object> jobContext = chunkContext.getStepContext().getJobExecutionContext();
				Long datasetId = (Long) jobContext.get("datasetId");
				DashboardPeriod period = DashboardPeriod.valueOf((String) jobContext.get("period"));
				LocalDate batchDate = LocalDate.parse((String) jobContext.get("batchDate"));

				log.info("[PopularReviewStep] 인기리뷰 집계 시작 - DatasetID: {}, Period: {}", datasetId, period);

				// 2. Read: 대상 기간 내의 리뷰 데이터 원천 조회
				List<PopularReviewCandidate> candidates = popularReviewReader.read(batchDate, period);
				if (candidates.isEmpty()) {
					log.warn("[PopularReviewStep] 집계 대상 리뷰 데이터가 존재하지 않습니다.");
					return RepeatStatus.FINISHED;
				}

				// 3. Calculate: 좋아요 0개 원천 배제 필터링 및 선형 가중치 스코어링 (Top 50)
				List<PopularReviewCandidate> rankedReviews = popularReviewCalculator.calculate(candidates);

				// 4. Write: 결과 데이터셋 최종 저장
				popularReviewWriter.write(rankedReviews, datasetId);

				log.info("[PopularReviewStep] 인기리뷰 집계 완료 - 산출된 리뷰 수: {}건", rankedReviews.size());
				return RepeatStatus.FINISHED;
			}, transactionManager)
			.build();
	}
}