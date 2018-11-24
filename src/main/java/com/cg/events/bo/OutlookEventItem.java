package com.cg.events.bo;

import java.util.List;
import java.util.Map;

public class OutlookEventItem {
	
	private String id;
	private String subject;
	private Boolean isAllDay;
	private Map<String, String> start;
	private Map<String, String> end;
	private Map<String, String> location;
	private List<OutlookEventAttendees> attendees;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public Map<String, String> getStart() {
		return start;
	}
	public void setStart(Map<String, String> start) {
		this.start = start;
	}
	public Map<String, String> getEnd() {
		return end;
	}
	public void setEnd(Map<String, String> end) {
		this.end = end;
	}
	public Map<String, String> getLocation() {
		return location;
	}
	public void setLocation(Map<String, String> location) {
		this.location = location;
	}
	public List<OutlookEventAttendees> getAttendees() {
		return attendees;
	}
	public void setAttendees(List<OutlookEventAttendees> attendees) {
		this.attendees = attendees;
	}
	public Boolean getIsAllDay() {
		return isAllDay;
	}
	public void setIsAllDay(Boolean isAllDay) {
		this.isAllDay = isAllDay;
	}
}
