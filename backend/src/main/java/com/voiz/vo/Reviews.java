package com.voiz.vo;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

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
@Table(name = "VOYZ_Reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reviews {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "review_seq_generator")
    @SequenceGenerator(name = "review_seq_generator", sequenceName = "VOYZ_REVIEWS_SEQ", allocationSize = 1)
    @Column(name = "REVIEW_IDX", nullable = false)
    private Long reviewId; // 리뷰 ID

    @Column(name = "ORDER_IDX", nullable = false)
    private String orderId; // 주문 ID

    @Column(name = "USER_ID", nullable = false)
    private String userId; // 사용자 ID

     @Column(name = "REVIEW_COMMENT", nullable = false)
    private String comment; // 리뷰 내용

    @Column(name = "RATING", nullable = false)
    private int rating; // 평점

    @Column(name = "NATIONALITY", nullable = false)
    private String nationality; // 국적

    @Column(name = "LANGUAGE")
    private String language; // 언어

    @Column(name = "MENU_IDX", nullable = false)
    private int  menuId; // 메뉴 ID 목록

   @CreationTimestamp
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

   


    
}
