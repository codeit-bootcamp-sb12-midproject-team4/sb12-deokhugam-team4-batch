package com.codeit.deokhugambatch.dashboard.trendingkeyword.service;

import com.codeit.deokhugambatch.dashboard.trendingkeyword.client.LlmClient;
import com.codeit.deokhugambatch.dashboard.trendingkeyword.model.KeywordFrequency;
import com.codeit.deokhugambatch.dashboard.trendingkeyword.model.LlmKeywordResult;
import com.codeit.deokhugambatch.dashboard.trendingkeyword.prompt.TrendingKeywordPrompt;
import com.codeit.deokhugambatch.dashboard.trendingkeyword.validator.LlmKeywordValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("llmKeywordService")
@RequiredArgsConstructor
public class LlmKeywordService {

	/**
	 * LLM 통신 Client
	 */
	private final LlmClient llmClient;

	/**
	 * LLM 결과 검증기
	 */
	private final LlmKeywordValidator llmKeywordValidator;

	public List<LlmKeywordResult> refineKeywords(
		List<KeywordFrequency> keywords
	) {

		if (keywords == null || keywords.isEmpty()) {
			return List.of();
		}

		log.info(
			"LLM 키워드 후처리 시작 - 입력 {}건",
			keywords.size()
		);

		// Prompt 생성
		String prompt = TrendingKeywordPrompt.createPrompt(keywords);

		// LLM 호출
		List<LlmKeywordResult> results =
			llmClient.refineKeywords(prompt);

		// 결과 검증
		List<LlmKeywordResult> validatedResults =
			llmKeywordValidator.validate(results);

		log.info(
			"LLM 키워드 후처리 완료 - 출력 {}건",
			validatedResults.size()
		);

		return validatedResults;
	}
}