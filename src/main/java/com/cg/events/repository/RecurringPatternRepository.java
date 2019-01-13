package com.cg.events.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.cg.events.bo.RecurringPattern;

@Repository
public interface RecurringPatternRepository extends JpaRepository<RecurringPattern, Long>{

	public RecurringPattern findByRecurringEventId(Long recurringEventId);
	
	public 
}
