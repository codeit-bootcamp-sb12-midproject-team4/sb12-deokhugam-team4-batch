package com.codeit.deokhugambatch.dashboard.popularbook.calculator;

import com.codeit.deokhugambatch.dashboard.common.calculator.DashboardCalculator;
import com.codeit.deokhugambatch.dashboard.popularbook.model.PopularBookCandidate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Component("popularBookCalculator")
public class PopularBookCalculator
	implements DashboardCalculator<PopularBookCandidate, PopularBookCandidate> {

	/**
	 * 평점 가중치
	 */
	private static final double RATING_WEIGHT = 0.45;

	/**
	 * 리뷰 수 가중치
	 */
	private static final double REVIEW_WEIGHT = 0.25;

	/**
	 * 좋아요 수 가중치
	 */
	private static final double LIKE_WEIGHT = 0.20;

	/**
	 * 댓글 수 가중치
	 */
	private static final double COMMENT_WEIGHT = 0.10;

	/**
	 * 최대 랭킹
	 */
	private static final int MAX_RANKING = 50;

	@Override
	public List<PopularBookCandidate> calculate(List<PopularBookCandidate> candidates) {

		if (candidates == null || candidates.isEmpty()) {
			return List.of();
		}

		long maxReviewCount = candidates.stream()
			.mapToLong(PopularBookCandidate::getReviewCount)
			.max()
			.orElse(1L);

		long maxLikeCount = candidates.stream()
			.mapToLong(PopularBookCandidate::getLikeCount)
			.max()
			.orElse(1L);

		long maxCommentCount = candidates.stream()
			.mapToLong(PopularBookCandidate::getCommentCount)
			.max()
			.orElse(1L);

		for (PopularBookCandidate candidate : candidates) {

			double ratingScore =
				candidate.getAverageRating() / 5.0;

			double reviewScore =
				normalizeLog(candidate.getReviewCount(), maxReviewCount);

			double likeScore =
				normalizeLog(candidate.getLikeCount(), maxLikeCount);

			double commentScore =
				normalizeLog(candidate.getCommentCount(), maxCommentCount);

			double finalScore =
				(ratingScore * RATING_WEIGHT)
					+ (reviewScore * REVIEW_WEIGHT)
					+ (likeScore * LIKE_WEIGHT)
					+ (commentScore * COMMENT_WEIGHT);

			candidate.setScore(
				BigDecimal.valueOf(finalScore)
					.setScale(2, RoundingMode.HALF_UP)
			);
		}

		List<PopularBookCandidate> rankedCandidates = candidates.stream()
			.sorted(
				Comparator
					.comparing(PopularBookCandidate::getScore, Comparator.reverseOrder())
					.thenComparing(PopularBookCandidate::getReviewCount, Comparator.reverseOrder())
					.thenComparing(PopularBookCandidate::getLikeCount, Comparator.reverseOrder())
					.thenComparing(PopularBookCandidate::getCommentCount, Comparator.reverseOrder())
					.thenComparing(PopularBookCandidate::getAverageRating, Comparator.reverseOrder())
					.thenComparing(PopularBookCandidate::getBookId)
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