package com.codeit.deokhugambatch.dashboard.common.support;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class DatasetIdGenerator {

	private static final DateTimeFormatter ID_FORMATTER =
		DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	private static final int MAX_SEQUENCE = 999;

	private int sequence = 0;

	private String lastTimestamp = "";

	public synchronized Long generate() {
		String timestamp = LocalDateTime.now().format(ID_FORMATTER);

		if (!timestamp.equals(lastTimestamp)) {
			lastTimestamp = timestamp;
			sequence = 0; // sequence 초기화 단순화
		}

		// prefix 증가 연산 사용
		int currentSequence = ++sequence;

		if (currentSequence > MAX_SEQUENCE) {
			throw new IllegalStateException("Dataset ID sequence overflow.");
		}

		return Long.parseLong(timestamp + String.format("%03d", currentSequence));
	}
}