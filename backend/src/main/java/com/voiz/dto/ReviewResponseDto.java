package com.voiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponseDto {

    private int reviewIdx;
    private int menuIdx; 
    private int orderIdx;
    private String userId;
    private String guestName;
    private String comment;
    private int rating;
    private String nationality;
    private String language;
    private LocalDateTime createdAt;
    private String menuName; // optional: joined from VOYZ_MENUS
    
}
