package com.codeit.deokhugambatch.dashboard.trendingkeyword.calculator;

import com.codeit.deokhugambatch.dashboard.trendingkeyword.model.LlmKeywordResult;
import com.codeit.deokhugambatch.dashboard.trendingkeyword.model.TrendingKeywordCandidate;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("trendingKeywordCalculator")
public class TrendingKeywordCalculator {


	public List<TrendingKeywordCandidate> calculate(
		List<LlmKeywordResult> results
	) {

		if (results == null || results.isEmpty()) {
			log.info("계산할 트렌딩 키워드가 없습니다.");
			return List.of();
		}


		List<LlmKeywordResult> sortedResults =
			results.stream()
				.sorted(
					Comparator
						.comparingDouble(LlmKeywordResult::score)
						.reversed()
						.thenComparing(LlmKeywordResult::keyword)
				)
				.limit(10)
				.toList();


		List<TrendingKeywordCandidate> candidates =
			IntStream.range(0, sortedResults.size())
				.mapToObj(index -> {

					LlmKeywordResult result =
						sortedResults.get(index);


					return TrendingKeywordCandidate.builder()
						.ranking(index + 1)
						.keyword(result.keyword())
						.score(result.score())
						.build();
				})
				.toList();


		log.info(
			"트렌딩 키워드 계산 완료 - {}건",
			candidates.size()
		);


		return candidates;
	}
}