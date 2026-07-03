package com.codeit.deokhugambatch.user.repository;

import com.codeit.deokhugambatch.user.entity.OauthKakao;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OauthKakaoRepository extends JpaRepository<OauthKakao, UUID> {
  @Modifying
  @Query(value = "DELETE FROM oauth_kakao WHERE user_id IN (SELECT id FROM users WHERE deleted_at <= :oneDayAgo)", nativeQuery = true)
  void deleteExpiredOauthKakao(@Param("oneDayAgo") Instant oneDayAgo);
}