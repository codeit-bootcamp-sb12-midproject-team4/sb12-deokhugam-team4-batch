package com.codeit.deokhugambatch.dashboard.trendingkeyword.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import com.codeit.deokhugambatch.dashboard.trendingkeyword.document.SearchKeywordDocument;

//검색 키워드 로그 Elasticsearch Repository
public interface SearchKeywordRepository
	extends ElasticsearchRepository<SearchKeywordDocument, String> {
}