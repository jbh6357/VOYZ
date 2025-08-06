package com.voiz.vo;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "VOYZ_SPECIAL_DAY_MATCH")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecialDayMatch {
	
	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "special_day_match_seq")
    @SequenceGenerator(name = "special_day_match_seq", sequenceName = "SPECIAL_DAY_MATCH_SEQUENCE", allocationSize = 1)
    @Column(name = "SM_IDX")
    private int sm_idx;
	
	@Column(name = "SD_IDX", nullable = false)
	private int sd_idx;
	
	@Column(name = "USER_ID", nullable = false)
	private String userId;
}
