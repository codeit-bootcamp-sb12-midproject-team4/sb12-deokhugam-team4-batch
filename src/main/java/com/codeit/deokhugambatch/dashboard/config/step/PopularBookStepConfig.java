package com.codeit.deokhugambatch.dashboard.config.step;

import com.codeit.deokhugambatch.dashboard.common.calculator.DashboardCalculator;
import com.codeit.deokhugambatch.dashboard.common.model.DashboardPeriod;
import com.codeit.deokhugambatch.dashboard.common.reader.DashboardReader;
import com.codeit.deokhugambatch.dashboard.common.writer.DashboardWriter;
import com.codeit.deokhugambatch.dashboard.popularbook.model.PopularBookCandidate;
import com.codeit.deokhugamcommon.domain.dashboard.entity.PopularBook;
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
 * 인기도서(Popular Book) 집계 및 랭킹 산출 스텝을 정의하는 설정 클래스입니다.
 * 베이지안 평균 집계를 위해 원천 데이터를 Full-Pool로 다루어야 하므로 Tasklet 아키텍처를 채택했습니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class PopularBookStepConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	@Qualifier("popularBookReader")
	private final DashboardReader<PopularBookCandidate> popularBookReader;
	@Qualifier("popularBookCalculator")
	private final DashboardCalculator<PopularBookCandidate, PopularBook> popularBookCalculator;
	@Qualifier("popularBookWriter")
	private final DashboardWriter<PopularBook> popularBookWriter;

	@Bean
	public Step popularBookStep() {
		return new StepBuilder("popularBookStep", jobRepository)
			.tasklet((contribution, chunkContext) -> {
				// 1. JobExecutionContext에서 공유 파라미터 획득
				Map<String, Object> jobContext = chunkContext.getStepContext().getJobExecutionContext();
				Long datasetId = (Long) jobContext.get("datasetId");
				DashboardPeriod period = DashboardPeriod.valueOf((String) jobContext.get("period"));
				LocalDate batchDate = LocalDate.parse((String) jobContext.get("batchDate"));

				log.info("[PopularBookStep] 인기도서 집계 시작 - DatasetID: {}, Period: {}", datasetId, period);

				// 2. Read: 대상 기간 내의 도서 통계 후보군 전건 조회
				List<PopularBookCandidate> candidates = popularBookReader.read(batchDate, period);
				if (candidates.isEmpty()) {
					log.warn("[PopularBookStep] 집계 대상 도서 데이터가 존재하지 않습니다.");
					return RepeatStatus.FINISHED;
				}

				// 3. Calculate: 베이지안 평균 알고리즘 적용 및 상위 50위 정렬/커팅
				List<PopularBook> rankedBooks = popularBookCalculator.calculate(candidates);

				// 4. Write: 결과 엔티티 변환 및 영속화(Bulk Insert)
				popularBookWriter.write(rankedBooks, datasetId);

				log.info("[PopularBookStep] 인기도서 집계 완료 - 산출된 도서 수: {}건", rankedBooks.size());
				return RepeatStatus.FINISHED;
			}, transactionManager)
			.build();
	}
}