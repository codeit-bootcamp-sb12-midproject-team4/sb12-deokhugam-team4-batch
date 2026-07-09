package com.codeit.deokhugambatch.dashboard.trendingkeyword.model;

import lombok.Builder;

@Builder
public record KeywordFrequency(

	/**
	 * Elasticsearch에서 집계된 검색 키워드
	 */
	String keyword,

	/**
	 * 검색 빈도(검색 횟수)
	 */
	long frequency

) {
}