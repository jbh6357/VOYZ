package com.voiz.vo;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "VOYZ_CALENDAR")
@Data
public class Calendars {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calendar_seq_generator")
    @SequenceGenerator(name = "calendar_seq_generator", sequenceName = "VOYZ_CALENDAR_SEQ", allocationSize = 1)
    @Column(name = "CALENDAR_IDX")
    private Long calendarIdx;

    // 기존 Users와의 관계 (어떤 유저의 캘린더 항목인지)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID")
    private Users user;


    // 이 캘린더 항목이 어떤 마케팅 제안과 연결되는지를 명시
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MARKETING_IDX") 
    private Marketing marketing;

}