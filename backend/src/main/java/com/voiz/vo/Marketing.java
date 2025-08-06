package com.voiz.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
	
	@Column(name = "TYPE", nullable = false)
    private String type;
	
	@Column(name = "CONTENT", nullable = false, length = 1000)
    private String content;
	
	@Column(name = "START_DATE", nullable = false)
	private LocalDate startDate;
	
	@Column(name = "END_DATE", nullable = false)
	private LocalDate endDate;
	
	@Column(name = "FIXED_AT", nullable = false)
    private LocalDateTime fixedAt;
	
	@Column(name = "STATUS", nullable = false)
    private String status;
	
	@Column(name = "REMINDER_IDX", nullable = false)
    private int reminder_idx;
	
	@Column(name = "DESCRIPTION", nullable = false)
    private String description;

    @Column(name = "TARGET_CUSTOMER", nullable = false)
    private String targetCustomer;

    @Column(name = "SUGGESTED_ACTION", nullable = false)
    private String suggestedAction;

    @Column(name = "EXPECTED_EFFECT", nullable = false)
    private String expectedEffect;

    @Column(name = "CONFIDENCE", nullable = false)
    private Float confidence;

    @Column(name = "PRIORITY", nullable = false)
    private String priority;
    
	@PrePersist
    protected void onCreate() {
		fixedAt = LocalDateTime.now();
    }
}
