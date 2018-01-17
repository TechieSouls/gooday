package com.cg.events;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.cg.CgApplication;
import com.cg.events.bo.Event;
import com.cg.events.bo.Event.ScheduleEventAs;
import com.cg.events.bo.EventMember;
import com.cg.events.bo.PredictiveCalendar;
import com.cg.events.bo.RecurringPattern;
import com.cg.events.controller.EventController;
import com.cg.jobs.RecurringEventUpdaterJob;
import com.cg.user.bo.User;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CgApplication.class)
@WebAppConfiguration
public class TestEventController extends TestCase{

	@Autowired
	EventController eventController;
	
	@Autowired
	RecurringEventUpdaterJob job;
	
	@Test
	public void testSaveEvent() {
		
		String searchStartDateBetween = "2017-08-16 00:06:00";
		String searchEndDateBetween = "2017-08-16 00:08:00";
		
		try {
			Event event = new Event();
			event.setTitle("Gym Time");
			event.setDescription("Time to go office");
			event.setCreatedById(16l);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); 
			
			Date startDate = sdf.parse("2017-08-18 19:00:00");
			event.setStartTime(startDate);
			
			Date endDate = sdf.parse("2017-08-18 20:00:00");
			event.setEndTime(endDate);
			event.setScheduleAs(ScheduleEventAs.MeTime.toString());
			
			List<EventMember> eventMem = new ArrayList<>();
			EventMember eventMember = new EventMember();
			//eventMember.set
			//eventMem
			//event = eventController.createEvent(event);
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(startDate);
			System.out.println(cal.getTime().getDay());
			
			//RecurringPattern rp = new RecurringPattern();
			//rp.setDayOfWeek(cal.getTime().getDay());
			//rp = eventController.saveRecurringPattern(rp);
			
			
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void testSearchAvaiableUsers() {
		
		String searchStartDateBetween = "2017-08-16 00:06:00";
		String searchEndDateBetween = "2017-08-16 00:08:00";
		
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); 
			
			Date startDate = sdf.parse("2017-08-16 20:30:00");
			
			Date endDate = sdf.parse("2017-08-16 21:30:00");
			
			List<User> users = eventController.getAvailableUsers(startDate,endDate);
			System.out.println(users.size());
			
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void testFacebookEvents() {
		String facbeookId = "1874010556259204";
		String accessToken = "EAACEdEose0cBAK2Dlpkb8w0TdPnmmN58utoTwi7FWX7c2vFqsGm2cToWQ6O8Lz06SCaK26U1o6wmDZCYXZAebmZCNxJWVZA0V0esH3ZBN4r4ym79iX7W7yUfMpkTNkadLwvz1hef5gdwPxxJbnGzYpxH1HdzgAZAcfLwYfZAqxSalZAZAlRL7U1gvnZBfao6HI0bQZD";
		eventController.getFacebookUserEvents(facbeookId,accessToken);
	}
	
	@Test
	public void testGetGoogleEvents() {
		Long userId = 31l;
		String accessToken = "ya29.GlubBBScEFjCfKLyATTfY1CWHr6FB7VPIdXuAxAZLJ20yk1zbc6vOOrMWxU46Ltmp_I-x840uPYgj1jEAqEc5Kx_ZvvVtZuwj6bx0xpJ2kc9sI4Q4RhoCDlPhe0E";
		eventController.getGoogleEvents(accessToken,userId);
	}
	
	@Test
	public void testPredictiveCalendarData() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 18);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		long startTime = cal.getTimeInMillis();
		
		cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 19);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		long endTime = cal.getTimeInMillis();
		
		
		//List<PredictiveCalendar> predictiveCalendars = eventController.predictiveCalendarData(22l,startTime,endTime);
		//System.out.println(predictiveCalendars);
		
	}
	
	@Test
	public void testRunRecurringEventUpdatorJob() {
		job.runRecurringEventUpdatorJob();
	}
	
}
