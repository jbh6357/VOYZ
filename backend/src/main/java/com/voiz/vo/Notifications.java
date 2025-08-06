package com.voiz.vo;

import java.time.LocalDateTime;

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
@Table(name = "VOYZ_NOTIFICATIONS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notifications {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_seq")
    @SequenceGenerator(name = "notification_seq", sequenceName = "NOTIFICATION_SEQUENCE", allocationSize = 1)
	@Column(name = "NOTIFICATIONS_IDX")
	private int notificationIdx;
	
	@Column(name = "USER_ID")
	private String userId;
	
	@Column(name = "TITLE")
	private String title;
	
	@Column(name = "MESSAGE")
	private String message;

	@Column(name = "SCHEDULED_AT")
	private LocalDateTime scheduledAt;
	
	@Column(name = "SENT")
	private int sent;
	
}
