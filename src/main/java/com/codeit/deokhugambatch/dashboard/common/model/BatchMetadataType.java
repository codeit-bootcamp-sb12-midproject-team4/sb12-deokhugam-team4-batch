package com.codeit.deokhugambatch.dashboard.common.model;

/**
 * batch_metadata 테이블의 metadata_type 컬럼과 매핑되는 Enum입니다.
 *
 * <p>각 Dashboard 통계 데이터셋의 최신 Dataset ID를 관리하기 위한
 * 메타데이터 타입을 정의합니다.
 *
 * <ul>
 *     <li>POPULAR_BOOK   : 인기도서</li>
 *     <li>POPULAR_REVIEW : 인기리뷰</li>
 *     <li>POWER_USER     : 파워유저</li>
 * </ul>
 *
 * <p>batch_metadata의 (metadata_type, period) 조합은 Unique Key를 가지며,
 * 배치 완료 시 MetadataUpdateTasklet이 최신 dataset_id를 갱신합니다.
 */
public enum BatchMetadataType {
	POPULAR_BOOK,
	POPULAR_REVIEW,
	POWER_USER
}