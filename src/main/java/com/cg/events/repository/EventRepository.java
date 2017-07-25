package com.cg.events.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.cg.events.bo.Event;

@Repository
public interface EventRepository extends CrudRepository<Event, Long> {
	List<Event> findByCreatedById(Long userId);
}
