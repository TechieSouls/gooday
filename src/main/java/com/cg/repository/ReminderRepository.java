package com.cg.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.cg.reminders.bo.Reminder;

public interface ReminderRepository extends JpaRepository<Reminder, Long>{
	
	List<Reminder> findByCreatedByIdOrderByReminderTimeDesc(Long userId);
	
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("update Reminder r set r.status = 'Finish' where r.reminderId = :reminderId")
	public void updateReminderToFinish(@Param("reminderId") Long reminderId);
	
	@Query("select r from Reminder r JOIN r.reminderMembers rm where r.status = 'Start' and rm.memberId = :reminderId and rm.status = 'Accept' order by r.reminderTime asc")
	List<Reminder> findByAcceptedReminderMemberStatusAsc(@Param("reminderId") Long userId);

	@Query("select r from Reminder r JOIN r.reminderMembers rm where (DATE(r.reminderTime) <= :endDate or r.reminderTime is null) and r.status = 'Start' and rm.memberId = :reminderId and rm.status = 'Accept' order by r.reminderTime asc")
	List<Reminder> findAllRemindersByAcceptedReminderMemberStatusAsc(@Param("reminderId") Long userId,@Param("endDate") Date endDate);
	
	@Query("select r from Reminder r JOIN r.reminderMembers rm where rm.memberId = :userId and (rm.status = 'Accept' or rm.status is null) order by r.reminderTime asc")
	List<Reminder> findAllUserRemidners(@Param("userId") Long userId);
	
	@Query("select r from Reminder r where r.reminderTime is not null and r.reminderTime >= now() and TIMESTAMPDIFF(MINUTE,now(),r.reminderTime) = 1 order by r.reminderTime asc")
	List<Reminder> findAllRemindersWithTimeDifferenceEqualToOne();
	
	@Query("select r from Reminder r where r.reminderTime is null and DATE(r.reminderTime) <= DATE(now()) and r.status = 'Finish' order by r.reminderTime asc")
	List<Reminder> findCompletedReminders();
}
