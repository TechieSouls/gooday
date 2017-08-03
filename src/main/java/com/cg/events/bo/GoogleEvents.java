package com.cg.events.bo;

import java.util.List;

public class GoogleEvents {
	
	private String timeZone;
	private List<GoogleEventItem> items;
	private int errorCode;
	private String errorDetail;
	
	public String getTimeZone() {
		return timeZone;
	}
	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
	public List<GoogleEventItem> getItems() {
		return items;
	}
	public void setItems(List<GoogleEventItem> items) {
		this.items = items;
	}
	public int getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	public String getErrorDetail() {
		return errorDetail;
	}
	public void setErrorDetail(String errorDetail) {
		this.errorDetail = errorDetail;
	}
}
