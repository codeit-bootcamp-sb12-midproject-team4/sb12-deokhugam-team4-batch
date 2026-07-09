package com.codeit.deokhugambatch.dashboard.trendingkeyword.model;

import lombok.Builder;

@Builder
public record TrendingKeywordCandidate(
	int ranking,
	String keyword,
	double score
) {
}