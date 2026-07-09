package com.codeit.deokhugambatch.dashboard.trendingkeyword.writer;

import com.codeit.deokhugambatch.dashboard.common.writer.DashboardWriter;
import com.codeit.deokhugambatch.dashboard.trendingkeyword.model.TrendingKeywordCandidate;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component("trendingKeywordWriter")
@RequiredArgsConstructor
public class TrendingKeywordWriter
	implements DashboardWriter<TrendingKeywordCandidate> {

	private final JdbcTemplate jdbcTemplate;

	@Override
	public void write(
		List<TrendingKeywordCandidate> candidates,
		Long datasetId
	) {

		if (candidates == null || candidates.isEmpty()) {
			return;
		}

		String sql = """
			INSERT INTO trending_keyword (
			    dataset_id,
			    ranking,
			    keyword,
			    score
			)
			VALUES (
			    ?, ?, ?, ?
			)
			""";

		jdbcTemplate.batchUpdate(
			sql,
			new BatchPreparedStatementSetter() {

				@Override
				public void setValues(
					PreparedStatement ps,
					int index
				) throws SQLException {

					TrendingKeywordCandidate candidate =
						candidates.get(index);

					ps.setLong(1, datasetId);
					ps.setInt(2, candidate.ranking());
					ps.setString(3, candidate.keyword());
					ps.setDouble(4, candidate.score());
				}

				@Override
				public int getBatchSize() {
					return candidates.size();
				}
			}
		);
	}
}