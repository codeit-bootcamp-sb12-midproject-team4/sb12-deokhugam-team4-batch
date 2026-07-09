package com.codeit.deokhugambatch.dashboard.trendingkeyword.validator;

import com.codeit.deokhugambatch.dashboard.trendingkeyword.model.LlmKeywordResult;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LlmKeywordValidator {

	public List<LlmKeywordResult> validate(
		List<LlmKeywordResult> results
	) {

		if (results == null || results.isEmpty()) {
			throw new IllegalStateException("LLM 결과가 비어 있습니다.");
		}

		List<LlmKeywordResult> validated = results.stream()

			.filter(result -> result != null)

			.filter(result ->
				result.keyword() != null &&
					!result.keyword().isBlank()
			)

			.filter(result -> result.score() > 0)

			.collect(
				LinkedHashMap<String, LlmKeywordResult>::new,
				(map, result) -> map.putIfAbsent(
					result.keyword().trim(),
					result
				),
				LinkedHashMap::putAll
			)

			.values()

			.stream()

			.sorted(
				Comparator.comparingDouble(
					LlmKeywordResult::score
				).reversed()
			)

			.limit(10)

			.toList();

		if (validated.isEmpty()) {
			throw new IllegalStateException(
				"유효한 LLM 결과가 존재하지 않습니다."
			);
		}

		log.info(
			"LLM 결과 검증 완료 - {}건",
			validated.size()
		);

		return validated;
	}
}