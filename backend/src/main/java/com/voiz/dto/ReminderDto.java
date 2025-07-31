package com.voiz.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReminderDto {
	private String title;
	private String content;
	private LocalDate startDate;
	private LocalDate endDate;
}
