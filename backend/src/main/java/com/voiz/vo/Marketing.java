package com.voiz.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "VOYZ_MARKETING")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Marketing {
	
	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "marketing_seq")
    @SequenceGenerator(name = "marketing_seq", sequenceName = "MARKETING_SEQUENCE", allocationSize = 1)
    @Column(name = "MARKETING_IDX")
    private int marketingIdx;
	
	@Column(name = "TITLE", nullable = false)
    private String title;
	
	@Column(name = "TYPE")
    private String type;
	
	@Column(name = "CONTENT", nullable = false, length = 1000)
    private String content;
	
	@Column(name = "START_DATE", nullable = false)
	private LocalDate startDate;
	
	@Column(name = "END_DATE", nullable = false)
	private LocalDate endDate;
	
	@Column(name = "START_TIME", nullable = false)
	private LocalDateTime startTime; 
	
	@Column(name = "FIXED_AT")
    private LocalDateTime fixedAt;
	
	@Column(name = "STATUS")
    private String status;
	
	@Column(name = "REMINDER_IDX")
    private int reminder_idx;
	
	@Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "TARGET_CUSTOMER")
    private String targetCustomer;

    @Column(name = "SUGGESTED_ACTION")
    private String suggestedAction;

    @Column(name = "EXPECTED_EFFECT")
    private String expectedEffect;

    @Column(name = "CONFIDENCE")
    private Float confidence;

    @Column(name = "PRIORITY")
    private String priority;
    
	@PrePersist
    protected void onCreate() {
		fixedAt = LocalDateTime.now();
    }
}
