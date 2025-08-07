package com.voiz.dto;

import lombok.Data;

@Data
public class FcmTokenDto {
    private String userId;
    private String token;
    private String uuid;
}
