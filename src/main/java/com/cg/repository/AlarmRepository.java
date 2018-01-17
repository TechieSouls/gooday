package com.cg.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cg.bo.Alarm;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long>{
	
	public List<Alarm> findByUserId(Long userId);
}
