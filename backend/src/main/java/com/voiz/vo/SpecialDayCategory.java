package com.voiz.vo;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "VOYZ_SPECIAL_DAY_CATEGORY")
public class SpecialDayCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "special_day_category_seq")
    @SequenceGenerator(name = "special_day_category_seq", sequenceName = "SPECIAL_DAY_CATEGORY_SEQUENCE", allocationSize = 1)
    @Column(name = "SC_IDX")
    private int scIdx;
    
    @Column(name = "SD_IDX", nullable = false)
    private Long sdIdx;
    
    @Column(name = "CATEGORY", nullable = false, length = 20)
    private String category;
    
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;
    
    // 기본 생성자
    public SpecialDayCategory() {}
    
    // 생성자
    public SpecialDayCategory(Long sdIdx, String category) {
        this.sdIdx = sdIdx;
        this.category = category;
    }
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public int getScIdx() {
        return scIdx;
    }
    
    public void setScIdx(int scIdx) {
        this.scIdx = scIdx;
    }
    
    public Long getSdIdx() {
        return sdIdx;
    }
    
    public void setSdIdx(Long sdIdx) {
        this.sdIdx = sdIdx;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "SpecialDayCategory{" +
                "scIdx=" + scIdx +
                ", sdIdx=" + sdIdx +
                ", category='" + category + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}