package com.voiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NationalityAnalyticsDto {
    private String nationality;
    private long count;
}