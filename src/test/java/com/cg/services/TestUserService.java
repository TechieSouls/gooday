package com.cg.services;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.cg.bo.UserStat;
import com.cg.service.UserService;
import com.cg.user.bo.User;

public class TestUserService {

	@Autowired
	UserService userService;
	
	@Test
	public void test() {
		fail("Not yet implemented");
	}

	public void testFindUserStatByUserId() {
		
		String email = "ahmad.fuad@cenesgroup.com";
		
		User user = userService.findUserByEmail(email);
		
		Assert.assertTrue("User does not exists!!", user != null);
		
		UserStat userStat = userService.findUserStatByUserId(user.getUserId());
		
		Assert.assertTrue("UserStats does not exists!!", userStat != null);
		Assert.assertTrue("User CenesContacts count is 0", userStat.getCenesContactCounts() > 0);
		Assert.assertTrue("User Events Hosted count is 0", userStat.getEventsHostedCounts() > 0);
		Assert.assertTrue("User Events Attended count is 0", userStat.getEventsAttendedCounts() > 0);
		
	}
	
}
