package com.codeit.deokhugambatch.dashboard.trendingkeyword.client;

import com.codeit.deokhugambatch.dashboard.trendingkeyword.model.KeywordFrequency;
import com.codeit.deokhugambatch.dashboard.trendingkeyword.model.LlmKeywordResult;
import java.util.List;

public interface LlmClient {
	List<LlmKeywordResult> refineKeywords(String prompt);
}