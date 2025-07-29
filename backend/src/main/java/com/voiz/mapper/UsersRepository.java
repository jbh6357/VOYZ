package com.voiz.mapper;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.voiz.vo.Users;

@Repository
public interface UsersRepository extends JpaRepository<Users, String>{

    Optional<Users> findByUserId(String userId);
    Optional<Users> findByUserName(String username);
    boolean existsByUserName(String userName);

}
