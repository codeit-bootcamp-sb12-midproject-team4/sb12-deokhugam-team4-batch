package com.codeit.deokhugambatch.dashboard.poweruser.calculator;

import com.codeit.deokhugambatch.dashboard.common.calculator.DashboardCalculator;
import com.codeit.deokhugambatch.dashboard.poweruser.model.UserActionLedger; // 유저별 액션 건수 DTO 가정
import com.codeit.deokhugambatch.dashboard.poweruser.model.PowerUserCandidate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Component
public class PowerUserCalculator implements DashboardCalculator<UserActionLedger, PowerUserCandidate> {

	private static final int MAX_RANKING_LIMIT = 10; // 파워유저는 스키마 제약상 10명 고정

	// 가중치 상수 정의
	private static final long POINT_REVIEW_CREATED = 50;
	private static final long POINT_COMMENT_RECEIVED = 15;
	private static final long POINT_LIKE_RECEIVED = 10;
	private static final long POINT_COMMENT_CREATED = 5;
	private static final long POINT_LIKE_CLICKED = 1;

	@Override
	public List<PowerUserCandidate> calculate(List<UserActionLedger> input) {
		if (input == null || input.isEmpty()) {
			return List.of();
		}

		// 1. 액션 포인트 합산 시스템 엔진 구동
		List<PowerUserCandidate> scoredUsers = input.stream()
			.map(ledger -> {
				long totalScore = (ledger.getReviewCount() * POINT_REVIEW_CREATED)
					+ (ledger.getCommentReceivedCount() * POINT_COMMENT_RECEIVED)
					+ (ledger.getLikeReceivedCount() * POINT_LIKE_RECEIVED)
					+ (ledger.getCommentCreatedCount() * POINT_COMMENT_CREATED)
					+ (ledger.getLikeClickedCount() * POINT_LIKE_CLICKED);

				PowerUserCandidate candidate = new PowerUserCandidate();
				candidate.setUserId(ledger.getUserId());
				candidate.setNickname(ledger.getNickname());
				candidate.setLikeCount(ledger.getLikeReceivedCount());     // 스키마 저장용 명세 매핑
				candidate.setCommentCount(ledger.getCommentReceivedCount()); // 스키마 저장용 명세 매핑
				candidate.setScore(BigDecimal.valueOf(totalScore).setScale(2, RoundingMode.HALF_UP));
				return candidate;
			})
			.sorted(Comparator.comparing(PowerUserCandidate::getScore).reversed())
			.toList();

		// 2. 순위 부여 및 정확히 Top 10 하드 가이딩 필터링
		return IntStream.range(0, Math.min(scoredUsers.size(), MAX_RANKING_LIMIT))
			.mapToObj(i -> {
				PowerUserCandidate user = scoredUsers.get(i);
				user.setRanking(i + 1); // 1위부터 10위까지 보장 (chk_power_user_ranking 제약 충족)
				return user;
			})
			.toList();
	}
}