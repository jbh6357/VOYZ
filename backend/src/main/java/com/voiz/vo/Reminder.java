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
@Table(name = "VOYZ_Reminder")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reminder {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reminder_seq")
    @SequenceGenerator(name = "reminder_seq", sequenceName = "REMINDER_SEQUENCE", allocationSize = 1)
	@Column(name = "REMINDER_IDX")
	private int reminderIdx;
	
	@Column(name = "USER_ID")
	private String userId;
}
