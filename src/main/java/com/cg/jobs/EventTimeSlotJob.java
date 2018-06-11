package com.cg.jobs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.cg.events.bo.Event;
import com.cg.manager.EventTimeSlotManager;
import com.cg.threads.TimeSlotThread;

@Service
public class EventTimeSlotJob {
	
	@Autowired
	EventTimeSlotManager timeSlotManager;
	
	@Scheduled(cron="0 0/1 * * * *")
	public void runSaveEventsInSlots() {
		System.out.println("[Date : "+new Date()+", EventTimeSlotJob Job STARTS]");
		runEventTimeSlotJob();
		runEventMemberTimeSlotJob();
		System.out.println("[Date : "+new Date()+", EventTimeSlotJob Job ENDS]");
	}
	
	public void runEventTimeSlotJob() {
		List<Event> events = timeSlotManager.findEventsToProcessed();
		if (events != null && events.size() > 0) {
			List<Event> eventsToAllocateToThread = new ArrayList<>();
			int trackElementsTraversed = 0;
			for (Event event : events) {
				eventsToAllocateToThread.add(event);
				trackElementsTraversed += 1;
				if (events.size() == trackElementsTraversed) {
					TimeSlotThread timeSlotThread = new TimeSlotThread();
					timeSlotThread.setTimeSlotManager(timeSlotManager);
					timeSlotThread.setEvents(eventsToAllocateToThread);
					timeSlotThread.run();
					eventsToAllocateToThread = new ArrayList<>();
				} else {
					if (eventsToAllocateToThread.size() == 10) {
						TimeSlotThread timeSlotThread = new TimeSlotThread();
						timeSlotThread.setTimeSlotManager(timeSlotManager);
						timeSlotThread.setEvents(eventsToAllocateToThread);
						timeSlotThread.run();
						eventsToAllocateToThread = new ArrayList<>();
					}
				}
			}
		}
	}
	
	public void runEventMemberTimeSlotJob() {
		List<Event> events = timeSlotManager.findEventMembersToProcessed();
		if (events != null && events.size() > 0) {
			List<Event> eventsToAllocateToThread = new ArrayList<>();
			int trackElementsTraversed = 0;
			for (Event event : events) {
				eventsToAllocateToThread.add(event);
				trackElementsTraversed += 1;
				if (events.size() == trackElementsTraversed) {
					EventMemberTimeSlotThread timeSlotThread = new EventMemberTimeSlotThread();
					timeSlotThread.setEvents(eventsToAllocateToThread);
					timeSlotThread.run();
					eventsToAllocateToThread = new ArrayList<>();
				} else {
					if (eventsToAllocateToThread.size() == 10) {
						EventMemberTimeSlotThread timeSlotThread = new EventMemberTimeSlotThread();
						timeSlotThread.setEvents(eventsToAllocateToThread);
						timeSlotThread.run();
						eventsToAllocateToThread = new ArrayList<>();
					}
				}
			}
		}
	}
	
	/*class TimeSlotThread implements Runnable {
		private List<Event> events;
		
		public List<Event> getEvents() {
			return events;
		}
		public void setEvents(List<Event> events) {
			this.events = events;
		}

		@Override
		public void run() {
			timeSlotManager.saveEventsInSlots(events);
		}
	}*/
	
	class EventMemberTimeSlotThread implements Runnable {
		private List<Event> events;
		
		public List<Event> getEvents() {
			return events;
		}
		public void setEvents(List<Event> events) {
			this.events = events;
		}
		@Override
		public void run() {
			timeSlotManager.saveEventMemberSlots(events);
		}
	}
}
