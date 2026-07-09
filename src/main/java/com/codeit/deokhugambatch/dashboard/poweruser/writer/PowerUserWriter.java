package com.codeit.deokhugambatch.dashboard.poweruser.writer;

import com.codeit.deokhugambatch.dashboard.common.writer.DashboardWriter;
import com.codeit.deokhugambatch.dashboard.poweruser.model.PowerUserCandidate;
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

@Component("powerUserWriter")
@RequiredArgsConstructor
public class PowerUserWriter
	implements DashboardWriter<PowerUserCandidate> {

	private final JdbcTemplate jdbcTemplate;

	@Override
	public void write(List<PowerUserCandidate> candidates, Long datasetId) {

		if (candidates == null || candidates.isEmpty()) {
			return;
		}

		String sql = """
            INSERT INTO power_user (
                id,
                dataset_id,
                user_id,
                period,
                batch_date,
                ranking,
                nickname,
                score,
                like_count,
                comment_count,
                created_at
            )
            VALUES (
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(6)
            )
            """;

		jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int index)
				throws SQLException {

				PowerUserCandidate candidate = candidates.get(index);

				ps.setBytes(1, uuidToBytes(UUID.randomUUID()));
				ps.setLong(2, datasetId);
				ps.setBytes(3, uuidToBytes(candidate.getUserId()));

				ps.setString(4, candidate.getPeriod().name());
				ps.setDate(5, Date.valueOf(candidate.getBatchDate()));

				ps.setInt(6, candidate.getRanking());

				ps.setString(7, candidate.getNickname());

				ps.setBigDecimal(8, candidate.getScore());

				// 저장 스키마는 받은 좋아요 / 받은 댓글만 저장
				ps.setLong(9, candidate.getReceivedLikeCount());
				ps.setLong(10, candidate.getReceivedCommentCount());
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