package com.voiz.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "VOYZ_ORDERS") // 실제 주문 테이블명으로 설정해주세요.
@Data
public class SalesOrder {

    @Id
    @Column(name = "ORDER_IDX")
    private Long orderIdx;

    @Column(name = "USER_ID")
    private String userId; // restaurantId로 사용됩니다.

    @Column(name = "TOTAL_AMOUNT")
    private BigDecimal totalAmount;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
}