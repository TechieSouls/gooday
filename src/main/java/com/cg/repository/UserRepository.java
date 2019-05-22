package com.cg.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cg.user.bo.User;
@Repository
public interface UserRepository extends CrudRepository<User, Long> {

	User findUserByUsername(String username);
	User findUserByFacebookId(String facebookID);
	User findUserByGoogleId(String googleId);
	User findUserByEmail(String email);
	User findByEmailAndFacebookIdIsNull(String email);
	User findByEmailAndGoogleIdIsNull(String email);
	User findUserByEmailAndPassword(String email,String password);
	User findByPhone(String phone);
	List<User> findByPhoneContaining(String phone);
	User findByEmailAndResetTokenIsNull(String email);
	
	@Query("select u from User u where resetToken =:resetToken and HOUR(TIMEDIFF(resetTokenCreatedAt,now())) <= 1")
	User findByResetTokenAndResetTokenCreatedAt(@Param("resetToken") String resetToken);
	
	@Query("select u from User u where name LIKE CONCAT(:name,'%')")
	List<User> findUserByName(@Param("name") String name);
	
	
	@Transactional
	@Modifying
	@Query("update User u set u.name = :name where u.userId = :userId")
	public void updateNameByUserId(@Param("name") String name, @Param("userId") Long userId);

	@Transactional
	@Modifying
	@Query("update User u set u.gender = :gender where u.userId = :userId")
	public void updateGenderByUserId(@Param("gender") String gender, @Param("userId") Long userId);

	@Transactional
	@Modifying
	@Query("update User u set u.birthDayStr = :dobStr where u.userId = :userId")
	public void updateDobStrByUserId(@Param("dobStr") String dobStr, @Param("userId") Long userId);

	@Transactional
	@Modifying
	@Query("update User u set u.password = :password where u.userId = :userId")
	public void updatePasswordByUserId(@Param("password") String password, @Param("userId") Long userId);

	@Transactional
	@Modifying
	public void deleteByUserId(Long userId);
}
