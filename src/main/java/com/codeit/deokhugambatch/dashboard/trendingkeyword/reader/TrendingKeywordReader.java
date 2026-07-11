package com.codeit.deokhugambatch.dashboard.trendingkeyword.reader;

import com.codeit.deokhugambatch.dashboard.common.model.DashboardPeriod;
import com.codeit.deokhugambatch.dashboard.common.reader.DashboardReader;
import com.codeit.deokhugambatch.dashboard.trendingkeyword.model.KeywordFrequency;
import com.codeit.deokhugambatch.dashboard.trendingkeyword.service.ElasticsearchKeywordService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("trendingKeywordReader")
@RequiredArgsConstructor
public class TrendingKeywordReader
	implements DashboardReader<KeywordFrequency> {

	/**
	 * Elasticsearch 조회 서비스
	 */
	private final ElasticsearchKeywordService elasticsearchKeywordService;

	/**
	 * @param batchDate 배치 실행 기준일(인터페이스 호환용)
	 * @param period 집계 주기 (REALTIME만 허용)
	 * @return Elasticsearch Aggregation 결과
	 */
	@Override
	public List<KeywordFrequency> read(
		LocalDate batchDate,
		DashboardPeriod period
	) {

		if (period != DashboardPeriod.REALTIME) {
			throw new IllegalArgumentException(
				"TrendingKeywordReader는 REALTIME Period만 지원합니다."
			);
		}

		log.info(
			"TrendingKeywordReader 시작 - batchDate={}, period={}",
			batchDate,
			period
		);

		List<KeywordFrequency> keywords =
			elasticsearchKeywordService.getTopKeywords();

		log.info(
			"TrendingKeywordReader 완료 - {}건 조회",
			keywords.size()
		);

		return keywords;
	}
}