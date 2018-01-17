package com.cg.users;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.cg.CgApplication;
import com.cg.events.bo.MeTime;
import com.cg.events.bo.MeTimeEvent;
import com.cg.manager.EmailManager;
import com.cg.user.controller.UserController;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CgApplication.class)
@WebAppConfiguration
public class TestUserController extends TestCase {

	@Autowired
	UserController userController;
	
	@Autowired
	EmailManager emailManager;
	
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testSaveMeTime() {
		MeTime metime = new MeTime();
		
		List<MeTimeEvent> meTimeEvents = new ArrayList<>();
		
		MeTimeEvent meTimeEvent = new MeTimeEvent();
		meTimeEvent.setDayOfWeek("Monday");
		meTimeEvent.setDescription("BedTime");
		//meTimeEvent.setStartTime(new Date().toString());
		//meTimeEvent.setEndTime(new Date().toString());
		meTimeEvent.setTitle("BedTime");
		meTimeEvents.add(meTimeEvent);
		
		metime.setEvents(meTimeEvents);
		metime.setUserId(1l);
		metime.setTimezone(TimeZone.getDefault().getDisplayName());
		
		ResponseEntity<String> meTimeResponse = userController.saveMeTime(metime);
		System.out.println(meTimeResponse);
		
	}
	
	@Test
	public void testSendForgotPasswordLink() {
		emailManager.sendForgotPasswordLink();
	}
	
}
