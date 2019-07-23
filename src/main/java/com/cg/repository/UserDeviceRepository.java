package com.cg.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.cg.user.bo.UserDevice;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long>{
	public List<UserDevice> findByUserId(Long userId);
	public UserDevice findByDeviceTypeAndUserId(String deviceType,Long userId);
	public List<UserDevice> findByDeviceType(String deviceType);
	
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("delete from UserDevice ud where ud.userId = :userId and ud.deviceType =:deviceType")
	public void deleteByUserIdAndDeviceType(@Param("userId") Long userId,@Param("deviceType") String deviceType);
	
	@Modifying
	@Transactional
	public void deleteByUserId(Long userId);
}
