package com.codeit.deokhugambatch.dashboard.poweruser.reader;

import com.codeit.deokhugambatch.dashboard.common.model.DashboardPeriod;
import com.codeit.deokhugambatch.dashboard.common.reader.DashboardReader;
import com.codeit.deokhugambatch.dashboard.poweruser.model.PowerUserCandidate;
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

@Component("powerUserReader")
@RequiredArgsConstructor
public class PowerUserReader implements DashboardReader<PowerUserCandidate> {

	private final NamedParameterJdbcTemplate jdbcTemplate;

	@Override
	public List<PowerUserCandidate> read(
		LocalDate batchDate,
		DashboardPeriod period
	) {

		LocalDateTime startDateTime = calculateStartDateTime(batchDate, period);
		LocalDateTime endDateTime = LocalDateTime.of(batchDate, LocalTime.MAX);

		String sql = """
			WITH review_stats AS (
			    SELECT
			        r.user_id,
			        COUNT(*) AS review_count
			    FROM review r
			    WHERE r.deleted_at IS NULL
			      AND (
			            :isAllTime = TRUE
			            OR r.created_at BETWEEN :startDateTime AND :endDateTime
			      )
			    GROUP BY r.user_id
			),

			received_like_stats AS (
			    SELECT
			        r.user_id,
			        COUNT(*) AS received_like_count
			    FROM review_like rl
			    INNER JOIN review r
			        ON r.id = rl.review_id
			    WHERE r.deleted_at IS NULL
			      AND (
			            :isAllTime = TRUE
			            OR rl.created_at BETWEEN :startDateTime AND :endDateTime
			      )
			    GROUP BY r.user_id
			),

			received_comment_stats AS (
			    SELECT
			        r.user_id,
			        COUNT(*) AS received_comment_count
			    FROM comment c
			    INNER JOIN review r
			        ON r.id = c.review_id
			    WHERE c.deleted_at IS NULL
			      AND r.deleted_at IS NULL
			      AND (
			            :isAllTime = TRUE
			            OR c.created_at BETWEEN :startDateTime AND :endDateTime
			      )
			    GROUP BY r.user_id
			),

			written_comment_stats AS (
			    SELECT
			        c.user_id,
			        COUNT(*) AS written_comment_count
			    FROM comment c
			    WHERE c.deleted_at IS NULL
			      AND (
			            :isAllTime = TRUE
			            OR c.created_at BETWEEN :startDateTime AND :endDateTime
			      )
			    GROUP BY c.user_id
			),

			given_like_stats AS (
			    SELECT
			        rl.user_id,
			        COUNT(*) AS given_like_count
			    FROM review_like rl
			    WHERE
			        :isAllTime = TRUE
			        OR rl.created_at BETWEEN :startDateTime AND :endDateTime
			    GROUP BY rl.user_id
			)

			SELECT
			    u.id AS user_id,
			    u.nickname,

			    COALESCE(rs.review_count, 0) AS review_count,
			    COALESCE(rls.received_like_count, 0) AS received_like_count,
			    COALESCE(rcs.received_comment_count, 0) AS received_comment_count,
			    COALESCE(wcs.written_comment_count, 0) AS written_comment_count,
			    COALESCE(gls.given_like_count, 0) AS given_like_count

			FROM users u

			LEFT JOIN review_stats rs
			    ON rs.user_id = u.id

			LEFT JOIN received_like_stats rls
			    ON rls.user_id = u.id

			LEFT JOIN received_comment_stats rcs
			    ON rcs.user_id = u.id

			LEFT JOIN written_comment_stats wcs
			    ON wcs.user_id = u.id

			LEFT JOIN given_like_stats gls
			    ON gls.user_id = u.id

			WHERE u.deleted_at IS NULL
			  AND (
			        rs.review_count IS NOT NULL
			        OR rls.received_like_count IS NOT NULL
			        OR rcs.received_comment_count IS NOT NULL
			        OR wcs.written_comment_count IS NOT NULL
			        OR gls.given_like_count IS NOT NULL
			  )
			""";

		MapSqlParameterSource params = new MapSqlParameterSource()
			.addValue("startDateTime", startDateTime)
			.addValue("endDateTime", endDateTime)
			.addValue("isAllTime", period == DashboardPeriod.ALL_TIME);

		return jdbcTemplate.query(sql, params, (rs, rowNum) -> {

			PowerUserCandidate candidate = new PowerUserCandidate();

			candidate.setUserId(convertBytesToUuid(rs.getBytes("user_id")));
			candidate.setNickname(rs.getString("nickname"));

			candidate.setReviewCount(rs.getLong("review_count"));
			candidate.setReceivedLikeCount(rs.getLong("received_like_count"));
			candidate.setReceivedCommentCount(rs.getLong("received_comment_count"));
			candidate.setWrittenCommentCount(rs.getLong("written_comment_count"));
			candidate.setGivenLikeCount(rs.getLong("given_like_count"));

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
					"PowerUser는 REALTIME Period를 지원하지 않습니다."
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