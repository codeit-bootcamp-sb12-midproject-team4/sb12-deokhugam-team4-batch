package com.codeit.deokhugambatch.dashboard.popularbook.reader;

import com.codeit.deokhugambatch.dashboard.common.model.DashboardPeriod;
import com.codeit.deokhugambatch.dashboard.common.reader.DashboardReader;
import com.codeit.deokhugambatch.dashboard.popularbook.model.PopularBookCandidate;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PopularBookReader implements DashboardReader<PopularBookCandidate> {

	private final NamedParameterJdbcTemplate jdbcTemplate;

	@Override
	public List<PopularBookCandidate> read(LocalDate batchDate, DashboardPeriod period) {
		// 1. 주기(Period)에 따른 조회 시간 윈도우 계산
		LocalDateTime endDateTime = LocalDateTime.of(batchDate, LocalTime.MAX);
		LocalDateTime startDateTime = calculateStartTime(batchDate, period);

		// 2. 비즈니스 쿼리 작성 (Soft Delete 고려: deleted_at IS NULL)
		String sql = """
            SELECT 
                b.id AS book_id,
                b.title AS book_title,
                b.author AS author,
                b.thumbnail_key AS thumbnail_url,
                COUNT(DISTINCT r.id) AS review_count,
                COALESCE(AVG(r.rating), 0.0) AS average_rating,
                COUNT(DISTINCT rl.id) AS like_count,
                COUNT(DISTINCT c.id) AS comment_count
            FROM book b
            LEFT JOIN review r ON r.book_id = b.id AND r.deleted_at IS NULL
            LEFT JOIN review_like rl ON rl.review_id = r.id AND (:isAllTime = TRUE OR rl.created_at BETWEEN :startDateTime AND :endDateTime)
            LEFT JOIN comment c ON c.review_id = r.id AND c.deleted_at IS NULL AND (:isAllTime = TRUE OR c.created_at BETWEEN :startDateTime AND :endDateTime)
            WHERE b.deleted_at IS NULL
            GROUP BY b.id, b.title, b.author, b.thumbnail_key
        """;

		MapSqlParameterSource params = new MapSqlParameterSource()
			.addValue("startDateTime", startDateTime)
			.addValue("endDateTime", endDateTime)
			.addValue("isAllTime", period == DashboardPeriod.ALL_TIME);

		// 3. RowMapper 매핑 (BINARY(16) -> UUID 변환 포함)
		return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
			PopularBookCandidate candidate = new PopularBookCandidate();
			candidate.setBookId(convertBytesToUUID(rs.getBytes("book_id")));
			candidate.setBookTitle(rs.getString("book_title"));
			candidate.setAuthor(rs.getString("author"));
			candidate.setThumbnailUrl(rs.getString("thumbnail_url"));
			candidate.setReviewCount(rs.getLong("review_count"));
			candidate.setAverageRating(rs.getDouble("average_rating"));
			candidate.setLikeCount(rs.getLong("like_count"));
			candidate.setCommentCount(rs.getString("comment_count") != null ? rs.getLong("comment_count") : 0L);
			candidate.setPeriod(period);
			candidate.setBatchDate(batchDate);
			return candidate;
		});
	}

	private LocalDateTime calculateStartTime(LocalDate batchDate, DashboardPeriod period) {
		return switch (period) {
			case DAILY -> LocalDateTime.of(batchDate, LocalTime.MIN);
			case WEEKLY -> LocalDateTime.of(batchDate.minusDays(6), LocalTime.MIN);
			case MONTHLY -> LocalDateTime.of(batchDate.minusMonths(1), LocalTime.MIN);
			case ALL_TIME -> LocalDateTime.of(1970, 1, 1, 0, 0);
		};
	}

	private UUID convertBytesToUUID(byte[] bytes) {
		if (bytes == null || bytes.length != 16) return null;
		java.nio.ByteBuffer byteBuffer = java.nio.ByteBuffer.wrap(bytes);
		return new UUID(byteBuffer.getLong(), byteBuffer.getLong());
	}
}