package com.codeit.deokhugambatch.book.config;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.codeit.deokhugambatch.book.writer.BookEsUpdateWriter;
import com.codeit.deokhugamcommon.domain.book.Book;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class BookSyncJobConfig {
	private final EntityManagerFactory entityManagerFactory;
	private final BookEsUpdateWriter bookEsUpdateWriter;

	private static final int CHUNK_SIZE = 100;

	@Bean
	public Job bookSyncJob(JobRepository jobRepository, Step bookSyncStep) {
		return new JobBuilder("bookSyncJob", jobRepository)
			.start(bookSyncStep)
			.build();
	}

	@Bean
	public Step bookSyncStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("bookSyncStep", jobRepository)
			.<Book, Book>chunk(CHUNK_SIZE, transactionManager)
			.reader(bookReader(null)) // StepScope 효과를 위해 null 대입 (런타임에 바인딩됨)
			.writer(bookEsUpdateWriter)
			.build();
	}

	@Bean
	@StepScope
	public JpaPagingItemReader<Book> bookReader(
		@Value("#{jobParameters['fromDate']}") String fromDateStr) {

		Instant fromDate = (fromDateStr != null)
			? Instant.parse(fromDateStr)
			: Instant.now().minus(6, ChronoUnit.MINUTES);

		Map<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("fromDate", fromDate);

		return new JpaPagingItemReaderBuilder<Book>()
			.name("bookReader")
			.entityManagerFactory(entityManagerFactory)
			// 최근 5~6분 사이에 수정된 도서만 타겟팅 (Soft Delete 상태도 필요시 구문 추가 가능)
			.queryString("SELECT b FROM Book b WHERE b.updatedAt >= :fromDate AND b.deletedAt IS NULL")
			.parameterValues(parameterValues)
			.pageSize(CHUNK_SIZE)
			.build();
	}
}
