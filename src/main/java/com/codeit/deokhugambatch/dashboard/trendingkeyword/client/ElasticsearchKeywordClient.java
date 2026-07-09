package com.codeit.deokhugambatch.dashboard.trendingkeyword.client;

import com.codeit.deokhugambatch.dashboard.trendingkeyword.model.KeywordFrequency;
import java.time.Instant;
import java.util.List;

public interface ElasticsearchKeywordClient {

	/**
	 * 최근 검색 키워드를 Elasticsearch에서 집계하여 조회한다.
	 *
	 * @param from 조회 시작 시각(포함, UTC)
	 * @param to 조회 종료 시각(포함, UTC)
	 * @return 검색 빈도순 Top100 키워드 목록
	 */
	List<KeywordFrequency> findTopKeywords(
		Instant from,
		Instant to
	);

}