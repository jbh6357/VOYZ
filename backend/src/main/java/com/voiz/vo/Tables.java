package com.voiz.vo;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "VOYZ_TABLES")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tables {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "table_seq")
    @SequenceGenerator(name = "table_seq", sequenceName = "TABLE_SEQUENCE", allocationSize = 1)
    @Column(name = "TABLE_IDX")
    private int tableIdx;

    @Column(name = "USER_ID", nullable = false)
    private String userId;

    @Column(name = "TABLE_NUMBER", nullable = false)
    private int tableNumber;

    @Column(name = "TABLE_STATUS")
    private String tableStatus;

    @Column(name = "QR_CODE")
    private String qrCode;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
