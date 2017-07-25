package com.cg.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.cg.bo.UserFriend;

public interface UserFriendRepository extends JpaRepository<UserFriend, Long>{

	
	public List<UserFriend> findByUserIdAndFriendId(Long userId,Long friendId);
	public List<UserFriend> findByUserId(Long userId,Pageable pageable);
	public List<UserFriend> findByFriendId(Long friendId,Pageable pageable);
}
