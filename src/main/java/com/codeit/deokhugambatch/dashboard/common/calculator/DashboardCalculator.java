package com.codeit.deokhugambatch.dashboard.common.calculator;

import java.util.List;

/**
 * Dashboard 통계 및 랭킹 집계를 위한 비즈니스 계산 공통 인터페이스입니다.
 * <p>
 * 배치의 Tasklet 레이어에서 호출되며, Reader가 읽어온 원천 데이터 집합을 토대로
 * 스코어링, 정렬, 랭킹 부여 및 최대 상한선(Top N) 제한 필터링을 수행합니다.
 * @param <I> 입력 데이터 타입 (도메인별 집계 원천 데이터 또는 Candidate DTO)
 * @param <O> 출력 데이터 타입 (최종 적재할 엔티티 또는 결과 DTO)
 */
public interface DashboardCalculator<I, O> {

	/**
	 * 원천 데이터 리스트를 입력받아 비즈니스 수식에 따라 계산 및 정렬 후 상위 랭킹 리스트를 산출합니다.
	 * @param input 원천 데이터 리스트
	 * @return 랭킹과 점수가 부여되어 정렬된 최종 결과 리스트
	 */
	List<O> calculate(List<I> input);
}