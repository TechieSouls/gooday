package com.cg.events.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.cg.events.bo.EventMember;

@Repository
public interface EventMemberRepository extends CrudRepository<EventMember,Long> {
	
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("update EventMember em set em.status = :status where em.eventMemberId = :eventMemberId")
	public void updateEventMemberForStatusByEventMemberId(@Param("status") String status,@Param("eventMemberId") Long eventMemberId);
	
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("update EventMember em set em.picture = :picture where em.userId = :userId")
	public void updateEventMemberForPictureByEventMemberId(@Param("picture") String picture,@Param("userId") Long userId);
	
	@Query("select em from EventMember em where em.processed = :processed and em.status = :status")
	public List<EventMember> findByProcessedAndStatus(@Param("processed") Integer processed,@Param("status") String status);
}
