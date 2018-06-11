package com.cg.jobs;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.cg.events.bo.Event;
import com.cg.events.bo.RecurringEvent;
import com.cg.events.bo.RecurringPattern;
import com.cg.manager.RecurringManager;
import com.cg.service.EventService;

@Service
public class RecurringPatternUpdaterJob {

	@Autowired
	RecurringManager recurringManager;
	
	@Autowired
	EventService eventService;
	
	@Scheduled(cron="0 0/1 * * * *")
	public void runRecurringPatternUpdaterJob() {
		System.out.println("[Date : "+new Date()+", Daily RecurringPatternUpdaterJob STARTS]");

		List<RecurringEvent> recurringEvents = recurringManager.getMoreThanFourMonthsOldRecurringPattern();
		//Find All Recurring Patterns
		if (recurringEvents != null && recurringEvents.size() > 0) {
			for (RecurringEvent recurringEvent : recurringEvents) {
				List<RecurringPattern> patterns = recurringEvent.getRecurringPatterns();
				if (patterns != null && patterns.size() > 0) {
					for (RecurringPattern pattern : patterns) {
						Calendar currentCal = Calendar.getInstance();
						currentCal.setTime(pattern.getSlotsGeneratedUpto());
							
							if (pattern.getDayOfWeek() != null) {//Event to be occurred Daily
								List<Event> dailyEvents = recurringManager.handleDailyEventLogic(currentCal,recurringEvent,pattern);
								if (dailyEvents != null && dailyEvents.size() > 0) {
									eventService.saveEventsBatch(dailyEvents);
									System.out.println("Saving Daily Events Batch Size : "+dailyEvents.size());
									Event event = dailyEvents.get(dailyEvents.size()-1);
									
									Calendar generatedUptoCal = Calendar.getInstance();
									generatedUptoCal.setTime(event.getStartTime());
									generatedUptoCal.add(Calendar.DAY_OF_MONTH,1);
									pattern.setSlotsGeneratedUpto(generatedUptoCal.getTime());
								}
							} else if (pattern.getMonthOfYear() != null) {//Event to be occurred Yearly
								List<Event> yearlyEvents = recurringManager.handleYearlyEventLogic(currentCal,recurringEvent,pattern);
								if (yearlyEvents != null && yearlyEvents.size() > 0) {
									eventService.saveEventsBatch(yearlyEvents);
									System.out.println("Saving Yearly Events Batch Size : "+yearlyEvents.size());
								}
							}
					}
				}
				recurringEvent.setUpdateTimestamp(new Date());
				eventService.saveUpdateRecurringEvent(recurringEvent);
			}
		}
		
		System.out.println("[Date : "+new Date()+", Daily RecurringPatternUpdaterJob ENDS]");
	}
}
