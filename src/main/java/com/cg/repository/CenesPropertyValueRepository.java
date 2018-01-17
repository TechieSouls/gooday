package com.cg.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cg.bo.CenesProperty.PropertyOwningEntity;
import com.cg.bo.CenesPropertyValue;

@Repository
public interface CenesPropertyValueRepository extends JpaRepository<CenesPropertyValue, Long>{
	
	@Query("select cpv from CenesPropertyValue cpv JOIN cpv.cenesProperty cp where cpv.entityId = :userId and cpv.owningEntity = :propertyOwningEntity")
	List<CenesPropertyValue> findByEntityIdAndPropertyOwningEntity(@Param("userId") Long userId, @Param("propertyOwningEntity") PropertyOwningEntity propertyOwningEntity);
	
}
