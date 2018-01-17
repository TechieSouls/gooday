package com.cg.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cg.bo.CenesProperty;
import com.cg.bo.CenesProperty.PropertyOwningEntity;

@Repository
public interface CenesPropertyRepository extends JpaRepository<CenesProperty, Long> {
	
	public CenesProperty findByNameAndPropertyOwner(String name,PropertyOwningEntity propertyOwner);
}
