package com.codeit.deokhugambatch.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity(name = "BatchUser")
@Table(name = "users")
@Getter
public class User {

  @Id
  @Column(columnDefinition = "BINARY(16)")
  @JdbcTypeCode(SqlTypes.BINARY)
  private UUID id;

  private String email;
  private String nickname;
  private String password;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;
}
