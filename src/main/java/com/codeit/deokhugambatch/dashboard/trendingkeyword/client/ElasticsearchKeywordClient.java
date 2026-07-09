package com.codeit.deokhugambatch.dashboard.trendingkeyword.client;

import com.codeit.deokhugambatch.dashboard.trendingkeyword.model.KeywordFrequency;
import java.time.LocalDateTime;
import java.util.List;

public interface ElasticsearchKeywordClient {

	/**
	 * @param from 조회 시작 시각(포함)
	 * @param to 조회 종료 시각(포함)
	 * @return Elasticsearch Aggregation 결과
	 */
	List<KeywordFrequency> findTopKeywords(
		LocalDateTime from,
		LocalDateTime to
	);

}