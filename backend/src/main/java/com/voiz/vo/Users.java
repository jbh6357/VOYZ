package com.voiz.vo;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "VOYZ_USERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users {
   
   @Id
   @Column(name = "USER_ID")
   private String userId;
   
   @Column(name = "USER_PW", nullable = false)
   private String userPw;
   
   @Column(name = "USER_NAME", nullable = false)
   private String userName;
   
   @Column(name = "USER_EMAIL", nullable = false)
   private String userEmail;
   
   @Column(name = "USER_PHONE", nullable = false)
   private String userPhone;
   
   @Column(name = "STORE_NAME", nullable = false)
   private String storeName;
   
   @Column(name = "STORE_CATEGORY", nullable = false)
   private String storeCategory;
   
   @Column(name = "STORE_ADDRESS", nullable = false)
   private String storeAddress;
   
   @Column(name = "ROLE", nullable = false)
   private String role;
   
   @Column(name = "CREATED_AT")
   private LocalDateTime createdAt;
   
   @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
   
}
