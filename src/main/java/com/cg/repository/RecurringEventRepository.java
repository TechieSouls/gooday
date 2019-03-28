package com.cg.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cg.events.bo.RecurringEvent;

@Repository
public interface RecurringEventRepository extends JpaRepository<RecurringEvent, Long>{

	@Query("Select re from RecurringEvent re where processed = :processedStatus")
	public List<RecurringEvent> findUnprocessedEvents(@Param("processedStatus") int processedSatus);
	
	public RecurringEvent findBySourceEventIdAndCreatedById(String sourceEventId,Long userId);
	
	public List<RecurringEvent> findByCreatedByIdOrderByCreationTimestampDesc(Long userId);

	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("delete from RecurringEvent re where re.createdById = :createdById")
	public void deleteByCreatedById(@Param("createdById") Long createdById);

	@Query("select re from RecurringEvent re JOIN re.recurringPatterns rp where DATEDIFF(DATE(rp.slotsGeneratedUpto) , DATE(now())) < 120 group by re.recurringEventId")
	public List<RecurringEvent> findBySlotsGeneratedUptoAndCurrentTimeDifference(); 

}
