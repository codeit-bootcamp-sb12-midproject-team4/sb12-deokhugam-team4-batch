package com.codeit.deokhugambatch.dashboard.trendingkeyword.document;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Document(indexName = "search_keywords")
public class SearchKeywordDocument {

	@Id
	private String id;

	//사용자가 검색한 키워드
	@Field(type = FieldType.Keyword)
	private String keyword;

	//검색이 수행된 시각
	@Field(type = FieldType.Date, format = DateFormat.date_time)
	private Instant searchedAt;
}