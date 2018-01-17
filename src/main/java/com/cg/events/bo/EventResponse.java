package com.cg.events.bo;

import java.util.List;

public class EventResponse {

	private int errorCode;
	private String errorDetail;
	private List<Event> data;
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
	public List<Event> getData() {
		return data;
	}
	public void setData(List<Event> data) {
		this.data = data;
	}
}
