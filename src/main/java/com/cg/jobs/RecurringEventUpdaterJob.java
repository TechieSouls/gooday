package com.cg.jobs;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cg.events.bo.RecurringEvent;
import com.cg.manager.RecurringManager;
import com.cg.service.EventService;

@Service
public class RecurringEventUpdaterJob {
	
	@Autowired
	EventService eventService;
	
	@Autowired
	RecurringManager recurringManager;
	
	//@Scheduled(cron="0 0/2 * * * *")
	public void runRecurringEventUpdatorJob() {
		System.out.println("[Date : "+new Date()+", RecurringEventUpdaterJob STARTS]");
		//Find All Unprocessed RecurringEvents:
		List<RecurringEvent> recurringEvents =  eventService.findUnProcessRecurringEvents();
		
		//Find All Recurring Patterns
		if (recurringEvents != null && recurringEvents.size() > 0) {
			for (RecurringEvent recurringEvent : recurringEvents) {
				//recurringManager.processRecurringEvent(recurringEvent);
			}
		}
		
		System.out.println("[Date : "+new Date()+", RecurringEventUpdaterJob ENDS]");
	}
}
