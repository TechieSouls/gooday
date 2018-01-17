package com.cg.events.bo;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="recurring_patterns")
public class RecurringPattern {

	@Id
	@GeneratedValue (strategy=GenerationType.AUTO)
	@Column(name="recurring_pattern_id")
	private Long recurringPatternId;
	
	@Column(name="day_of_week")
	private Integer dayOfWeek;
	
	@Column(name="week_of_month")
	private Integer weekOfMonth;
	
	@Column(name="day_of_month")
	private Integer dayOfMonth;
	
	@Column(name="monthOfYear")
	private Integer monthOfYear;
	
	@Column(name="recurring_event_id")
	private Long recurringEventId;
	
	@Transient
	private Long dayOfWeekTimestamp;

	/**
	 * This will hold the last date upto which slots has been generated.
	 * We will use it in job to generate futhrer time slots.
	 */
	@Column(name="slots_generated_upto")
	private Date slotsGeneratedUpto;
	
	public Integer getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(Integer dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public Integer getWeekOfMonth() {
		return weekOfMonth;
	}

	public void setWeekOfMonth(Integer weekOfMonth) {
		this.weekOfMonth = weekOfMonth;
	}

	public Integer getDayOfMonth() {
		return dayOfMonth;
	}

	public void setDayOfMonth(Integer dayOfMonth) {
		this.dayOfMonth = dayOfMonth;
	}

	public Integer getMonthOfYear() {
		return monthOfYear;
	}

	public void setMonthOfYear(Integer monthOfYear) {
		this.monthOfYear = monthOfYear;
	}

	public Long getRecurringPatternId() {
		return recurringPatternId;
	}

	public void setRecurringPatternId(Long recurringPatternId) {
		this.recurringPatternId = recurringPatternId;
	}

	public Long getRecurringEventId() {
		return recurringEventId;
	}

	public void setRecurringEventId(Long recurringEventId) {
		this.recurringEventId = recurringEventId;
	}

	public Date getSlotsGeneratedUpto() {
		return slotsGeneratedUpto;
	}

	public void setSlotsGeneratedUpto(Date slotsGeneratedUpto) {
		this.slotsGeneratedUpto = slotsGeneratedUpto;
	}

	public Long getDayOfWeekTimestamp() {
		return dayOfWeekTimestamp;
	}

	public void setDayOfWeekTimestamp(Long dayOfWeekTimestamp) {
		this.dayOfWeekTimestamp = dayOfWeekTimestamp;
	}
}
