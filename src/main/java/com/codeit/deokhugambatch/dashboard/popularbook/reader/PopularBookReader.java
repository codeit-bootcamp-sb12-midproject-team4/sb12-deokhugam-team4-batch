package com.codeit.deokhugambatch.dashboard.popularbook.reader;

import com.codeit.deokhugambatch.dashboard.common.model.DashboardPeriod;
import com.codeit.deokhugambatch.dashboard.common.reader.DashboardReader;
import com.codeit.deokhugambatch.dashboard.popularbook.model.PopularBookCandidate;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Component("popularBookReader")
@RequiredArgsConstructor
public class PopularBookReader implements DashboardReader<PopularBookCandidate> {

	private final NamedParameterJdbcTemplate jdbcTemplate;

	@Override
	public List<PopularBookCandidate> read(LocalDate batchDate, DashboardPeriod period) {

		LocalDateTime startDateTime = calculateStartDateTime(batchDate, period);
		LocalDateTime endDateTime = LocalDateTime.of(batchDate, LocalTime.MAX);

		String sql = """
			WITH review_stats AS (
			    SELECT
			        r.book_id,
			        COUNT(*) AS review_count,
			        AVG(r.rating) AS average_rating
			    FROM review r
			    WHERE r.deleted_at IS NULL
			      AND (
			            :isAllTime = TRUE
			            OR r.created_at BETWEEN :startDateTime AND :endDateTime
			      )
			    GROUP BY r.book_id
			),
			
			like_stats AS (
			    SELECT
			        r.book_id,
			        COUNT(*) AS like_count
			    FROM review_like rl
			    INNER JOIN review r
			        ON r.id = rl.review_id
			    WHERE r.deleted_at IS NULL
			      AND (
			            :isAllTime = TRUE
			            OR rl.created_at BETWEEN :startDateTime AND :endDateTime
			      )
			    GROUP BY r.book_id
			),
			
			comment_stats AS (
			    SELECT
			        r.book_id,
			        COUNT(*) AS comment_count
			    FROM comment c
			    INNER JOIN review r
			        ON r.id = c.review_id
			    WHERE c.deleted_at IS NULL
			      AND r.deleted_at IS NULL
			      AND (
			            :isAllTime = TRUE
			            OR c.created_at BETWEEN :startDateTime AND :endDateTime
			      )
			    GROUP BY r.book_id
			)
			
			SELECT
			    b.id AS book_id,
			    b.title AS book_title,
			    b.author,
			    b.thumbnail_key AS thumbnail_url,
			
			    COALESCE(rs.review_count, 0) AS review_count,
			    COALESCE(rs.average_rating, 0.0) AS average_rating,
			    COALESCE(ls.like_count, 0) AS like_count,
			    COALESCE(cs.comment_count, 0) AS comment_count
			
			FROM book b
			
			LEFT JOIN review_stats rs
			       ON rs.book_id = b.id
			
			LEFT JOIN like_stats ls
			       ON ls.book_id = b.id
			
			LEFT JOIN comment_stats cs
			       ON cs.book_id = b.id
			
			WHERE b.deleted_at IS NULL
			      AND (
			            rs.review_count IS NOT NULL
			            OR ls.like_count IS NOT NULL
			            OR cs.comment_count IS NOT NULL
			      )
			""";

		MapSqlParameterSource params = new MapSqlParameterSource()
			.addValue("startDateTime", startDateTime)
			.addValue("endDateTime", endDateTime)
			.addValue("isAllTime", period == DashboardPeriod.ALL_TIME);

		return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
			PopularBookCandidate candidate = new PopularBookCandidate();

			candidate.setBookId(convertBytesToUuid(rs.getBytes("book_id")));
			candidate.setBookTitle(rs.getString("book_title"));
			candidate.setAuthor(rs.getString("author"));
			candidate.setThumbnailUrl(rs.getString("thumbnail_url"));

			candidate.setReviewCount(rs.getLong("review_count"));
			candidate.setAverageRating(rs.getDouble("average_rating"));
			candidate.setLikeCount(rs.getLong("like_count"));
			candidate.setCommentCount(rs.getLong("comment_count"));

			candidate.setPeriod(period);
			candidate.setBatchDate(batchDate);

			return candidate;
		});
	}

	private LocalDateTime calculateStartDateTime(LocalDate batchDate, DashboardPeriod period) {
		return switch (period) {
			case REALTIME ->
				throw new IllegalArgumentException(
					"PopularBook는 REALTIME Period를 지원하지 않습니다."
				);
			case DAILY ->
				batchDate.atStartOfDay();
			case WEEKLY ->
				batchDate.minusDays(6).atStartOfDay();
			case MONTHLY ->
				batchDate.withDayOfMonth(1).atStartOfDay();
			case ALL_TIME ->
				LocalDateTime.of(1970, 1, 1, 0, 0);
		};
	}

	private UUID convertBytesToUuid(byte[] bytes) {
		if (bytes == null || bytes.length != 16) {
			return null;
		}
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		return new UUID(
			buffer.getLong(),
			buffer.getLong()
		);
	}
}