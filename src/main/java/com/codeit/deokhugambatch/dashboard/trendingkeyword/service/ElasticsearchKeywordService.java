package com.codeit.deokhugambatch.dashboard.trendingkeyword.service;

import com.codeit.deokhugambatch.dashboard.trendingkeyword.client.ElasticsearchKeywordClient;
import com.codeit.deokhugambatch.dashboard.trendingkeyword.model.KeywordFrequency;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("elasticsearchKeywordService")
@RequiredArgsConstructor
public class ElasticsearchKeywordService {

	/**
	 * Elasticsearch 조회 Client
	 */
	private final ElasticsearchKeywordClient elasticsearchKeywordClient;

	/**
	 * 최근 3시간 검색 키워드 Top100 조회
	 * @return 검색 빈도순 Top100 키워드 목록
	 */
	public List<KeywordFrequency> getTopKeywords() {

		LocalDateTime to = LocalDateTime.now();
		LocalDateTime from = to.minusHours(3);

		log.info(
			"Elasticsearch 검색 키워드 조회 시작 - from={}, to={}",
			from,
			to
		);

		List<KeywordFrequency> keywords =
			elasticsearchKeywordClient.findTopKeywords(from, to);

		log.info(
			"Elasticsearch 검색 키워드 조회 완료 - {}건",
			keywords.size()
		);

		return keywords;
	}
}