package com.voiz.dto;

import java.util.List;

import lombok.Data;

@Data
public class OrderRequestDto{
    private String userId;           // 회원ID
    private int tableIdx;            // 테이블ID
    private String specialRequests;   // 특별 요청사항
    private String orderLanguage;    // 주문 언어
    private List<OrderDetailRequest> orderDetails;  // 주문 상세 리스트

    @Data
    public static class OrderDetailRequest {
        private int menuIdx;          // 메뉴ID
        private int quantity;         // 수량
        private String menuOption;    // 메뉴 옵션
        private String specialRequests; // 메뉴별 특별 요청
    }
}