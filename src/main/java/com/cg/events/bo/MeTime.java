package com.cg.events.bo;

import java.util.List;

public class MeTime {

	private Long userId;
	private Long recurringEventId;
	private List<MeTimeEvent> events;
	private String timezone;
	
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public List<MeTimeEvent> getEvents() {
		return events;
	}
	public void setEvents(List<MeTimeEvent> events) {
		this.events = events;
	}
	public String getTimezone() {
		return timezone;
	}
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}
	public Long getRecurringEventId() {
		return recurringEventId;
	}
	public void setRecurringEventId(Long recurringEventId) {
		this.recurringEventId = recurringEventId;
	}
}
