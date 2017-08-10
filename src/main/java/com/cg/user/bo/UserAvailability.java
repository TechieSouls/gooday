package com.cg.user.bo;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.primefaces.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@Entity
@Table(name="users_availability")
@JsonNaming(LowerCaseWithUnderscoresStrategy.class)
@JsonInclude(Include.NON_NULL)
public class UserAvailability {

	@Id
	@GeneratedValue (strategy=GenerationType.AUTO)
	@Column(name="user_availability_id")
	private Long userAvailabilityId;
	
	@Column(name="user_id")
	private Long userId;

	@Column(name="metadata")
	private String metadata;
	
	@Transient
	private Map<String, String> weekDetails;
	
	@Column(name="start_time")
	private String startTime;
	
	@Column(name="end_time")
	private String endTime;
	
	@Column(name="do_not_disturb")
	private Boolean doNotDisturb;
	
	public Long getUserAvailabilityId() {
		return userAvailabilityId;
	}
	public void setUserAvailabilityId(Long userAvailabilityId) {
		this.userAvailabilityId = userAvailabilityId;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Map<String, String> getWeekDetails() {
		return weekDetails;
	}
	public void setWeekDetails(Map<String, String> weekDetails) {
		this.weekDetails = weekDetails;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public String getMetadata() {
		return metadata;
	}
	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}
	public Boolean getDoNotDisturb() {
		return doNotDisturb;
	}
	public void setDoNotDisturb(Boolean doNotDisturb) {
		this.doNotDisturb = doNotDisturb;
	}
	/**
	 * load json into map
	 */
	public void loadWeeklyProperties() {
		if(this.metadata != null){
	        try{
	        	JSONObject weeklyJson = new JSONObject(metadata);	
	        	String validatedPropertiesJsonString =new ObjectMapper().writeValueAsString(weeklyJson);
		        this.weekDetails = new ObjectMapper().readValue(validatedPropertiesJsonString, HashMap.class);
	        }
	        catch(Exception ex){
	        	System.out.println(ex);
	        }
		} 	
	}
	
	public void loadMetadata() {
		JSONObject obj = new JSONObject();
		try{
			this.metadata = new ObjectMapper().writeValueAsString(weekDetails);
		}
		catch(Exception ex){
			System.out.println(ex);
		}
 	}
}
