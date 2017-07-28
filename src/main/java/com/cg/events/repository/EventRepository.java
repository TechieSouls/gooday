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
	List<Event> findByCreatedById(Long userId);
	
	@Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Event e WHERE e.sourceEventId = :sourceEventId")
	boolean existsBySourceEventId(@Param("sourceEventId") String sourceEventId);
	
	@Query("select e from Event e where createdById = :createdById and DATE(startTime) = :eventDate and timezone = :timezone order by startTime asc")
	List<Event> findByCreatedByIdAndStartDateAndTimeZone(@Param("createdById") Long createdById,@Param("eventDate") Date eventDate,@Param("timezone") String timezone);
}
