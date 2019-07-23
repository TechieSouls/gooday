package com.cg.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.cg.CgApplication;
import com.cg.bo.CalendarSyncToken;
import com.cg.service.GoogleService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CgApplication.class)
@WebAppConfiguration
public class TestGoogleService {

	@Test
	public void testGoogleCalendarlist() {
		String accessToken = "ya29.Glv2BbI7x5MAY-QWGeH7Nnopq3IdtZVxHO9s16T5F6evD9Uvw7gUPzjrhqAk5ea6s3eyy3ubW3jnzWlsJsrdW48dI6ONkh3jUlU6shC6z7_mvb90nUcHD75cRfQ5";
		GoogleService googleService = new GoogleService();
		googleService.googleCalendarList(accessToken);
	}
	
	@Test
	public void testGetCalenderEvents() {
		String accessToken = "ya29.Glv2BbI7x5MAY-QWGeH7Nnopq3IdtZVxHO9s16T5F6evD9Uvw7gUPzjrhqAk5ea6s3eyy3ubW3jnzWlsJsrdW48dI6ONkh3jUlU6shC6z7_mvb90nUcHD75cRfQ5";
		GoogleService googleService = new GoogleService();
		googleService.getCalenderEvents(false, accessToken);
		
	}
	
	@Test
	public void testGetAccessTokenFromRefreshToken() {
		
		String refreshToken = "1/FQaheqSjSyBzcUUGxt5V7HC0l6uFVpkI0ITYxYmRUt8";
		GoogleService googleService = new GoogleService();
		googleService.getAccessTokenFromRefreshToken(refreshToken);

	}
	
	@Test
	public void testUnsubscribeGoogleNotification() {
	
		CalendarSyncToken calendarSyncToken = new CalendarSyncToken();
		calendarSyncToken.setSubscriptionId("45a05a3b-8b39-4f14-966f-7030da6eee11");
		calendarSyncToken.setResourceId("S-XqvTH_evOhnlozkiW0SAZhXdI");
		calendarSyncToken.setRefreshToken("1/W9b--WxlMNBE003g9OG7o7kTqBOpAe1cmWR3MzZRcTk");
		
		
		GoogleService googleService = new GoogleService();
		googleService.unsubscribeGoogleNotification(calendarSyncToken);
		
	}
	
}
