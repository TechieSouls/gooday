package com.cg.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cg.user.bo.UserContact;

@Repository
public interface UserContactRepository extends JpaRepository<UserContact, Long>{

	List<UserContact> findByUserId(Long userId);
	List<UserContact> findByPhone(String phone);
	List<UserContact> findByUserIdOrderByNameAsc(Long userId);

}
