package com.codeit.deokhugambatch.dashboard.popularbook.model;

import com.codeit.deokhugambatch.dashboard.common.model.DashboardPeriod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 인기도서(Popular Book) 집계 파이프라인 전반에서 사용되는 핵심 Candidate 도메인 모델입니다.
 * <p>
 * Reader 단계에서 원천 스키마(book, review, comment, review_like)로부터 집계된 임시 데이터를 저장하며,
 * Calculator 단계에서 베이지안 평균 알고리즘에 의해 계산된 score 및 ranking이 바인딩된 후,
 * Writer 단계에서 popular_book 테이블(BINARY(16), DECIMAL(10,2) 등)로 최종 적재됩니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularBookCandidate {

	/**
	 * 원천 도서 고유 식별자 (book.id 와 매핑)
	 * DB 스키마의 BINARY(16) 규격에 대응하기 위해 UUID 타입을 사용합니다.
	 */
	private UUID bookId;

	/**
	 * 도서 제목 (book.title)
	 */
	private String bookTitle;

	/**
	 * 도서 저자 (book.author)
	 */
	private String author;

	/**
	 * 도서 표지 이미지 경로 (book.thumbnail_key)
	 * 최종 통계 테이블(popular_book.thumbnail_url)로 매핑되어 저장됩니다.
	 */
	private String thumbnailUrl;

	/**
	 * 도서에 등록된 누적/기간 내 총 리뷰 수 (v)
	 * 베이지안 평균 공식의 가중치 변수 $v$로 활용됩니다.
	 */
	private long reviewCount;

	/**
	 * 도서의 평균 평점 (R)
	 * 스키마의 DOUBLE 기반 계산 결과이며, 베이지안 평균 공식의 변수 $R$로 활용됩니다.
	 */
	private double averageRating;

	/**
	 * 해당 기간 내에 획득한 총 좋아요(Like) 수
	 */
	private long likeCount;

	/**
	 * 해당 기간 내에 생성된 총 댓글(Comment) 수
	 */
	private long commentCount;

	/**
	 * 집계 주기 포맷 (DAILY, WEEKLY, MONTHLY, ALL_TIME)
	 * batch_metadata 및 popular_book 테이블의 ENUM 타입과 동기화됩니다.
	 */
	private DashboardPeriod period;

	/**
	 * 배치가 실행된 기준 일자
	 */
	private LocalDate batchDate;

	// =========================================================================
	// Calculator 단계에서 연산되어 동적으로 채워지는 비즈니스 필드
	// =========================================================================

	/**
	 * 베이지안 평균 공식에 의해 최종 산출된 인기도 점수 (WR)
	 * popular_book 스키마의 DECIMAL(10, 2) 규격에 맞춰 반올림 및 스케일링을 보장합니다.
	 */
	private BigDecimal score;

	/**
	 * 점수 정렬 후 최종 부여된 순위 (1위 ~ 50위)
	 * popular_book 테이블의 chk_popular_book_ranking (1 AND 50) 제약 조건을 충족합니다.
	 */
	private int ranking;
}