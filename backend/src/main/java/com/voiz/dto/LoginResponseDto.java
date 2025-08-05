package com.voiz.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {
    private String userId;
    private String userName;
    private String storeName;
    private String storeCategory;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
} 