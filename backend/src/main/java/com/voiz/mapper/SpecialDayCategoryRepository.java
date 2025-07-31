package com.voiz.mapper;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.voiz.vo.SpecialDayCategory;

@Repository
public interface SpecialDayCategoryRepository extends JpaRepository<SpecialDayCategory, Integer> {
    
    /**
     * 특정 특일의 모든 카테고리 조회
     * @param sdIdx 특일 ID
     * @return 카테고리 목록
     */
    List<SpecialDayCategory> findBySdIdx(Long sdIdx);
    
    /**
     * 특정 카테고리에 속한 모든 특일 조회
     * @param category 카테고리명
     * @return 특일 카테고리 목록
     */
    List<SpecialDayCategory> findByCategory(String category);
    
    /**
     * 특일 ID와 카테고리로 중복 확인
     * @param sdIdx 특일 ID
     * @param category 카테고리명
     * @return 존재 여부
     */
    boolean existsBySdIdxAndCategory(Long sdIdx, String category);
    
    /**
     * 특정 특일의 모든 카테고리 삭제
     * @param sdIdx 특일 ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM SpecialDayCategory sdc WHERE sdc.sdIdx = :sdIdx")
    void deleteBySdIdx(@Param("sdIdx") Long sdIdx);
    
    /**
     * 특정 카테고리의 모든 데이터 삭제
     * @param category 카테고리명
     */
    @Modifying
    @Transactional
    void deleteByCategory(String category);
}