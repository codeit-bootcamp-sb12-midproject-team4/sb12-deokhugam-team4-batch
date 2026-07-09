package com.codeit.deokhugambatch.dashboard.poweruser.calculator;

import com.codeit.deokhugambatch.dashboard.common.calculator.DashboardCalculator;
import com.codeit.deokhugambatch.dashboard.poweruser.model.PowerUserCandidate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Component("powerUserCalculator")
public class PowerUserCalculator
	implements DashboardCalculator<PowerUserCandidate, PowerUserCandidate> {

	/**
	 * 리뷰 작성
	 */
	private static final long POINT_REVIEW_CREATED = 50L;

	/**
	 * 받은 댓글
	 */
	private static final long POINT_COMMENT_RECEIVED = 15L;

	/**
	 * 받은 좋아요
	 */
	private static final long POINT_LIKE_RECEIVED = 10L;

	/**
	 * 댓글 작성
	 */
	private static final long POINT_COMMENT_CREATED = 5L;

	/**
	 * 좋아요 클릭
	 */
	private static final long POINT_LIKE_CLICKED = 1L;

	/**
	 * 최대 랭킹
	 */
	private static final int MAX_RANKING = 10;

	@Override
	public List<PowerUserCandidate> calculate(
		List<PowerUserCandidate> candidates
	) {

		if (candidates == null || candidates.isEmpty()) {
			return List.of();
		}

		for (PowerUserCandidate candidate : candidates) {

			long totalScore =
				(candidate.getReviewCount() * POINT_REVIEW_CREATED)
					+ (candidate.getReceivedCommentCount() * POINT_COMMENT_RECEIVED)
					+ (candidate.getReceivedLikeCount() * POINT_LIKE_RECEIVED)
					+ (candidate.getWrittenCommentCount() * POINT_COMMENT_CREATED)
					+ (candidate.getGivenLikeCount() * POINT_LIKE_CLICKED);

			candidate.setScore(
				BigDecimal.valueOf(totalScore)
					.setScale(2, RoundingMode.HALF_UP)
			);
		}

		List<PowerUserCandidate> rankedCandidates = candidates.stream()
			.sorted(
				Comparator
					.comparing(PowerUserCandidate::getScore, Comparator.reverseOrder())
					.thenComparing(
						PowerUserCandidate::getReceivedLikeCount,
						Comparator.reverseOrder()
					)
					.thenComparing(
						PowerUserCandidate::getReceivedCommentCount,
						Comparator.reverseOrder()
					)
					.thenComparing(
						PowerUserCandidate::getReviewCount,
						Comparator.reverseOrder()
					)
					.thenComparing(PowerUserCandidate::getUserId)
			)
			.limit(MAX_RANKING)
			.toList();

		for (int i = 0; i < rankedCandidates.size(); i++) {
			rankedCandidates.get(i).setRanking(i + 1);
		}

		return rankedCandidates;
	}
}