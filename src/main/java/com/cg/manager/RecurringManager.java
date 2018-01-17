package com.cg.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cg.events.bo.Event;
import com.cg.events.bo.RecurringEvent;
import com.cg.events.bo.RecurringPattern;
import com.cg.events.bo.Event.EventProcessedStatus;
import com.cg.events.bo.Event.ScheduleEventAs;
import com.cg.events.repository.RecurringPatternRepository;
import com.cg.repository.RecurringEventRepository;

@Service
public class RecurringManager {
	
	private static int generateEventsUptoOneYear = 12*4; 
	
	@Autowired
	RecurringPatternRepository recurringPatternRepository;
	
	@Autowired
	RecurringEventRepository recurringEventRepository;
	
	
	public List<RecurringEvent> getMoreThanTwoMonthsOldRecurringPattern() {
		return recurringEventRepository.findBySlotsGeneratedUptoAndCurrentTimeDifference();
	}
	
	public List<Event> handleDailyEventLogic(Calendar currentCal,RecurringEvent recurringEvent,RecurringPattern recurringPattern) {
		List<Event> dailyEvents = new ArrayList<>();
		
		try {
			for (int i=1; i <= generateEventsUptoOneYear; i++) {//Make Events up to next 2 months
				
				//Get Current Day of month/year
				int dayOfWeek = recurringPattern.getDayOfWeek();
				while (currentCal.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
					currentCal.add(Calendar.DATE,1);
				}
				
				Calendar tempCal = Calendar.getInstance();
				tempCal.setTimeInMillis(recurringEvent.getStartTime().getTime());
				
				//User current Calendar to get Current Day for event
				Calendar startCal = Calendar.getInstance();
				startCal.setTime(new Date());
				startCal.set(Calendar.DAY_OF_MONTH, currentCal.get(Calendar.DAY_OF_MONTH));
				startCal.set(Calendar.YEAR,currentCal.get(currentCal.YEAR));
				startCal.set(Calendar.MONTH,currentCal.get(currentCal.MONTH));
				startCal.set(Calendar.HOUR_OF_DAY,tempCal.get(Calendar.HOUR_OF_DAY));
				startCal.set(Calendar.MINUTE, tempCal.get(Calendar.MINUTE));
				startCal.set(Calendar.SECOND, 0);
				startCal.set(Calendar.MILLISECOND, 0);
				System.out.println("Start Time : "+startCal.getTime());
				
				
				System.out.println("Recurring Event Time : "+recurringEvent.getEndTime());
				tempCal = Calendar.getInstance();
				tempCal.setTimeInMillis(recurringEvent.getEndTime().getTime());
				System.out.println("Temp End Cal : "+tempCal.getTime());
				
				Calendar recrringStartCal = Calendar.getInstance();
				recrringStartCal.setTimeInMillis(recurringEvent.getStartTime().getTime());
				System.out.println("Recurring Start Date : "+recrringStartCal.getTime());
				
				Calendar recrringEndCal = Calendar.getInstance();
				recrringEndCal.setTimeInMillis(recurringEvent.getEndTime().getTime());
				System.out.println("Recurring End Date : "+recrringEndCal.getTime());
				
				
				
				System.out.println(tempCal.getTime());
				
				Calendar endCal = Calendar.getInstance();
				endCal.setTime(new Date());
				if (recrringStartCal.get(Calendar.HOUR_OF_DAY) > recrringEndCal.get(Calendar.HOUR_OF_DAY)) {
					currentCal.add(Calendar.DAY_OF_MONTH, 1);
				}
				endCal.set(Calendar.DAY_OF_MONTH, currentCal.get(Calendar.DAY_OF_MONTH));
				endCal.set(Calendar.MONTH,currentCal.get(currentCal.MONTH));
				endCal.set(Calendar.YEAR,currentCal.get(currentCal.YEAR));
				endCal.set(Calendar.HOUR_OF_DAY,tempCal.get(Calendar.HOUR_OF_DAY));
				endCal.set(Calendar.MINUTE, tempCal.get(Calendar.MINUTE));
				endCal.set(Calendar.SECOND, 0);
				endCal.set(Calendar.MILLISECOND, 0);
				System.out.println("End Time : "+endCal.getTime());
				
				//Create Events for Recurring Events:
				Event event = populateEventObject(recurringEvent,startCal.getTime(),endCal.getTime(),ScheduleEventAs.MeTime.toString());
				dailyEvents.add(event);
				currentCal.add(Calendar.DATE,1);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return dailyEvents;
	}
	
	public List<Event> handleYearlyEventLogic(Calendar currentCal,RecurringEvent recurringEvent,RecurringPattern recurringPattern) {
		List<Event> yearlyEvents = new ArrayList<>();
		try {
			for (int i=1; i <= 2; i++) {//Make Events up to next 2 Years
				
				//Get Current Month Of Year
				int monthOfYear = recurringPattern.getMonthOfYear();
				while (currentCal.get(Calendar.MONTH) != monthOfYear) {
					currentCal.add(Calendar.MONTH,1);
				}
				
				Calendar tempCal = Calendar.getInstance();
				tempCal.setTimeInMillis(recurringEvent.getStartTime().getTime());
				
				//User current Calendar to get Current Day for event
				Calendar startCal = Calendar.getInstance();
				startCal.setTime(new Date());
				startCal.set(Calendar.DAY_OF_MONTH, tempCal.get(Calendar.DAY_OF_MONTH));
				startCal.set(Calendar.YEAR,currentCal.get(currentCal.YEAR));
				startCal.set(Calendar.MONTH,currentCal.get(currentCal.MONTH));
				startCal.set(Calendar.HOUR,0);
				startCal.set(Calendar.MINUTE, 0);
				startCal.set(Calendar.SECOND, 0);
				startCal.set(Calendar.MILLISECOND, 0);
				//System.out.println("Start Time : "+startCal.getTime());
				
				tempCal = Calendar.getInstance();
				tempCal.setTimeInMillis(recurringEvent.getEndTime().getTime());
				Calendar endCal = Calendar.getInstance();
				endCal.setTime(new Date());
				endCal.set(Calendar.DAY_OF_MONTH, tempCal.get(Calendar.DAY_OF_MONTH));
				endCal.set(Calendar.MONTH,currentCal.get(currentCal.MONTH));
				endCal.set(Calendar.YEAR,currentCal.get(currentCal.YEAR));
				endCal.set(Calendar.HOUR,0);
				endCal.set(Calendar.MINUTE, 0);
				endCal.set(Calendar.SECOND, 0);
				endCal.set(Calendar.MILLISECOND, 0);
				if (endCal.getTimeInMillis() < startCal.getTimeInMillis()) {
					endCal.add(Calendar.MONTH, 1);
				}
				//System.out.println("End Time : "+endCal.getTime());

				//Create Events for Recurring Events:
				Event event = populateEventObject(recurringEvent,startCal.getTime(),endCal.getTime(),ScheduleEventAs.Holiday.toString());
				yearlyEvents.add(event);
				
				currentCal.add(Calendar.MONTH,1);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return yearlyEvents;
	}
	
	public Event populateEventObject(RecurringEvent recurringEvent,Date startTime,Date endTime,String schedulesAs) {
		Event event = new Event();
		event.setTitle(recurringEvent.getTitle());
		event.setDescription(recurringEvent.getDescription());
		event.setStartTime(startTime);
		event.setEndTime(endTime);
		event.setSource(recurringEvent.getSource());
		event.setSourceEventId(recurringEvent.getSourceEventId());
		event.setScheduleAs(schedulesAs);
		event.setTimezone(recurringEvent.getTimezone());
		event.setRecurringEventId(recurringEvent.getRecurringEventId().toString());
		event.setCreatedById(recurringEvent.getCreatedById());
		event.setProcessed(EventProcessedStatus.UnProcessed.ordinal());
		return event;
	}
	
	
	public void deleteRecurringEventsByUserId(Long userId) {
		recurringEventRepository.deleteByCreatedById(userId);
	}
	
	public void saveRecurringEvent(RecurringEvent recurringEvent) {
		recurringEventRepository.save(recurringEvent);
	}
	
	public List<RecurringEvent> findRecurringEventsByCreatedById(Long createdById) {
		return recurringEventRepository.findByCreatedById(createdById);
	}	
}
