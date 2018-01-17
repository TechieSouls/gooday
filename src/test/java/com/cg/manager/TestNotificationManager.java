package com.cg.manager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.cg.CgApplication;
import com.cg.events.bo.EventMember;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CgApplication.class)
@WebAppConfiguration
public class TestNotificationManager {
	
	@Autowired
	NotificationManager notificationManager;
	
	public void testSendAcceptDeclinedPush() {
		EventMember eventMember = new EventMember();
		
		notificationManager.sendEventAcceptDeclinedPush(eventMember);
	}
	
	@Test
	public void testAndroidPush() {
		notificationManager.sendTestingNotificationToAndroid();
	}
	@Test
	public void testiOSPush() {
		notificationManager.sendTestingNotificationToIos();
	}
	
}
