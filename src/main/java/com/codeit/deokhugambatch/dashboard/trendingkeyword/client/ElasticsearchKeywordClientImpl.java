package com.codeit.deokhugambatch.dashboard.trendingkeyword.client;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import com.codeit.deokhugambatch.dashboard.trendingkeyword.model.KeywordFrequency;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.AggregationsContainer;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchKeywordClientImpl implements ElasticsearchKeywordClient {

	private static final String INDEX_NAME = "search_keywords";
	private static final String AGGREGATION_NAME = "top_keywords";

	private final ElasticsearchOperations elasticsearchOperations;

	@Override
	public List<KeywordFrequency> findTopKeywords(
		Instant from,
		Instant to
	) {

		NativeQuery query = NativeQuery.builder()
			.withQuery(q -> q
				.range(r -> r
					.date(d -> d
						.field("searchedAt")
						.gte(from.toString())
						.lte(to.toString())
					)
				)
			)
			.withMaxResults(0)
			.withAggregation(
				AGGREGATION_NAME,
				Aggregation.of(a -> a
					.terms(t -> t
						.field("keyword.keyword")
						.size(100)
					)
				)
			)
			.build();

		SearchHits<Object> searchHits =
			elasticsearchOperations.search(
				query,
				Object.class,
				IndexCoordinates.of(INDEX_NAME)
			);

		AggregationsContainer<?> container = searchHits.getAggregations();

		if (!(container instanceof ElasticsearchAggregations aggregations)) {
			return List.of();
		}

		var aggregation = aggregations.aggregationsAsMap().get(AGGREGATION_NAME);

		if (aggregation == null) {
			return List.of();
		}

		Aggregate aggregate = aggregation.aggregation().getAggregate();

		if (aggregate.sterms() == null) {
			return List.of();
		}

		List<KeywordFrequency> result = new ArrayList<>();

		aggregate.sterms().buckets().array().forEach(bucket ->
			result.add(
				KeywordFrequency.builder()
					.keyword(bucket.key().stringValue())
					.frequency(bucket.docCount())
					.build()
			)
		);

		log.info("검색 키워드 Aggregation 완료 - {}건", result.size());

		return result;
	}
}