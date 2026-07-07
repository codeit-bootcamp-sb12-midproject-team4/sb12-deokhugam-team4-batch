package com.codeit.deokhugambatch.dashboard.popularbook.tasklet;

import com.codeit.deokhugambatch.dashboard.common.calculator.DashboardCalculator;
import com.codeit.deokhugambatch.dashboard.common.model.DashboardPeriod;
import com.codeit.deokhugambatch.dashboard.common.reader.DashboardReader;
import com.codeit.deokhugambatch.dashboard.common.writer.DashboardWriter;
import com.codeit.deokhugambatch.dashboard.popularbook.model.PopularBookCandidate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PopularBookTasklet implements Tasklet {

	private final DashboardReader<PopularBookCandidate> popularBookReader;
	private final DashboardCalculator<PopularBookCandidate, PopularBookCandidate> popularBookCalculator;
	private final DashboardWriter<PopularBookCandidate> popularBookWriter;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		// 1. 상위 JobListener가 적재해 둔 공통 컨텍스트 메타데이터 추출
		Map<String, Object> jobContext = chunkContext.getStepContext().getJobExecutionContext();
		Long datasetId = (Long) jobContext.get("datasetId");
		DashboardPeriod period = DashboardPeriod.valueOf((String) jobContext.get("period"));
		LocalDate batchDate = LocalDate.parse((String) jobContext.get("batchDate"));

		log.info("[PopularBookTasklet] Pipeline 구동 시작 - DatasetID: {}", datasetId);

		// 2. Read
		List<PopularBookCandidate> candidates = popularBookReader.read(batchDate, period);
		if (candidates.isEmpty()) {
			log.warn("[PopularBookTasklet] 집계 데이터 풀이 비어있어 단계를 조기 종료합니다.");
			return RepeatStatus.FINISHED;
		}

		// 3. Calculate (베이지안 스코어링 수식 기반 Top 50 정렬)
		List<PopularBookCandidate> rankedBooks = popularBookCalculator.calculate(candidates);

		// 4. Write
		popularBookWriter.write(rankedBooks, datasetId);

		log.info("[PopularBookTasklet] Pipeline 정상 종료 - 최종 적재 완료");
		return RepeatStatus.FINISHED;
	}
}