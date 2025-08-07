package com.voiz.vo;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "VOYZ_MENU_REVIEWS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenusReviews {

    @Id
    @Column(name = "MENU_REVIEW_IDX", nullable = false)
    private Long menuReviewId; 

    @Column(name = "MENU_IDX", nullable = false)
    private String menuId; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REVIEW_IDX", referencedColumnName = "REVIEW_IDX")
    private Reviews reviewId; 

   
}
