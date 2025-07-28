package com.voiz.mapper;
import com.voiz.vo.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, String> {
    Optional<Users> findByUserName(String userName);
    Optional<Users> findByUserEmail(String userEmail);
}