package com.codeit.deokhugambatch.dashboard.poweruser.model;

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
 * 파워유저 집계 파이프라인에서 사용하는 Candidate 모델.
 *
 * <p>
 * Reader에서 사용자 활동 데이터를 집계하여 생성하고,
 * Calculator에서 액션 포인트 기반 점수와 순위를 계산한 뒤,
 * Writer에서 power_user 테이블에 저장한다.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PowerUserCandidate {

	/**
	 * 사용자 ID (users.id)
	 */
	private UUID userId;

	/**
	 * 사용자 닉네임
	 */
	private String nickname;

	/**
	 * 작성한 리뷰 수
	 */
	private long reviewCount;

	/**
	 * 작성한 리뷰가 받은 좋아요 수
	 */
	private long receivedLikeCount;

	/**
	 * 작성한 리뷰가 받은 댓글 수
	 */
	private long receivedCommentCount;

	/**
	 * 작성한 댓글 수
	 */
	private long writtenCommentCount;

	/**
	 * 누른 좋아요 수
	 */
	private long givenLikeCount;

	/**
	 * 집계 기간
	 */
	private DashboardPeriod period;

	/**
	 * 배치 기준 날짜
	 */
	private LocalDate batchDate;

	/**
	 * Calculator에서 계산되는 활동 점수
	 */
	private BigDecimal score;

	/**
	 * Calculator에서 부여하는 순위
	 */
	private int ranking;
}