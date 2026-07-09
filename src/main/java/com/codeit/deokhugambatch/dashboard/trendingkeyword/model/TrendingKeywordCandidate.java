package com.codeit.deokhugambatch.dashboard.trendingkeyword.model;

import lombok.Builder;

@Builder
public record TrendingKeywordCandidate(

	/**
	 * 최종 랭킹 (1~10)
	 */
	int ranking,

	/**
	 * LLM 후처리가 완료된 대표 키워드
	 */
	String keyword,

	/**
	 * 최종 집계 점수
	 */
	double score

) {
}