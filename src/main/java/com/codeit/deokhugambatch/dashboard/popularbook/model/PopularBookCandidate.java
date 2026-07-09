package com.codeit.deokhugambatch.dashboard.popularbook.model;

import com.codeit.deokhugambatch.dashboard.common.model.DashboardPeriod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 인기도서 집계 파이프라인에서 사용하는 Candidate 모델.
 *
 * <p>
 * Reader에서 원천 데이터를 조회하여 생성하고,
 * Calculator에서 score와 ranking을 계산한 뒤,
 * Writer에서 popular_book 테이블에 저장한다.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularBookCandidate {

	/**
	 * 도서 ID (book.id)
	 */
	private UUID bookId;

	/**
	 * 도서 제목
	 */
	private String bookTitle;

	/**
	 * 저자
	 */
	private String author;

	/**
	 * 썸네일 URL(또는 Key)
	 */
	private String thumbnailUrl;

	/**
	 * 리뷰 수
	 */
	private long reviewCount;

	/**
	 * 평균 평점
	 */
	private double averageRating;

	/**
	 * 좋아요 수
	 */
	private long likeCount;

	/**
	 * 댓글 수
	 */
	private long commentCount;

	/**
	 * 집계 기간
	 */
	private DashboardPeriod period;

	/**
	 * 배치 기준 날짜
	 */
	private LocalDate batchDate;

	/**
	 * Calculator에서 계산되는 베이지안 점수
	 */
	private BigDecimal score;

	/**
	 * Calculator에서 부여하는 순위
	 */
	private int ranking;
}