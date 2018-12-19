package com.cg.events.repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.cg.events.bo.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
	@Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Event e WHERE e.sourceEventId = :sourceEventId")
	boolean existsBySourceEventId(@Param("sourceEventId") String sourceEventId);
	
	List<Event> findBySourceEventId(String sourceEventId);
	
	public void deleteByCreatedById(Long createdById);
	
	List<Event> findBySourceEventIdAndCreatedById(String sourceEventId,Long createdById);
	
	@Query("select e from Event e where DATE(e.startTime) >= :eventDate and DATE(e.startTime) <= :endDate and e.createdById = :createdById and e.scheduleAs in ('Event','Holiday','Gathering') and e.timezone = :timezone order by e.startTime asc")
	List<Event> findByCreatedByIdAndStartDateAndTimeZone(@Param("createdById") Long createdById,@Param("eventDate") Date eventDate,@Param("endDate") Date endDate,@Param("timezone") String timezone);
	
	@Query("select e from Event e where DATE(e.startTime) >= now() and DATE(e.startTime) <= :endDate and e.createdById = :createdById and e.scheduleAs in ('Event','Holiday','Gathering') order by e.startTime asc")
	List<Event> findByCreatedById(@Param("createdById") Long createdById,@Param("endDate") Date endDate);
	
	@Query("select e from Event e LEFT JOIN e.eventMembers em where DATE(e.startTime) >= now() and DATE(e.startTime) <= :endDate and e.createdById = :createdById or em.userId = :createdById and em.status = 'Going'and e.scheduleAs in ('Event','Holiday','Gathering') and e.timezone = :timezone order by e.startTime asc")
	List<Event> findByCreatedByIdAndTimezone(@Param("createdById") Long createdById,@Param("endDate") Date endDate,@Param("timezone") String timezone);
	
	@Query("select e from Event e where DATE(e.startTime) >= :eventDate  and DATE(e.startTime) <= :endDate and e.createdById = :createdById and e.scheduleAs in ('Event','Holiday','Gathering') order by e.startTime asc")
	List<Event> findByCreatedByIdAndStartDate(@Param("createdById") Long createdById,@Param("eventDate") Date eventDate,@Param("endDate") Date endDate);
	
	@Query("select e from Event e where DATE(e.startTime) >= :startTime  and DATE(e.startTime) <= :endTime and e.createdById = :createdById order by e.startTime asc")
	List<Event> findByCreatedByIdAndStartTimeAndEndTime(@Param("createdById") Long createdById,@Param("startTime") Date eventDate,@Param("endTime") Date endDate);
	
	@Query("select e from Event e where processed = :processed")
	List<Event> findByEventProcessedOrNot(@Param("processed") int processed,Pageable pageable);
	
	public List<Event> findByCreatedByIdAndSourceAndScheduleAs(Long userId, String source, String scheduleAs);
	
	@Query("select e from Event e where createdById = :userId and scheduleAs in (:indicatorOptions)")
	public List<Event> findByCreatedByIdAndScheduleAs(@Param("userId") Long userId,@Param("indicatorOptions") List<String> indicatorOptions);

	@Query("select e from Event e where e.eventId in (:eventIds)")
	public List<Event> findByEventIds(@Param("eventIds") List<Long> eventIds);

	@Query("select e from Event e JOIN e.eventMembers em where DATE(e.startTime) >= :startTime  and DATE(e.startTime) <= :endTime and e.createdById != :createdById and em.userId = :createdById and em.status = 'Going' order by e.startTime asc")
	List<Event> findByEventMemberIdAndEventMemberStatusAndStartTimeAndEndTime(@Param("createdById") Long createdById,@Param("startTime") Date eventDate,@Param("endTime") Date endDate);

	@Query("select e from Event e JOIN e.eventMembers em where DATE(e.startTime) >= DATE(now()) and e.createdById != :createdById and em.userId = :createdById and em.status is null order by e.startTime asc")
	List<Event> findPendingEvents(@Param("createdById") Long createdById);
	
	@Query("select e from Event e JOIN e.eventMembers em where DATE(e.endTime) >= DATE(now()) and e.source = 'Cenes' and e.scheduleAs = 'Gathering' and em.userId = :userId and em.status = :status order by e.startTime asc")
	public List<Event> findFutureGatherings(@Param("userId") Long userId,@Param("status") String status);
	
	
	@Query("select e from Event e JOIN e.eventMembers em where e.startTime >= :eventDate  and e.startTime <= :endDate and em.userId = :createdById and em.status = 'Going' and e.scheduleAs in ('Event','Holiday','Gathering') order by e.startTime asc")
	List<Event> findByCreatedByIdAndStartDateAndEventMemberStatus(@Param("createdById") Long createdById,@Param("eventDate") Date eventDate,@Param("endDate") Date endDate);

	@Query("select e from Event e JOIN e.eventMembers em where DATE(e.startTime) >= now() and em.userId = :createdById and em.status = 'Going' and e.scheduleAs in ('Event','Holiday','Gathering') order by e.startTime asc")
	List<Event> findByCreatedByIdAndStartDateOnlyAndEventMemberStatus(@Param("createdById") Long createdById);
		
	@Query("select e from Event e where  DATE_FORMAT(now(),'%Y-%m-%d %H:%i:00') >= DATE_FORMAT(e.endTime,'%Y-%m-%d %H:%i:00') and e.source = 'Cenes' and e.scheduleAs = 'Gathering' order by e.startTime asc")
	List<Event> findPastUserGatherings();
	
	@Query("select e from Event e JOIN e.eventMembers em where em.processed = :processed and em.status = :status")
	List<Event> findByEventMemberUnProcessedAndStatus(@Param("processed") int processed,@Param("status") String status);

	@Query("select e from Event e where e.startTime >= now() and e.scheduleAs = 'Gathering' and TIMESTAMPDIFF(MINUTE,now(),e.startTime) = 0 order by e.startTime asc")
	List<Event> findAllEventsWithTimeDifferenceEqualToOne();
	
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("delete from Event e where e.createdById = :createdById and e.source = :source and e.scheduleAs = 'Event'")
	public void deleteEventsByCreatedByIdAndSource(@Param("createdById") Long createdById,@Param("source") String source);
	
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("delete from Event e where e.createdById = :createdById and e.source = :source and e.scheduleAs = :scheduleAs")
	public void deleteEventsByCreatedByIdAndSourceAndScheduleAs(@Param("createdById") Long createdById,@Param("source") String source,@Param("scheduleAs") String scheduleAs);
	
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("delete from Event e where e.createdById = :createdById and e.scheduleAs = :scheduleAs")
	public void deleteEventsByCreatedByIdAndScheduleAs(@Param("createdById") Long createdById,@Param("scheduleAs") String scheduleAs);

}
