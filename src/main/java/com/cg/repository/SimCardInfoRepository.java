package com.cg.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cg.bo.SimCardInfo;

public interface SimCardInfoRepository extends JpaRepository<SimCardInfo, Long>{

	SimCardInfo findByUserId(Long userId);
}
