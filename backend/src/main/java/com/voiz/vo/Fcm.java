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
@Table(name = "VOYZ_FCM")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fcm {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fcm_seq")
    @SequenceGenerator(name = "fcm_seq", sequenceName = "FCM_SEQUENCE", allocationSize = 1)
	@Column(name = "FCM_IDX")
	private int fcmIdx;
	
	@Column(name = "USER_ID")
	private String userId;
	
	@Column(name = "UUID")
	private String uuid;
	
	@Column(name = "TOKEN")
	private String token;
}
