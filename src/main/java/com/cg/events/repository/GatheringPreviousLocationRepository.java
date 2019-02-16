package com.cg.events.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cg.bo.GatheringPreviousLocation;

public interface GatheringPreviousLocationRepository extends JpaRepository<GatheringPreviousLocation, Long>{

	public List<GatheringPreviousLocation> findByUserId(Long userId);
	
	public void deleteByEventId(Long eventId);
	
	public List<GatheringPreviousLocation> findTop15ByUserIdOrderByGatheringPreviousLocationIdDesc(Long userId);
}
