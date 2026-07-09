package com.codeit.deokhugambatch.book.writer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Component;

import com.codeit.deokhugamcommon.domain.book.Book;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BookEsUpdateWriter implements ItemWriter<Book> {
	private final ElasticsearchOperations elasticsearchOperations;
	private static final String INDEX_NAME = "books";

	@Override
	public void write(Chunk<? extends Book> chunk) throws Exception {
		List<UpdateQuery> updateQueries = chunk.getItems().stream()
			.map(book -> {
				Map<String, Object> updateFields = new HashMap<>();
				updateFields.put("reviewCount", book.getReviewCount());
				updateFields.put("rating", book.getRating());

				return UpdateQuery.builder(book.getId().toString())
					.withDocument(Document.from(updateFields))
					.withDocAsUpsert(true)
					.build();
			})
			.collect(Collectors.toList());

		if (!updateQueries.isEmpty()) {
			// 3. Bulk API를 통해 한 번에 네트워크 효율적으로 업데이트 반영
			elasticsearchOperations.bulkUpdate(updateQueries, IndexCoordinates.of(INDEX_NAME));
		}
	}
}
