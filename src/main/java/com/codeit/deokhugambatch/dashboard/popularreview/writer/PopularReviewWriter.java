package com.codeit.deokhugambatch.dashboard.popularreview.writer;

import com.codeit.deokhugambatch.dashboard.common.writer.DashboardWriter;
import com.codeit.deokhugambatch.dashboard.popularreview.model.PopularReviewCandidate;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Component("popularReviewWriter")
@RequiredArgsConstructor
public class PopularReviewWriter
	implements DashboardWriter<PopularReviewCandidate> {

	private final JdbcTemplate jdbcTemplate;

	@Override
	public void write(List<PopularReviewCandidate> candidates, Long datasetId) {

		if (candidates == null || candidates.isEmpty()) {
			return;
		}

		String sql = """
            INSERT INTO popular_review (
                id,
                dataset_id,
                review_id,
                period,
                batch_date,
                ranking,
                book_title,
                book_author,
                thumbnail_url,
                user_nickname,
                review_summary,
                review_rating,
                score,
                like_count,
                comment_count,
                created_at
            )
            VALUES (
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(6)
            )
            """;

		jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int index)
				throws SQLException {

				PopularReviewCandidate candidate = candidates.get(index);

				ps.setBytes(1, uuidToBytes(UUID.randomUUID()));
				ps.setLong(2, datasetId);
				ps.setBytes(3, uuidToBytes(candidate.getReviewId()));

				ps.setString(4, candidate.getPeriod().name());
				ps.setDate(5, Date.valueOf(candidate.getBatchDate()));

				ps.setInt(6, candidate.getRanking());

				ps.setString(7, candidate.getBookTitle());
				ps.setString(8, candidate.getBookAuthor());
				ps.setString(9, candidate.getThumbnailUrl());

				ps.setString(10, candidate.getUserNickname());
				ps.setString(11, candidate.getReviewSummary());

				ps.setInt(12, candidate.getReviewRating());

				ps.setBigDecimal(13, candidate.getScore());

				ps.setLong(14, candidate.getLikeCount());
				ps.setLong(15, candidate.getCommentCount());
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