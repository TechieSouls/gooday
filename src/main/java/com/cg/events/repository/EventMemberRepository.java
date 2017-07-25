package com.cg.events.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.cg.events.bo.EventMember;

@Repository
public interface EventMemberRepository extends CrudRepository<EventMember,Long> {
	
}
