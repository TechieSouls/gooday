package com.cg.events.bo;

import java.util.Map;

public class OutlookEventAttendees {
	
	private String type;
	private Map<String, String> status;
	private Map<String, String> emailAddress;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Map<String, String> getStatus() {
		return status;
	}
	public void setStatus(Map<String, String> status) {
		this.status = status;
	}
	public Map<String, String> getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(Map<String, String> emailAddress) {
		this.emailAddress = emailAddress;
	}
}
