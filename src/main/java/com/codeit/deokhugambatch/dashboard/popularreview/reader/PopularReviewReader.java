package com.codeit.deokhugambatch.dashboard.popularreview.reader;

import com.codeit.deokhugambatch.dashboard.common.model.DashboardPeriod;
import com.codeit.deokhugambatch.dashboard.common.reader.DashboardReader;
import com.codeit.deokhugambatch.dashboard.popularreview.model.PopularReviewCandidate;
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

@Component("popularReviewReader")
@RequiredArgsConstructor
public class PopularReviewReader implements DashboardReader<PopularReviewCandidate> {

	private final NamedParameterJdbcTemplate jdbcTemplate;

	@Override
	public List<PopularReviewCandidate> read(
		LocalDate batchDate,
		DashboardPeriod period
	) {

		LocalDateTime startDateTime = calculateStartDateTime(batchDate, period);
		LocalDateTime endDateTime = LocalDateTime.of(batchDate, LocalTime.MAX);

		String sql = """
			WITH like_stats AS (
			    SELECT
			        rl.review_id,
			        COUNT(*) AS like_count
			    FROM review_like rl
			    WHERE
			        :isAllTime = TRUE
			        OR rl.created_at BETWEEN :startDateTime AND :endDateTime
			    GROUP BY rl.review_id
			),
			
			comment_stats AS (
			    SELECT
			        c.review_id,
			        COUNT(*) AS comment_count
			    FROM comment c
			    WHERE c.deleted_at IS NULL
			      AND (
			            :isAllTime = TRUE
			            OR c.created_at BETWEEN :startDateTime AND :endDateTime
			      )
			    GROUP BY c.review_id
			)
			
			SELECT
			    r.id AS review_id,
			    b.title AS book_title,
			    b.author AS book_author,
			    b.thumbnail_key AS thumbnail_url,
			    u.nickname AS user_nickname,
			    LEFT(r.content, 300) AS review_summary,
			    r.rating AS review_rating,
			    COALESCE(ls.like_count, 0) AS like_count,
			    COALESCE(cs.comment_count, 0) AS comment_count
			
			FROM review r
			
			INNER JOIN book b
			    ON b.id = r.book_id
			
			INNER JOIN users u
			    ON u.id = r.user_id
			
			LEFT JOIN like_stats ls
			    ON ls.review_id = r.id
			
			LEFT JOIN comment_stats cs
			    ON cs.review_id = r.id
			
			WHERE r.deleted_at IS NULL
			  AND b.deleted_at IS NULL
			  AND u.deleted_at IS NULL
			  AND (
			        :isAllTime = TRUE
			        OR r.created_at BETWEEN :startDateTime AND :endDateTime
			  )
			  AND (
			        ls.like_count IS NOT NULL
			        OR cs.comment_count IS NOT NULL
			  )
			""";

		MapSqlParameterSource params = new MapSqlParameterSource()
			.addValue("startDateTime", startDateTime)
			.addValue("endDateTime", endDateTime)
			.addValue("isAllTime", period == DashboardPeriod.ALL_TIME);

		return jdbcTemplate.query(sql, params, (rs, rowNum) -> {

			PopularReviewCandidate candidate = new PopularReviewCandidate();

			candidate.setReviewId(convertBytesToUuid(rs.getBytes("review_id")));
			candidate.setBookTitle(rs.getString("book_title"));
			candidate.setBookAuthor(rs.getString("book_author"));
			candidate.setThumbnailUrl(rs.getString("thumbnail_url"));
			candidate.setUserNickname(rs.getString("user_nickname"));

			candidate.setReviewSummary(rs.getString("review_summary"));
			candidate.setReviewRating(rs.getInt("review_rating"));

			candidate.setLikeCount(rs.getLong("like_count"));
			candidate.setCommentCount(rs.getLong("comment_count"));

			candidate.setBatchDate(batchDate);
			candidate.setPeriod(period);

			return candidate;
		});
	}

	private LocalDateTime calculateStartDateTime(
		LocalDate batchDate,
		DashboardPeriod period
	) {

		return switch (period) {

			case REALTIME ->
				throw new IllegalArgumentException(
					"PopularReview는 REALTIME Period를 지원하지 않습니다."
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
