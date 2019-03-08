package com.cg.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.cg.bo.Notification;
import com.cg.bo.Notification.NotificationReadStatus;
import com.cg.bo.Notification.NotificationTypeAction;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>{
	
	public List<Notification> findByRecepientIdOrderByCreatedAtDesc(Long userId); 
	public List<Notification> findByRecepientIdAndReadStatusOrderByCreatedAtDesc(Long userId,NotificationReadStatus readStatus); 
	public Notification findByNotificationTypeIdAndRecepientId(Long notificationTypeId,Long recepientId);
	public Notification findByNotificationTypeIdAndRecepientIdAndAction(Long notificationTypeId,Long recepientId, NotificationTypeAction action);

	@Query("select COUNT(*) from Notification notification where notification.recepientId = :recepientId AND notification.readStatus = :readStatus")
	public List<Object> findCountsByRecepientIdAndReadStatus(@Param("recepientId") Long recepientId,@Param("readStatus") NotificationReadStatus readStatus);
	
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("delete from Notification n where n.recepientId = :recepientId and n.notificationTypeId =:notificationTypeId")
	public void deleteByRecepientIdAndNotificationTypeId(@Param("recepientId") Long recepientId,@Param("notificationTypeId") Long notificationTypeId);

	@Modifying(clearAutomatically = true)
	@Transactional
	public void deleteByNotificationTypeId(Long notificationTypeId);
	
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("update Notification n set n.readStatus = :readStatus where n.recepientId = :recepientId")
	public void updateReadStatusByreceipientId(@Param("readStatus") Notification.NotificationReadStatus readStatus ,@Param("recepientId") Long recepientId);

	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("update Notification n set n.readStatus = :readStatus where n.recepientId = :recepientId and n.notificationTypeId = :notificationTypeId")
	public void updateReadStatusByreceipientIdAndNotificationTypeId(@Param("readStatus") Notification.NotificationReadStatus readStatus ,@Param("recepientId") Long recepientId, @Param("notificationTypeId") Long notificationTypeId);
}
