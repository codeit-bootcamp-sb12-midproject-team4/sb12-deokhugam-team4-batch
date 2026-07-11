package com.codeit.deokhugambatch.dashboard.popularbook.writer;

import com.codeit.deokhugambatch.dashboard.common.writer.DashboardWriter;
import com.codeit.deokhugambatch.dashboard.popularbook.model.PopularBookCandidate;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Date;
import java.util.List;
import java.util.UUID;

@Component("popularBookWriter")
@RequiredArgsConstructor
public class PopularBookWriter
	implements DashboardWriter<PopularBookCandidate> {

	private final JdbcTemplate jdbcTemplate;

	@Override
	public void write(List<PopularBookCandidate> candidates, Long datasetId) {

		if (candidates == null || candidates.isEmpty()) {
			return;
		}

		String sql = """
            INSERT INTO popular_book (
                id,
                dataset_id,
                book_id,
                period,
                batch_date,
                ranking,
                book_title,
                author,
                thumbnail_url,
                score,
                review_count,
                like_count,
                comment_count,
                average_rating,
                created_at
            )
            VALUES (
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(6)
            )
            """;

		jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int index) throws SQLException {

				PopularBookCandidate candidate = candidates.get(index);

				ps.setBytes(1, uuidToBytes(UUID.randomUUID()));
				ps.setLong(2, datasetId);
				ps.setBytes(3, uuidToBytes(candidate.getBookId()));

				ps.setString(4, candidate.getPeriod().name());
				ps.setDate(5, Date.valueOf(candidate.getBatchDate()));

				ps.setInt(6, candidate.getRanking());

				ps.setString(7, candidate.getBookTitle());
				ps.setString(8, candidate.getAuthor());
				ps.setString(9, candidate.getThumbnailUrl());

				ps.setBigDecimal(10, candidate.getScore());

				ps.setLong(11, candidate.getReviewCount());
				ps.setLong(12, candidate.getLikeCount());
				ps.setLong(13, candidate.getCommentCount());

				ps.setBigDecimal(14, candidate.getAverageRating());
			}

			@Override
			public int getBatchSize() {
				return candidates.size();
			}
		});
	}

	/**
	 * UUID -> BINARY(16)
	 */
	private byte[] uuidToBytes(UUID uuid) {

		if (uuid == null) {
			throw new IllegalArgumentException("UUID must not be null.");
		}

		ByteBuffer buffer = ByteBuffer.allocate(16);

		buffer.putLong(uuid.getMostSignificantBits());
		buffer.putLong(uuid.getLeastSignificantBits());

		return buffer.array();
	}
}