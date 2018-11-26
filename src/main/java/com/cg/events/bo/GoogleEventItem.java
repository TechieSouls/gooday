package com.cg.events.bo;

import java.util.List;
import java.util.Map;

public class GoogleEventItem {

	private String id;
	private String status;
	private String summary;
	private String description;
	private String location;
	private String recurringEventId;
	private Map<String,String> start;
	private Map<String,String> end;
	private List<GoogleEventAttendees> attendees;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public List<GoogleEventAttendees> getAttendees() {
		return attendees;
	}
	public void setAttendees(List<GoogleEventAttendees> attendees) {
		this.attendees = attendees;
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
	public String getRecurringEventId() {
		return recurringEventId;
	}
	public void setRecurringEventId(String recurringEventId) {
		this.recurringEventId = recurringEventId;
	}
	@Override
	public String toString() {
		return "GoogleEventItem [id=" + id + ", status=" + status + ", summary=" + summary + ", description="
				+ description + ", location=" + location + ", recurringEventId=" + recurringEventId + ", start=" + start
				+ ", end=" + end + ", attendees=" + attendees + "]";
	}
}
