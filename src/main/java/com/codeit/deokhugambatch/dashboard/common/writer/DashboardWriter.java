package com.codeit.deokhugambatch.dashboard.common.writer;

import java.util.List;

/**
 * Dashboard 통계 및 랭킹 데이터를 영속성 저장소(MySQL)에 적재하기 위한 공통 Writer 인터페이스입니다.
 * <p>
 * Tasklet 레이어로부터 집계·계산이 완료된 결과 데이터와 당해 배치 차수의 고유 식별자를 넘겨받아
 * 데이터셋 ID 바인딩 및 최종 저장을 수행합니다.
 *
 * @param <O> 최종 적재 대상 엔티티 또는 DTO 타입 (예: PopularBook, PopularReview, PowerUser)
 */
public interface DashboardWriter<O> {

	/**
	 * 산출된 결과 목록에 발급된 datasetId를 일괄 매핑하고 데이터베이스에 영속화합니다.
	 * 제공된 DB 스키마 규격(BIGINT)에 맞춰 datasetId는 Long 타입을 사용합니다.
	 *
	 * @param items     집계 및 계산이 완료된 결과 목록
	 * @param datasetId 해당 배치 차수의 고유 데이터셋 ID (BIGINT)
	 */
	void write(List<O> items, Long datasetId);
}