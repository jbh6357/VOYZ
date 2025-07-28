package com.voiz.mapper;

import com.voiz.vo.TokenEntity;
import com.voiz.vo.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<TokenEntity, Long> {

}
