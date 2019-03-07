package com.cg.events.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.cg.events.bo.EventTimeSlot;

@Repository
public interface EventTimeSlotRepository extends  JpaRepository<EventTimeSlot, Long>{

	@Query("Select ets from EventTimeSlot ets where ets.status = 'Free' and DATE(ets.eventDate) >= :startDate and DATE(ets.eventDate) <= :endDate")
	public List<EventTimeSlot> findEventTimeSlotByStatusAndEventDate(@Param("startDate") Date startDate,@Param("endDate") Date endDate);
	
	@Query("Select ets from EventTimeSlot ets where ets.startTime >= :startTime and ets.endTime <= :endTime and ets.userId = :userId")
	public List<EventTimeSlot> findByStartAndEndTimeAndUserId(@Param("startTime") long startTime,@Param("endTime") long endTime,@Param("userId") Long userId);

	@Query("Select ets from EventTimeSlot ets where ets.startTime >= :startTime and ets.endTime <= :endTime and ets.eventId = :eventId")
	public List<EventTimeSlot> findByStartAndEndTimeAndEventId(@Param("startTime") long startTime,@Param("endTime") long endTime,@Param("eventId") Long eventId);

	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("delete from EventTimeSlot ets where ets.userId =:userId and ets.scheduleAs = :scheduleAs")
	public void deleteByUserIdAndScheduleAs(@Param("userId") Long userId,@Param("scheduleAs") String scheduleAs);
	
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("delete from EventTimeSlot ets where ets.userId =:userId and ets.source = :source")
	public void deleteByUserIdAndSource(@Param("userId") Long userId,@Param("source") String source);
	
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("delete from EventTimeSlot ets where ets.userId =:userId and ets.source = :source and ets.scheduleAs = :scheduleAs")
	public void deleteByUserIdAndSourceAndScheduleAs(@Param("userId") Long userId,@Param("source") String source,@Param("scheduleAs") String scheduleAs);
	
	public void deleteByEventId(Long eventId);
	
	public void deleteByUserId(Long userId);
	
	public void deleteByRecurringEventId(Long recurringEventId);
}
