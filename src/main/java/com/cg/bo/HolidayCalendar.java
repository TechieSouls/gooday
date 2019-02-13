package com.cg.bo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="holiday_calendars")
public class HolidayCalendar extends CgGeneral {

	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="holiday_calendar_id")
	private Long holidayCalendarId;
	
	@Column(name="country_name")
	private String countryName;
	
	@Column(name="countryCode")
	private String countryCode;
	
	@Column(name="country_calendar_id")
	private String countryCalendarId;
	
	@Column(name="user_id")
	private Long userId;

	public Long getHolidayCalendarId() {
		return holidayCalendarId;
	}

	public void setHolidayCalendarId(Long holidayCalendarId) {
		this.holidayCalendarId = holidayCalendarId;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getCountryCalendarId() {
		return countryCalendarId;
	}

	public void setCountryCalendarId(String countryCalendarId) {
		this.countryCalendarId = countryCalendarId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
}
