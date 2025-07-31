package com.voiz.mapper;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.voiz.vo.SpecialDayMatch;

@Repository
public interface SpecialDayMatchRepository extends JpaRepository<SpecialDayMatch, Integer> {
    
    /**
     * 특정 유저의 모든 매칭 데이터 조회
     * @param userId 유저 ID
     * @return 매칭 데이터 목록
     */
    List<SpecialDayMatch> findByUserId(String userId);
    
    /**
     * 특정 특일의 모든 매칭 데이터 조회
     * @param sd_idx 특일 ID
     * @return 매칭 데이터 목록
     */
    @Query("SELECT sdm FROM SpecialDayMatch sdm WHERE sdm.sd_idx = :sd_idx")
    List<SpecialDayMatch> findBySd_idx(@Param("sd_idx") int sd_idx);
    
    /**
     * 유저와 특일의 매칭 데이터 존재 여부 확인
     * @param userId 유저 ID
     * @param sd_idx 특일 ID
     * @return 존재 여부
     */
    @Query("SELECT CASE WHEN COUNT(sdm) > 0 THEN true ELSE false END FROM SpecialDayMatch sdm WHERE sdm.userId = :userId AND sdm.sd_idx = :sd_idx")
    boolean existsByUserIdAndSd_idx(@Param("userId") String userId, @Param("sd_idx") int sd_idx);
    
    /**
     * 특정 유저의 모든 매칭 데이터 삭제
     * @param userId 유저 ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM SpecialDayMatch sdm WHERE sdm.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);
    
    /**
     * 특정 특일의 모든 매칭 데이터 삭제
     * @param sd_idx 특일 ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM SpecialDayMatch sdm WHERE sdm.sd_idx = :sd_idx")
    void deleteBySd_idx(@Param("sd_idx") int sd_idx);
}
