package com.codeit.deokhugambatch.dashboard.popularreview.calculator;

import com.codeit.deokhugambatch.dashboard.common.calculator.DashboardCalculator;
import com.codeit.deokhugambatch.dashboard.popularreview.model.PopularReviewCandidate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Component("popularReviewCalculator")
public class PopularReviewCalculator
	implements DashboardCalculator<PopularReviewCandidate, PopularReviewCandidate> {

	/**
	 * 평점 가중치
	 */
	private static final double RATING_WEIGHT = 0.30;

	/**
	 * 좋아요 가중치
	 */
	private static final double LIKE_WEIGHT = 0.50;

	/**
	 * 댓글 가중치
	 */
	private static final double COMMENT_WEIGHT = 0.20;

	/**
	 * 최대 랭킹
	 */
	private static final int MAX_RANKING = 50;

	@Override
	public List<PopularReviewCandidate> calculate(List<PopularReviewCandidate> candidates) {

		if (candidates == null || candidates.isEmpty()) {
			return List.of();
		}

		long maxLikeCount = candidates.stream()
			.mapToLong(PopularReviewCandidate::getLikeCount)
			.max()
			.orElse(1L);

		long maxCommentCount = candidates.stream()
			.mapToLong(PopularReviewCandidate::getCommentCount)
			.max()
			.orElse(1L);

		for (PopularReviewCandidate candidate : candidates) {

			double ratingScore =
				candidate.getReviewRating() / 5.0;

			double likeScore =
				normalizeLog(candidate.getLikeCount(), maxLikeCount);

			double commentScore =
				normalizeLog(candidate.getCommentCount(), maxCommentCount);

			double finalScore =
				(ratingScore * RATING_WEIGHT)
					+ (likeScore * LIKE_WEIGHT)
					+ (commentScore * COMMENT_WEIGHT);

			candidate.setScore(
				BigDecimal.valueOf(finalScore)
					.setScale(2, RoundingMode.HALF_UP)
			);
		}

		List<PopularReviewCandidate> rankedCandidates = candidates.stream()
			.sorted(
				Comparator
					.comparing(PopularReviewCandidate::getScore, Comparator.reverseOrder())
					.thenComparing(PopularReviewCandidate::getLikeCount, Comparator.reverseOrder())
					.thenComparing(PopularReviewCandidate::getCommentCount, Comparator.reverseOrder())
					.thenComparing(PopularReviewCandidate::getReviewRating, Comparator.reverseOrder())
					.thenComparing(PopularReviewCandidate::getReviewId)
			)
			.limit(MAX_RANKING)
			.toList();

		for (int i = 0; i < rankedCandidates.size(); i++) {
			rankedCandidates.get(i).setRanking(i + 1);
		}

		return rankedCandidates;
	}

	/**
	 * 로그 기반 정규화
	 *
	 * 결과 범위 : 0.0 ~ 1.0
	 */
	private double normalizeLog(long value, long maxValue) {

		if (maxValue <= 0) {
			return 0.0;
		}

		return Math.log(value + 1.0)
			/ Math.log(maxValue + 1.0);
	}
}