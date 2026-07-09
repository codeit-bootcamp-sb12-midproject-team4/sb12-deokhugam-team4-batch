package com.codeit.deokhugambatch.user.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity(name = "BatchOAuth")
@Table(name = "oauth_kakao")
@Getter
public class OauthKakao {

	@Id
	@Column(columnDefinition = "BINARY(16)")
	@JdbcTypeCode(SqlTypes.BINARY)
	private UUID id;

	private String email;
	private String nickname;

	@Column(name = "kakao_id")
	private String kakaoId;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	@Column(name = "created_at")
	private Instant createdAt;

	@Column(name = "updated_at")
	private Instant updatedAt;

}
