package com.cg.events.bo;

import java.util.Map;

public class FacebookEventItem {
	
	private String id;
	private String name;
	private String description;
	private String start_time;
	private String end_time;
	private String rsvp_static;
	private String timezone;
	private Map<String,Object> place;
	private Map<String,Object> attending;
	private Map<String,Object> maybe;
	private Map<String,Object> declined;
	private Map<String,Object> picture;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getStart_time() {
		return start_time;
	}
	public void setStart_time(String start_time) {
		this.start_time = start_time;
	}
	public String getEnd_time() {
		return end_time;
	}
	public void setEnd_time(String end_time) {
		this.end_time = end_time;
	}
	public String getRsvp_static() {
		return rsvp_static;
	}
	public void setRsvp_static(String rsvp_static) {
		this.rsvp_static = rsvp_static;
	}
	public String getTimezone() {
		return timezone;
	}
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}
	public Map<String, Object> getPlace() {
		return place;
	}
	public void setPlace(Map<String, Object> place) {
		this.place = place;
	}
	public Map<String, Object> getAttending() {
		return attending;
	}
	public void setAttending(Map<String, Object> attending) {
		this.attending = attending;
	}
	public Map<String, Object> getMaybe() {
		return maybe;
	}
	public void setMaybe(Map<String, Object> maybe) {
		this.maybe = maybe;
	}
	public Map<String, Object> getDeclined() {
		return declined;
	}
	public void setDeclined(Map<String, Object> declined) {
		this.declined = declined;
	}
	public Map<String, Object> getPicture() {
		return picture;
	}
	public void setPicture(Map<String, Object> picture) {
		this.picture = picture;
	}
}
