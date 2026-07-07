package com.codeit.deokhugambatch.dashboard.common.model;

/**
 * Dashboard 통계 집계 주기를 정의하는 공통 Enum 클래스입니다.
 * DB 스키마의 period ENUM('DAILY','WEEKLY','MONTHLY','ALL_TIME') 규격과 일치합니다.
 */
public enum DashboardPeriod {
	REALTIME,
	DAILY,
	WEEKLY,
	MONTHLY,
	ALL_TIME
}