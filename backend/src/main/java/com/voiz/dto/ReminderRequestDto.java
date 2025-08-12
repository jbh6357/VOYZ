package com.voiz.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReminderRequestDto {
	private String title;
	private String content;
	private LocalDate startDate;
	private LocalDate endDate;
	private LocalDateTime startTime;
}
