package com.codeit.deokhugambatch.batch.notification;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import com.codeit.deokhugambatch.batch.notification.repository.NotificationRepository;
import com.codeit.deokhugamcommon.domain.book.Book;
import com.codeit.deokhugamcommon.domain.notification.entity.Notification;
import com.codeit.deokhugamcommon.domain.review.entity.Review;
import com.codeit.deokhugamcommon.domain.user.User;

import jakarta.persistence.EntityManager;

@EnableJpaAuditing
@SpringBootTest(properties = {
	"spring.profiles.active=",
	"spring.datasource.url=jdbc:h2:mem:notification-batch-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
	"spring.datasource.username=sa",
	"spring.datasource.password=",
	"spring.datasource.driver-class-name=org.h2.Driver",
	"spring.sql.init.mode=never",
	"spring.jpa.hibernate.ddl-auto=create-drop",
	"spring.batch.jdbc.initialize-schema=always",
	"spring.batch.job.enabled=false"
})
class NotificationBatchTest {

	private static final String PREFIX = "batch-test-notification-";

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	@Qualifier("notificationDeleteJob")
	private Job notificationDeleteJob;

	private User user;
	private Review review;

	@BeforeEach
	void setUp() {
		transactionTemplate.executeWithoutResult(status -> {
			user = new User(
				"receiver@test.com",
				"receiver",
				"password"
			);
			entityManager.persist(user);

			Book book = Book.builder()
				.title("title")
				.author("author")
				.description("description")
				.publisher("publisher")
				.publishedDate(LocalDate.of(2024, 1, 1))
				.isbn("9781234567890")
				.thumbnailKey("thumbnail_key")
				.rating(0.0)
				.reviewCount(0L)
				.build();

			entityManager.persist(book);

			review = new Review(
				"review content",
				null,
				5,
				0L,
				0L,
				book,
				user
			);
			entityManager.persist(review);

			entityManager.flush();
			entityManager.clear();
		});
	}

	@Test
	void deleteConfirmedOldNotifications() throws Exception {
		insertNotifications(30);
		assertThat(countNotifications()).isEqualTo(30);

		confirmOldNotifications(10);
		assertThat(countConfirmedNotifications()).isEqualTo(10);

		JobExecution jobExecution = jobLauncher.run(notificationDeleteJob, jobParameters());

		assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		assertThat(countNotifications()).isEqualTo(20);
		assertThat(countConfirmedNotifications()).isZero();
	}

	//  알림 저장
	private void insertNotifications(int count) {
		List<Notification> notifications = IntStream.rangeClosed(1, count)
			.mapToObj(i -> new Notification(
				user,
				review,
				PREFIX + i,
				false
			))
			.toList();

		notificationRepository.saveAllAndFlush(notifications);
		entityManager.clear();
	}

	//  알림 확인
	private void confirmOldNotifications(int count) {
		Instant oldUpdatedAt = Instant.now().minusSeconds(8 * 24 * 60 * 60);

		IntStream.rangeClosed(1, count)
			.forEach(i -> jdbcTemplate.update(
				"""
					UPDATE notification
					SET confirmed = true,
						updated_at = ?
					WHERE message = ?
					""",
				oldUpdatedAt,
				PREFIX + i
			));

		entityManager.clear();
	}

	private long countNotifications() {
		return notificationRepository.count();
	}

	private long countConfirmedNotifications() {
		return notificationRepository.findAll().stream()
			.filter(Notification::isConfirmed)
			.count();
	}

	private JobParameters jobParameters() {
		return new JobParametersBuilder()
			.addLong("time", System.currentTimeMillis())
			.toJobParameters();
	}

}