package com.voiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메뉴별 감성 분석 결과 DTO")
public class MenuSentimentDto {
    
    @Schema(description = "메뉴 ID", example = "123")
    private Integer menuId;
    
    @Schema(description = "메뉴명", example = "불고기 정식")
    private String menuName;
    
    @Schema(description = "긍정 리뷰 수", example = "15")
    private Long positiveCount;
    
    @Schema(description = "부정 리뷰 수", example = "3")
    private Long negativeCount;
    
    @Schema(description = "중립 리뷰 수", example = "7")
    private Long neutralCount;
    
    @Schema(description = "평균 평점", example = "4.1")
    private Double averageRating;
    
    public MenuSentimentDto() {}
    
    public MenuSentimentDto(Integer menuId, String menuName, Long positiveCount, Long negativeCount, Long neutralCount, Double averageRating) {
        this.menuId = menuId;
        this.menuName = menuName;
        this.positiveCount = positiveCount;
        this.negativeCount = negativeCount;
        this.neutralCount = neutralCount;
        this.averageRating = averageRating;
    }
    
    public Integer getMenuId() {
        return menuId;
    }
    
    public void setMenuId(Integer menuId) {
        this.menuId = menuId;
    }
    
    public String getMenuName() {
        return menuName;
    }
    
    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }
    
    public Long getPositiveCount() {
        return positiveCount;
    }
    
    public void setPositiveCount(Long positiveCount) {
        this.positiveCount = positiveCount;
    }
    
    public Long getNegativeCount() {
        return negativeCount;
    }
    
    public void setNegativeCount(Long negativeCount) {
        this.negativeCount = negativeCount;
    }
    
    public Long getNeutralCount() {
        return neutralCount;
    }
    
    public void setNeutralCount(Long neutralCount) {
        this.neutralCount = neutralCount;
    }
    
    public Double getAverageRating() {
        return averageRating;
    }
    
    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
}