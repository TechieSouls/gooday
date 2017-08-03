package com.cg.events.bo;

import java.util.Map;

public class FacebookEventAttendees {
	
	private String id;
	private String name;
	private String rsvp_status;
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
	public String getRsvp_status() {
		return rsvp_status;
	}
	public void setRsvp_status(String rsvp_status) {
		this.rsvp_status = rsvp_status;
	}
	public Map<String, Object> getPicture() {
		return picture;
	}
	public void setPicture(Map<String, Object> picture) {
		this.picture = picture;
	}
}
