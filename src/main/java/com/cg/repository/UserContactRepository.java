package com.cg.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cg.user.bo.UserContact;

@Repository
public interface UserContactRepository extends JpaRepository<UserContact, Long>{

	List<UserContact> findByUserId(Long userId);
	List<UserContact> findByPhone(String phone);
	List<UserContact> findByUserIdOrderByNameAsc(Long userId);
	List<UserContact> findByPhoneContaining(String phone);
	
	
	@Query("SELECT COUNT(uc) FROM UserContact uc WHERE uc.cenesMember = 1 and uc.friendId is not null and uc.userId = :userId")
    public Long findCenesMemberCountsByUserId(@Param("userId") Long userId);
	
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("update UserContact uc set uc.friendId = :friendId where phone = :phone")
	public void updateFrindIdByPhone(@Param("friendId") Long friendId, @Param("phone") String phone);
	public void deleteByUserId(Long userId); 

}
