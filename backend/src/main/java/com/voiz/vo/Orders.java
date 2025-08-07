package com.voiz.vo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "VOYZ_ORDERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_seq")
    @SequenceGenerator(name = "order_seq", sequenceName = "ORDER_SEQUENCE", allocationSize = 1)
    @Column(name = "ORDER_IDX")
    private int orderIdx;

    @Column(name = "USER_ID", nullable = false)
    private String userId;

    @Column(name = "TABLE_IDX", nullable = false)
    private int tableIdx;

    @Column(name = "ORDER_NUMBER")
    private String orderNumber;

    @Column(name = "TOTAL_AMOUNT")
    private int totalAmount;

    @Column(name = "SPECIAL_REQUESTS")
    private String specialRequests;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "ORDER_LANGUAGE")
    private String orderLanguage;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}