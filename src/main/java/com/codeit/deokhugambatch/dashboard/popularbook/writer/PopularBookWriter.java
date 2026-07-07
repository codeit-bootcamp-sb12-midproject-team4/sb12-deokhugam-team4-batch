package com.codeit.deokhugambatch.dashboard.popularbook.writer;

import com.codeit.deokhugambatch.dashboard.common.writer.DashboardWriter;
import com.codeit.deokhugambatch.dashboard.popularbook.model.PopularBookCandidate;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PopularBookWriter implements DashboardWriter<PopularBookCandidate> {

	private final JdbcTemplate jdbcTemplate;

	@Override
	@Transactional
	public void write(List<PopularBookCandidate> items, Long datasetId) {
		if (items == null || items.isEmpty()) {
			return;
		}

		String sql = """
            INSERT INTO popular_book (
                id, dataset_id, book_id, period, batch_date, ranking, 
                book_title, author, thumbnail_url, score, 
                review_count, like_count, comment_count, average_rating, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(6))
        """;

		jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				PopularBookCandidate item = items.get(i);

				// UUID를 MySQL BINARY(16)에 적합한 byte 배열로 변환
				UUID newId = UUID.randomUUID();
				ps.setBytes(1, convertUUIDToBytes(newId));
				ps.setLong(2, datasetId);
				ps.setBytes(3, convertUUIDToBytes(item.getBookId()));
				ps.setString(4, item.getPeriod().name());
				ps.setDate(5, java.sql.Date.valueOf(item.getBatchDate()));
				ps.setInt(6, item.getRanking());
				ps.setString(7, item.getBookTitle());
				ps.setString(8, item.getAuthor());
				ps.setString(9, item.getThumbnailUrl());
				ps.setBigDecimal(10, item.getScore());
				ps.setLong(11, item.getReviewCount());
				ps.setLong(12, item.getLikeCount());
				ps.setLong(13, item.getCommentCount());
				ps.setDouble(14, item.getAverageRating());
			}

			@Override
			public int getBatchSize() {
				return items.size();
			}
		});
	}

	private byte[] convertUUIDToBytes(UUID uuid) {
		if (uuid == null) return new byte[16];
		java.nio.ByteBuffer bb = java.nio.ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return bb.array();
	}
}