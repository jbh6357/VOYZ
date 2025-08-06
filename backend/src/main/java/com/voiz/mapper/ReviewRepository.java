package com.voiz.mapper;

import com.voiz.vo.MenusReviews;
import com.voiz.vo.Reviews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Reviews, Long> {

    
    @Query("SELECT mr FROM MenusReviews mr JOIN FETCH mr.reviewId WHERE mr.menuId = :menuId")
    List<MenusReviews> findByMenuId(@Param("menuId") String menuId);
}