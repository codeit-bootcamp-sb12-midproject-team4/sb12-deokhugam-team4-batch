package com.codeit.deokhugambatch.dashboard.popularbook.calculator;

import com.codeit.deokhugambatch.dashboard.common.calculator.DashboardCalculator;
import com.codeit.deokhugambatch.dashboard.popularbook.model.PopularBookCandidate; // 1차 집계 DTO 가정
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Component
public class PopularBookCalculator implements DashboardCalculator<PopularBookCandidate, PopularBookCandidate> {

	private static final double MIN_THRESHOLD_M = 10.0; // 최소 제한선 (m)
	private static final int MAX_RANKING_LIMIT = 50;   // 인기도서 상한선

	@Override
	public List<PopularBookCandidate> calculate(List<PopularBookCandidate> input) {
		if (input == null || input.isEmpty()) {
			return List.of();
		}

		// 1. 전체 도서의 평균 평점(C) 산출
		double globalAverageRatingC = input.stream()
			.mapToDouble(PopularBookCandidate::getAverageRating)
			.average()
			.orElse(0.0);

		// 2. 베이지안 스코어 계산 및 정렬
		List<PopularBookCandidate> scoredBooks = input.stream()
			.map(book -> {
				double v = book.getReviewCount();
				double r = book.getAverageRating();

				double wr = ((v / (v + MIN_THRESHOLD_M)) * r)
					+ ((MIN_THRESHOLD_M / (v + MIN_THRESHOLD_M)) * globalAverageRatingC);

				book.setScore(
					BigDecimal.valueOf(wr)
						.setScale(2, RoundingMode.HALF_UP)
				);

				return book;
			})
			.sorted(Comparator.comparing(PopularBookCandidate::getScore).reversed())
			.toList();

		// 3. 순위(Ranking) 부여 및 Top 50 제한
		return IntStream.range(0, Math.min(scoredBooks.size(), MAX_RANKING_LIMIT))
			.mapToObj(i -> {
				PopularBookCandidate book = scoredBooks.get(i);
				book.setRanking(i + 1);
				return book;
			})
			.toList();
	}
}