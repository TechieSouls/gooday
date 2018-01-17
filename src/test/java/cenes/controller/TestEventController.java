package cenes.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.cg.CgApplication;
import com.cg.events.bo.Event;
import com.cg.events.bo.EventMember;
import com.cg.events.bo.PredictiveCalendar;
import com.cg.events.controller.EventController;
import com.cg.jobs.EventTimeSlotJob;
import com.cg.manager.EmailManager;
import com.cg.manager.EventTimeSlotManager;
import com.cg.utils.CenesUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CgApplication.class)
@WebAppConfiguration
public class TestEventController {

	@Autowired
	EventController eventController;
	
	@Autowired
	EventTimeSlotJob eventTimeSlotJob;
	
	@Autowired
	EventTimeSlotManager timeSlotManager;
	
	@Autowired
	EmailManager emailManager;
	
	public void testCreateEvent() {
		
		Event event = new Event();
		event.setTitle("duni birthday");
		event.setDescription("Let celevrate duni bday in office");
		event.setLocation("Marriot,Chd");
		event.setCreatedById(1l);
		event.setSource("Cenes");
		event.setTimezone("Asia/Kolkota");
		event.setScheduleAs("Event");
		event.setEventPicture("http://cenes.test2.redblink.net/assets/default_images/default_event_image.png");
		
		List<EventMember> members = new ArrayList<>();
		EventMember member = new EventMember();
		member.setName("Duni Thakur");
		member.setUserId(2l);
		member.setPicture("http://cenes.test2.redblink.net/assets/uploads/59/profile/df4fd41a-42a8-4516-bb61-d3037781e6b1.jpg");
		members.add(member);
		
		member = new EventMember();
		member.setName("Duni Chand");
		member.setUserId(3l);
		member.setPicture("http://cenes.test2.redblink.net/assets/uploads/59/profile/df4fd41a-42a8-4516-bb61-d3037781e6b1.jpg");
		members.add(member);
		event.setEventMembers(members);
		//eventController.saveEvent(event);
	}
	
	@Test
	public void testSaveEventsInSlots() {
		
		try {
			String startTime = "2017-09-09 09:00:00";
			String endTime = "2017-09-09 15:30:00";
			
			List<Event> events = new ArrayList<>();
			Event event = new Event();
			event.setStartTime(CenesUtils.yyyyMMddTHHmmss.parse(startTime));
			event.setEndTime(CenesUtils.yyyyMMddTHHmmss.parse(endTime));
			event.setEventId(3l);
			event.setCreatedById(3l);
			events.add(event);
			timeSlotManager.saveEventsInSlots(events);
				
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testPredictiveCalendarData() {
		
		try {
			Calendar startCal = Calendar.getInstance();
			startCal.set(Calendar.DAY_OF_MONTH, 9);
			startCal.set(Calendar.HOUR_OF_DAY, 11);
			startCal.set(Calendar.MINUTE, 0);
			startCal.set(Calendar.SECOND, 0);
			startCal.set(Calendar.MILLISECOND, 0);
			System.out.println(startCal.getTime());
			System.out.println(startCal.getTimeInMillis());
			
			Calendar endCal = Calendar.getInstance();
			endCal.set(Calendar.DAY_OF_MONTH, 9);
			endCal.set(Calendar.HOUR_OF_DAY, 15);
			endCal.set(Calendar.MINUTE, 0);
			endCal.set(Calendar.SECOND, 0);
			endCal.set(Calendar.MILLISECOND, 0);
			System.out.println(endCal.getTime());
			System.out.println(endCal.getTimeInMillis());
			
			//ResponseEntity<List<PredictiveCalendar>> predictiveCalendar = eventController.predictiveCalendarData(1l, startCal.getTimeInMillis(), endCal.getTimeInMillis());
			//System.out.println(predictiveCalendar);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testDeleteEvents() {
		eventController.deleteEvent(5113l);
	}
	
	@Test
	public void testRunSaveEventsInSlots() {
		//eventTimeSlotJob.runSaveEventsInSlots();
		emailManager.sendForgotPasswordLink();
	}
}
