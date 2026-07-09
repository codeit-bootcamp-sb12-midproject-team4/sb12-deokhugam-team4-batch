package com.codeit.deokhugambatch.dashboard.trendingkeyword.writer;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrendingKeywordSnapshotWriter {

	private final JdbcTemplate jdbcTemplate;

	public Long createSnapshot() {

		String sql = """
			INSERT INTO trending_keyword_snapshot (
			    calculated_at
			)
			VALUES (
			    ?
			)
			""";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(connection -> {

			PreparedStatement ps = connection.prepareStatement(
				sql,
				Statement.RETURN_GENERATED_KEYS
			);

			ps.setTimestamp(
				1,
				Timestamp.valueOf(LocalDateTime.now())
			);

			return ps;

		}, keyHolder);

		Number generatedKey = keyHolder.getKey();

		if (generatedKey == null) {
			throw new IllegalStateException(
				"Trending Keyword Snapshot 생성에 실패했습니다."
			);
		}

		return generatedKey.longValue();
	}
}