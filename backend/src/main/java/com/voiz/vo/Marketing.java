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
	
	@Column(name = "CONTENT", nullable = false)
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
	
	@PrePersist
    protected void onCreate() {
		fixedAt = LocalDateTime.now();
    }
}
