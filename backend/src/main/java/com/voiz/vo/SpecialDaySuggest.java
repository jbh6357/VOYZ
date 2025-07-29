package com.voiz.vo;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "VOYZ_SPECIAL_DAY_SUG")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecialDaySuggest {
	
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "special_day_sug_seq")
    @SequenceGenerator(name = "special_day_sug_seq", sequenceName = "VOYZ_SPECIAL_DAY_SUG_SEQ", allocationSize = 1)
    @Column(name = "SSU_IDX")
    private int ssuIdx;

    @Column(name = "TITLE", nullable = false)
    private String title;

    @Column(name = "CONTENT", nullable = false)
    private String content;

    @Column(name = "START_DATE", nullable = false)
    private LocalDate startDate;

    @Column(name = "END_DATE", nullable = false)
    private LocalDate endDate;

    @Column(name = "SM_IDX", nullable = false)
    private int sm_idx;

    @Column(name = "CALENDAR_IDX", nullable = false)
    private int calendarIdx;	
}
