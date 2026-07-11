package com.codeit.deokhugambatch.dashboard.trendingkeyword.prompt;

import com.codeit.deokhugambatch.dashboard.trendingkeyword.model.KeywordFrequency;
import java.util.List;
import java.util.StringJoiner;

public final class TrendingKeywordPrompt {

	private TrendingKeywordPrompt() {
	}

	//Gemini Prompt 생성
	public static String createPrompt(
		List<KeywordFrequency> keywords
	) {

		StringBuilder prompt = new StringBuilder();

		prompt.append("""
            You are a deterministic keyword normalization engine operating in a production batch pipeline.

            Your task is to clean and normalize a list of trending search keywords.

            ## Rules

            1. Merge only keywords that represent EXACTLY the same concept.

            Allowed:
            - Spacing differences
              Example:
              "클린코드" == "클린 코드"

            - Capitalization differences
              Example:
              "Java" == "java"

            - Korean / English names of the same concept
              Example:
              "자바" == "Java"

            - Very obvious one-character typographical errors

            - Singular / plural forms ONLY when they clearly represent the same concept.

            --------------------------------------------------

            Never merge:

            - Parent / child concepts
            - Broader / narrower concepts
            - Related technologies
            - Different products
            - Different frameworks
            - Different libraries
            - Different abbreviations

            --------------------------------------------------

            Remove:

            - Profanity
            - Offensive words
            - Spam
            - Advertisement keywords
            - Abuse keywords
            - Meaningless random strings

            --------------------------------------------------

            Score Rule

            When multiple keywords are merged into one concept:

            - representative keyword MUST be one of the original keywords.
            - representative keyword should prefer Korean.
            - if Korean does not exist, use the most common original spelling.
            - score MUST be the SUM of every merged keyword score.

            --------------------------------------------------

            Output Rules

            - Return ONLY a raw JSON array.
            - Do NOT include markdown.
            - Do NOT include explanation.
            - Do NOT include comments.
            - Do NOT include ```json.
            - Output size may be smaller than the input.
            - Do NOT generate new keywords.
            - Every keyword must originate from the input.

            JSON format:

            [
              {
                "keyword":"대표키워드",
                "score":123
              }
            ]

            Input:

            [
            """);

		StringJoiner joiner = new StringJoiner(",\n");

		for (KeywordFrequency keyword : keywords) {

			joiner.add("""
                {
                  "keyword":"%s",
                  "score":%d
                }
                """.formatted(
				escape(keyword.keyword()),
				keyword.frequency()
			));
		}

		prompt.append(joiner);

		prompt.append("""
            
            ]
            """);

		return prompt.toString();
	}

	//JSON 문자열 Escape
	private static String escape(String value) {

		if (value == null) {
			return "";
		}

		return value
			.replace("\\", "\\\\")
			.replace("\"", "\\\"");
	}
}
