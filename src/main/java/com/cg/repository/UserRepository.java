package com.cg.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.cg.user.bo.User;
@Repository
public interface UserRepository extends CrudRepository<User, Long> {

	User findUserByUsername(String username);
	User findUserByFacebookID(String facebookID);
	User findUserByEmail(String email);

}
