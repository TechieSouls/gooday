package com.cg.events.bo;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="event_time_slots")
public class EventTimeSlot {

	public enum TimeSlotSource {
		Facebook,
		Google,
		Outlook,
		Gathering,
		MeTime
	}
	public enum TimeSlotStatus {
		Free,
		Booked
	}
	
	@Id
	@GeneratedValue (strategy=GenerationType.AUTO)
	@Column(name="event_time_slot_id")
	private Long eventTimeSlotId;
	
	@Column(name="start_time")
	private Long startTime;
	
	@Column(name="end_time")
	private Long endTime;

	@Column(name="event_date")
	private Date eventDate;

	@Column(name="event_start_time")
	private Date eventStartTime;
	
	@Column(name="user_id")
	private Long userId;
	
	@Column(name="event_id")
	private Long eventId;
	
	@Column(name="status")
	private String status;
	
	@Column(name="schedule_as")
	private String scheduleAs;
	
	@Column(name="source")
	private String source;
	
	@Column(name="recurring_event_id")
	private Long recurringEventId;
	
	public Long getEventTimeSlotId() {
		return eventTimeSlotId;
	}
	public void setEventTimeSlotId(Long eventTimeSlotId) {
		this.eventTimeSlotId = eventTimeSlotId;
	}
	public Long getStartTime() {
		return startTime;
	}
	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}
	public Long getEndTime() {
		return endTime;
	}
	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}
	public Date getEventDate() {
		return eventDate;
	}
	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Long getEventId() {
		return eventId;
	}
	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getScheduleAs() {
		return scheduleAs;
	}
	public void setScheduleAs(String scheduleAs) {
		this.scheduleAs = scheduleAs;
	}
	public Date getEventStartTime() {
		return eventStartTime;
	}
	public void setEventStartTime(Date eventStartTime) {
		this.eventStartTime = eventStartTime;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public Long getRecurringEventId() {
		return recurringEventId;
	}
	public void setRecurringEventId(Long recurringEventId) {
		this.recurringEventId = recurringEventId;
	}
	@Override
	public String toString() {
		return "EventTimeSlot [eventTimeSlotId=" + eventTimeSlotId + ", startTime=" + startTime + ", endTime=" + endTime
				+ ", eventDate=" + eventDate + ", userId=" + userId + ", eventId=" + eventId + ", status=" + status
				+ "]";
	}
}
