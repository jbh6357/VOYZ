package com.voiz.mapper;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.voiz.vo.Reminder;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Integer> {
	
	@Query("SELECT r.reminderIdx FROM Reminder r WHERE r.userId = :userId")
	int findReminderIdxByUserId(@Param("userId") String userId);
	
}
