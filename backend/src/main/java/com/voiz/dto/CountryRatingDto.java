package com.voiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "국가별 평균 평점 및 작성자 수 DTO")
public class CountryRatingDto {
    
    @Schema(description = "국가명", example = "일본")
    private String nationality;
    
    @Schema(description = "해당 국가 리뷰 작성자 수", example = "25")
    private Long count;
    
    @Schema(description = "해당 국가 평균 평점", example = "4.2")
    private Double averageRating;
    
    public CountryRatingDto() {}
    
    public CountryRatingDto(String nationality, Long count, Double averageRating) {
        this.nationality = nationality;
        this.count = count;
        this.averageRating = averageRating;
    }
    
    public String getNationality() {
        return nationality;
    }
    
    public void setNationality(String nationality) {
        this.nationality = nationality;
    }
    
    public Long getCount() {
        return count;
    }
    
    public void setCount(Long count) {
        this.count = count;
    }
    
    public Double getAverageRating() {
        return averageRating;
    }
    
    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
}