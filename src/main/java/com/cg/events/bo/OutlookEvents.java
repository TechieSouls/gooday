package com.cg.events.bo;

import java.util.List;

public class OutlookEvents {
	private int errorCode;
	private String errorDetail;
	private List<OutlookEventItem> value;
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
	public List<OutlookEventItem> getValue() {
		return value;
	}
	public void setValue(List<OutlookEventItem> value) {
		this.value = value;
	}
}
