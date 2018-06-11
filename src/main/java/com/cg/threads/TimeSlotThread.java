package com.cg.threads;

import java.util.List;

import com.cg.events.bo.Event;
import com.cg.manager.EventTimeSlotManager;

public class TimeSlotThread implements Runnable {
	
	private EventTimeSlotManager timeSlotManager;
	
	private List<Event> events;
	
	public List<Event> getEvents() {
		return events;
	}
	public void setEvents(List<Event> events) {
		this.events = events;
	}
	public EventTimeSlotManager getTimeSlotManager() {
		return timeSlotManager;
	}
	public void setTimeSlotManager(EventTimeSlotManager timeSlotManager) {
		this.timeSlotManager = timeSlotManager;
	}
	@Override
	public void run() {
		timeSlotManager.saveEventsInSlots(events);
	}
}