package com.codeit.deokhugambatch.dashboard.config.step;

import com.codeit.deokhugambatch.dashboard.common.calculator.DashboardCalculator;
import com.codeit.deokhugambatch.dashboard.common.model.DashboardPeriod;
import com.codeit.deokhugambatch.dashboard.common.reader.DashboardReader;
import com.codeit.deokhugambatch.dashboard.common.writer.DashboardWriter;
import com.codeit.deokhugambatch.dashboard.poweruser.model.PowerUserCandidate;
import com.codeit.deokhugambatch.dashboard.poweruser.model.UserActionLedger;
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
 * 파워유저(Power User) 집계 및 랭킹 산출 스텝을 정의하는 설정 클래스입니다.
 * 유저별 다중 액션 원장(Ledger)을 기반으로 포인트를 가중 합산하므로 Tasklet 아키텍처를 채택했습니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class PowerUserStepConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	@Qualifier("powerUserReader")
	private final DashboardReader<UserActionLedger> powerUserReader;
	@Qualifier("powerUserCalculator")
	private final DashboardCalculator<UserActionLedger, PowerUserCandidate> powerUserCalculator;
	@Qualifier("powerUserWriter")
	private final DashboardWriter<PowerUserCandidate> powerUserWriter;

	@Bean
	public Step powerUserStep() {
		return new StepBuilder("powerUserStep", jobRepository)
			.tasklet((contribution, chunkContext) -> {
				// 1. JobExecutionContext에서 공유 파라미터 획득
				Map<String, Object> jobContext = chunkContext.getStepContext().getJobExecutionContext();
				Long datasetId = (Long) jobContext.get("datasetId");
				DashboardPeriod period = DashboardPeriod.valueOf((String) jobContext.get("period"));
				LocalDate batchDate = LocalDate.parse((String) jobContext.get("batchDate"));

				log.info("[PowerUserStep] 파워유저 집계 시작 - DatasetID: {}, Period: {}", datasetId, period);

				// 2. Read: 대상 기간 내의 유저별 활동 원장(Ledger) 집계 데이터 조회
				List<UserActionLedger> ledgers = powerUserReader.read(batchDate, period);
				if (ledgers.isEmpty()) {
					log.warn("[PowerUserStep] 집계 대상 유저 활동 데이터가 존재하지 않습니다.");
					return RepeatStatus.FINISHED;
				}

				// 3. Calculate: 액션 포인트 합산 알고리즘 적용 및 상위 10위 정렬/커팅
				List<PowerUserCandidate> rankedUsers = powerUserCalculator.calculate(ledgers);

				// 4. Write: 결과 데이터셋 최종 저장
				powerUserWriter.write(rankedUsers, datasetId);

				log.info("[PowerUserStep] 파워유저 집계 완료 - 산출된 파워유저 수: {}건", rankedUsers.size());
				return RepeatStatus.FINISHED;
			}, transactionManager)
			.build();
	}
}