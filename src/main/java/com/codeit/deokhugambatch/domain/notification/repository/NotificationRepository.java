package com.codeit.deokhugambatch.domain.notification.repository;

import com.codeit.deokhugambatch.domain.notification.entity.Notification;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  @Modifying
  @Query(
      """
            DELETE FROM Notification n
            WHERE n.confirmed = true
              and n.updatedAt < :threshold
          """)
  int deleteConfirmedNotifications(@Param("threshold") Instant threshold);
}
