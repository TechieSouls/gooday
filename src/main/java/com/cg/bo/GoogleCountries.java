package com.cg.bo;

import java.util.List;

public class GoogleCountries{

	private Boolean IsSuccess;

	private List<GoogleCountryItem> Response;
	//private int errorCode;
	//private String errorDetail;
	
	public Boolean getIsSuccess() {
		return IsSuccess;
	}
	public void setIsSuccess(Boolean isSuccess) {
		IsSuccess = isSuccess;
	}
	public List<GoogleCountryItem> getResponse() {
		return Response;
	}
	public void setResponse(List<GoogleCountryItem> response) {
		Response = response;
	}
	/*public int getErrorCode() {
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
	}*/
}
