package com.codeit.deokhugambatch.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class SoftDeletableEntity extends UpdatableEntity {

  // soft delete
  @Column(name = "deleted_at")
  private Instant deletedAt;

  public void markDeleted() {
    this.deletedAt = Instant.now();
  }

  public boolean isDeleted() {
    return this.deletedAt != null;
  }

}
