package com.codeit.deokhugambatch.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class UpdatableEntity extends BaseEntity {

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

}
