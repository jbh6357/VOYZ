package com.voiz.mapper;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.voiz.vo.Notifications;

@Repository
public interface NotificationsRepository extends JpaRepository<Notifications, Integer>{
	@Query("SELECT a FROM Notifications a WHERE a.sent = 0 AND a.scheduledAt <= CURRENT_TIMESTAMP")
    List<Notifications> findDueAlerts();
}
