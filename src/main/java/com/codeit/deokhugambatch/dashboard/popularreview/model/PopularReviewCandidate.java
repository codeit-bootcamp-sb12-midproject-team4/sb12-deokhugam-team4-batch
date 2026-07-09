package com.codeit.deokhugambatch.dashboard.popularreview.model;

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
 * 인기 리뷰 집계 파이프라인에서 사용하는 Candidate 모델.
 *
 * <p>
 * Reader에서 원천 데이터를 조회하여 생성하고,
 * Calculator에서 score와 ranking을 계산한 뒤,
 * Writer에서 popular_review 테이블에 저장한다.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularReviewCandidate {

	/**
	 * 리뷰 ID (review.id)
	 */
	private UUID reviewId;

	/**
	 * 도서 제목
	 */
	private String bookTitle;

	/**
	 * 도서 저자
	 */
	private String bookAuthor;

	/**
	 * 썸네일 URL(또는 Key)
	 */
	private String thumbnailUrl;

	/**
	 * 작성자 닉네임
	 */
	private String userNickname;

	/**
	 * 리뷰 요약 (최대 300자)
	 */
	private String reviewSummary;

	/**
	 * 리뷰 평점
	 */
	private int reviewRating;

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
	 * Calculator에서 계산되는 인기 점수
	 */
	private BigDecimal score;

	/**
	 * Calculator에서 부여하는 순위
	 */
	private int ranking;
}