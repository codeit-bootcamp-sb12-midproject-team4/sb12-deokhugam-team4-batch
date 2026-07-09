package com.codeit.deokhugambatch.user.repository;

import com.codeit.deokhugambatch.user.entity.User;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {
  @Modifying
  @Query(value = "DELETE FROM users WHERE deleted_at <= :oneDayAgo", nativeQuery = true)
  void deleteExpiredUsers(@Param("oneDayAgo") Instant oneDayAgo);

}
