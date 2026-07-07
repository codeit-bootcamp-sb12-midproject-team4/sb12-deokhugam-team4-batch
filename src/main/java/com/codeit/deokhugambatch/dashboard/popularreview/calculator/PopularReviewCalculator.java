package com.codeit.deokhugambatch.dashboard.popularreview.calculator;

import com.codeit.deokhugambatch.dashboard.common.calculator.DashboardCalculator;
import com.codeit.deokhugambatch.dashboard.popularreview.model.PopularReviewCandidate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Component
public class PopularReviewCalculator implements DashboardCalculator<PopularReviewCandidate, PopularReviewCandidate> {

	private static final int MAX_RANKING_LIMIT = 50;

	@Override
	public List<PopularReviewCandidate> calculate(List<PopularReviewCandidate> input) {
		if (input == null || input.isEmpty()) {
			return List.of();
		}

		// 1. 제약조건 필터링 (좋아요 1개 이상만 Pool 유지) 및 선형 결합 스코어링
		List<PopularReviewCandidate> scoredReviews = input.stream()
			.filter(review -> review.getLikeCount() >= 1) // [핵심 제약조건] 좋아요 0개 원천 배제
			.map(review -> {
				long likeCount = review.getLikeCount();
				long commentCount = review.getCommentCount();

				// Score = (L * 2.0) + (C * 1.0)
				double score = (likeCount * 2.0) + commentCount;

				review.setScore(
					BigDecimal.valueOf(score)
						.setScale(2, RoundingMode.HALF_UP)
				);

				return review;
			})
			.sorted(Comparator.comparing(PopularReviewCandidate::getScore).reversed())
			.toList();

		// 2. 순위 부여 및 Top 50 제한
		return IntStream.range(0, Math.min(scoredReviews.size(), MAX_RANKING_LIMIT))
			.mapToObj(i -> {
				PopularReviewCandidate review = scoredReviews.get(i);
				review.setRanking(i + 1);
				return review;
			})
			.toList();
	}
}