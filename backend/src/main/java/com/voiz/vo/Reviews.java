package com.voiz.vo;

import java.time.LocalDateTime;

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
    private Long reviewIdx;

    @Column(name = "ORDER_IDX", nullable = false)
    private String orderIdx;

    @Column(name = "USER_ID", nullable = false)
    private String userId;

    @Column(name = "RATING", nullable = false)
    private int rating;

    @Column(name = "REVIEW_COMMENT", nullable = false)
    private String reviewComment;

    @Column(name = "NATIONALITY", nullable = false)
    private String nationality;

    @Column(name = "LANGUAGE")
    private String language;

    @Column(name = "STATUS")
    private String status;

    @CreationTimestamp
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "MENU_IDX", nullable = false)
    private String menuIdx;

    
}
