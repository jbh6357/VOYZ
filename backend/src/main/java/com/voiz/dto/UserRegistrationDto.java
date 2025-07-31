package com.voiz.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserRegistrationDto {
    private String userId;
    private String userPw;
    private String userName;
    private String userPhone;
    private String storeName;
    private String storeCategory;
    private String storeAddress;
} 