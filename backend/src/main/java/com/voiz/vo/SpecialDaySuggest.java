package com.voiz.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "VOYZ_SPECIAL_DAY_SUG")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
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
    private int smIdx;

    @Column(name = "CALENDAR_IDX", nullable = false)
    private int calendarIdx;	
    
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

    @Column(name = "DATA_SOURCE", nullable = false)
    private String dataSource;
    
    @PrePersist
    protected void onCreate() {
    	dataSource = "한국천문연구원 특일 정보제공 서비스 API";
    }
    
}
