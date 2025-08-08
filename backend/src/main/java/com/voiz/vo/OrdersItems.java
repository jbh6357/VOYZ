package com.voiz.vo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "VOYZ_ORDERS_ITEMS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdersItems {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_item_seq")
    @SequenceGenerator(name = "order_item_seq", sequenceName = "ORDER_ITEM_SEQUENCE", allocationSize = 1)
    @Column(name = "ORDER_ITEM_IDX")
    private int orderItemIdx;

    @Column(name = "ORDER_IDX", nullable = false)
    private int orderIdx;

    @Column(name = "MENU_IDX", nullable = false)
    private int menuIdx;

    @Column(name = "QUANTITY")
    private int quantity;

    @Column(name = "UNIT_PRICE")
    private int unitPrice;

    @Column(name = "TOTAL_PRICE")
    private int totalPrice;

    @Column(name = "ITEM_OPTIONS")
    private String itemOptions;

    @Column(name = "SPECIAL_REQUESTS")
    private String specialRequests;
}
