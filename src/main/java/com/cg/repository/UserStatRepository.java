package com.cg.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cg.bo.UserStat;

@Repository
public interface UserStatRepository extends JpaRepository<UserStat, Long>{

	public UserStat findByUserId(Long userId);
	
	public void deleteByUserId(Long userId);
	
}
