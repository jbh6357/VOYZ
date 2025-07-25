package com.voiz.vo;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "VOYZ_SPECIAL_DAY")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecialDay {

	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "special_day_seq")
    @SequenceGenerator(name = "special_day_seq", sequenceName = "SPECIAL_DAY_SEQUENCE", allocationSize = 1)
    @Column(name = "SD_IDX")
    private int sd_idx;
	
	@Column(name = "NAME", nullable = false)
    private String name;
	
	@Column(name = "TYPE", nullable = false)
	private String type;
	
	@Column(name = "CATEGORY")
	private String category;
	
	@Column(name = "START_DATE", nullable = false)
	private LocalDate startDate;

	@Column(name = "END_DATE", nullable = false)
	private LocalDate endDate;

	@Column(name = "IS_HOLIDAY", nullable = false)
	private int isHoliday;
	
	
}
