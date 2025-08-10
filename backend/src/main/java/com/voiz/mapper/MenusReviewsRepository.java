package com.voiz.mapper;

import com.voiz.vo.MenusReviews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenusReviewsRepository extends JpaRepository<MenusReviews, Long> {
}