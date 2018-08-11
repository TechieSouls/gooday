package com.cg.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cg.bo.NotificationCountData;


@Repository
public interface NotificationCountDataRepository extends JpaRepository<NotificationCountData, Long>{
	
	NotificationCountData findByUserId(Long userId);
	
	@Modifying
    @Query("UPDATE NotificationCountData ncd SET ncd.badgeCount = 0 WHERE ncd.userId = :userId")
    int setBadgeCountsToZero(@Param("userId") Long userId);
}
