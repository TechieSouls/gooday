package com.cg.events.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cg.events.bo.Event;

@Repository
public interface EventRepository extends CrudRepository<Event, Long> {
	@Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Event e WHERE e.sourceEventId = :sourceEventId")
	boolean existsBySourceEventId(@Param("sourceEventId") String sourceEventId);
	
	Event findBySourceEventId(String sourceEventId);
	
	@Query("select e from Event e where createdById = :createdById and DATE(startTime) = :eventDate and timezone = :timezone order by startTime asc")
	List<Event> findByCreatedByIdAndStartDateAndTimeZone(@Param("createdById") Long createdById,@Param("eventDate") Date eventDate,@Param("timezone") String timezone);
	
	@Query("select e from Event e where createdById = :createdById and DATE(startTime) >= now() order by startTime asc")
	List<Event> findByCreatedById(@Param("createdById") Long createdById);
	
	@Query("select e from Event e where createdById = :createdById and DATE(startTime) >= now() and timezone = :timezone order by startTime asc")
	List<Event> findByCreatedByIdAndTimezone(@Param("createdById") Long createdById,@Param("timezone") String timezone);
	
	@Query("select e from Event e where createdById = :createdById and DATE(startTime) = :eventDate order by startTime asc")
	List<Event> findByCreatedByIdAndStartDate(@Param("createdById") Long createdById,@Param("eventDate") Date eventDate);
}
