package com.cg.jobs;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.cg.events.bo.RecurringEvent;
import com.cg.manager.EventManager;
import com.cg.manager.EventTimeSlotManager;
import com.cg.manager.RecurringManager;
import com.cg.service.EventService;
import com.cg.threads.RecurringEventThread;

@Service
public class RecurringEventUpdaterJob {
	
	@Autowired
	EventService eventService;
	
	@Autowired
	EventManager eventManager;
	
	@Autowired
	EventTimeSlotManager eventTimeSlotManager;
	
	@Autowired
	RecurringManager recurringManager;
	
	//@Scheduled(cron="0 0/2 * * * *")
	public void runRecurringEventUpdatorJob() {
		System.out.println("[Date : "+new Date()+", RecurringEventUpdaterJob STARTS]");
		//Find All Unprocessed RecurringEvents:
		
		System.out.println("[Date : "+new Date()+", Deleting Recurring Events STARTS]");
		List<RecurringEvent> recurringEvents =  eventService.findAllDeletedRecurringEvents();
		for (RecurringEvent recurringEvent: recurringEvents) {
			
			RecurringEventThread recurringEventThread = new RecurringEventThread();
			recurringEventThread.setEventManager(eventManager);
			recurringEventThread.setEventTimeSlotManager(eventTimeSlotManager);
			recurringEventThread.setRecurringManager(recurringManager);
			recurringEventThread.setRecurringEventId(recurringEvent.getRecurringEventId());
			recurringEventThread.run();
		}
		System.out.println("[Date : "+new Date()+", Deleting Recurring Events ENDS]");
		

		System.out.println("[Date : "+new Date()+", RecurringEventUpdate STARTS]");

		List<RecurringEvent> unProcessedRecurringEvents =  eventService.findUnProcessRecurringEvents();
		
		//Find All Recurring Patterns
		if (unProcessedRecurringEvents != null && unProcessedRecurringEvents.size() > 0) {
			recurringManager.generateSlotsForRecurringEventList(unProcessedRecurringEvents);
		}
		System.out.println("[Date : "+new Date()+", RecurringEventUpdate ENDS]");

		System.out.println("[Date : "+new Date()+", RecurringEventUpdaterJob ENDS]");
	}
}
