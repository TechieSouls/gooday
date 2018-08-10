package com.cg.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cg.bo.NotificationCountData;


@Repository
public interface NotificationCountDataRepository extends JpaRepository<NotificationCountData, Long>{
	
	NotificationCountData findByUserId(Long userId);
}
