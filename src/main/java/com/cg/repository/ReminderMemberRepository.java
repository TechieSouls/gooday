package com.cg.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.cg.reminders.bo.ReminderMember;

@Repository
public interface ReminderMemberRepository extends JpaRepository<ReminderMember, Long> {
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("update ReminderMember rm set rm.status=:status where rm.reminderMemberId=:reminderMemberId")
	public void updateStatus(@Param("reminderMemberId") Long reminderMemberId, @Param("status") String status);
	
	public ReminderMember findByReminderMemberId(Long reminderMemberId);

}
