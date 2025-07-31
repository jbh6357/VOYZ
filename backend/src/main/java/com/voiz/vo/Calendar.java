package com.voiz.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "VOYZ_CALENDAR")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Calendar {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calendar_seq")
    @SequenceGenerator(name = "calendar_seq", sequenceName = "CALENDAR_SEQUENCE", allocationSize = 1)
	@Column(name = "CALENDAR_IDX")
	private int calendarIdx;
	
	@Column(name = "USER_ID")
	private String userId;
}
