package com.voiz.dto;

import lombok.Data;

@Data
public class UserRegistrationDto {
    private String username;
    private String password;
    private String email;
    private String name;
    private String role;
    private String storeName;
    private String phone;
    private String storeCategory;
    private String storeAddress;
} 