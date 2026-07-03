package com.codeit.deokhugambatch.batch.notification.repository;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codeit.deokhugamcommon.domain.notification.entity.Notification;

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