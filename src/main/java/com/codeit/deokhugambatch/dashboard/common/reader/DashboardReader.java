package com.codeit.deokhugambatch.dashboard.common.reader;

import com.codeit.deokhugambatch.dashboard.common.model.DashboardPeriod;
import java.time.LocalDate;
import java.util.List;

/**
 * Dashboard 통계 및 랭킹 산출을 위한 원천 데이터 조회 공통 인터페이스입니다.
 * <p>
 * Tasklet 내부에서 호출되며, 배치 기준일(batchDate)과 집계 주기(period)를 기반으로
 * 계산(Calculator) 레이어에 전달할 대량의 원천 데이터 또는 1차 집계 후보군(Candidate) 리스트를 조회합니다.
 * @param <I> 집계 대상이 되는 인풋 데이터 타입 (예: 엔티티 또는 QueryDSL Projection DTO)
 */
public interface DashboardReader<I> {

	/**
	 * 지정된 배치 기준일과 집계 주기에 부합하는 조회 기간을 산출하여 원천 데이터를 조회합니다.
	 * @param batchDate 배치 실행 기준 날짜 (Tasklet의 JobExecutionContext 등에서 전달받음)
	 * @param period    집계 주기 (DAILY, WEEKLY, MONTHLY, ALL_TIME)
	 * @return 집계 및 스코어링 대상 데이터 목록 (데이터가 존재하지 않을 경우 빈 리스트 반환)
	 */
	List<I> read(LocalDate batchDate, DashboardPeriod period);
}