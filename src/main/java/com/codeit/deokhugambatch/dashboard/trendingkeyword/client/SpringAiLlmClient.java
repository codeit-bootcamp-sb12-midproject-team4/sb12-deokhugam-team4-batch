package com.codeit.deokhugambatch.dashboard.trendingkeyword.client;

import com.codeit.deokhugambatch.dashboard.trendingkeyword.model.LlmKeywordResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringAiLlmClient implements LlmClient {

	private final ChatClient chatClient;

	private final ObjectMapper objectMapper;

	@Override
	public List<LlmKeywordResult> refineKeywords(String prompt) {

		log.info("Gemini LLM 호출 시작");

		try {

			String response = chatClient
				.prompt()
				.user(prompt)
				.call()
				.content();

			if (response == null || response.isBlank()) {
				throw new IllegalStateException("LLM 응답이 비어 있습니다.");
			}

			String json = extractJson(response);

			List<LlmKeywordResult> results =
				objectMapper.readValue(
					json,
					new TypeReference<>() {
					}
				);

			log.info(
				"Gemini LLM 호출 완료 - {}건",
				results.size()
			);

			return results;

		} catch (Exception e) {

			Throwable rootCause = e;
			while (rootCause.getCause() != null && rootCause != rootCause.getCause()) {
				rootCause = rootCause.getCause();
			}

			log.error("🔥🔥🔥 구글 API가 뱉은 진짜 에러 원인: {}", rootCause.getMessage());
			// ---------------------------------------------------------

			log.error("Gemini LLM 호출 실패", e);

			throw new IllegalStateException(
				"Gemini LLM 호출에 실패했습니다.",
				e
			);
		}
	}

	private String extractJson(String response) {

		String json = response.trim();

		if (!json.startsWith("```")) {
			return json;
		}

		// ```json, ```JSON, ```Json 등 모두 처리
		json = json.replaceFirst("(?i)^```json\\s*", "");
		json = json.replaceFirst("^```\\s*", "");

		if (json.endsWith("```")) {
			json = json.substring(
				0,
				json.lastIndexOf("```")
			);
		}

		return json.trim();
	}
}