package com.cg.events.bo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;

@Entity
@Table(name="recurring_events")
public class RecurringEvent {
	
	public enum RecurringEventStatus{Started,Cancelled}
	public enum RecurringEventProcessStatus{unprocessed,processed}
	
	@Id
	@GeneratedValue (strategy=GenerationType.AUTO)
	@Column(name="recurring_event_id")
	private Long recurringEventId;
	
	@Column(name="title")
	private String title;
	
	@Column(name="description")
	private String description;
	
	@Column(name="start_time")
	private Date startTime;
	
	@Column(name="end_time")
	private Date endTime;
	
	@Column(name="created_by_id")
	private Long createdById;

	@Column(name="timezone")
	private String timezone;
	
	@Column(name="status")
	private String status;
	
	@Column(name="source")
	private String source;
	
	@Column(name="source_event_id")
	private String sourceEventId;
	
	@Column(name="creation_timestamp")
	private Date creationTimestamp;

	@Column(name="update_timestamp")
	private Date updateTimestamp;
	
	@Column(name="processed")
	private Integer processed;
	
	@OneToMany(cascade = CascadeType.ALL,fetch=FetchType.EAGER)
	@JoinColumn(name="recurring_event_id")
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	private List<RecurringPattern> recurringPatterns = new ArrayList<>();
	
	public Long getRecurringEventId() {
		return recurringEventId;
	}

	public void setRecurringEventId(Long recurringEventId) {
		this.recurringEventId = recurringEventId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Long getCreatedById() {
		return createdById;
	}

	public void setCreatedById(Long createdById) {
		this.createdById = createdById;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getCreationTimestamp() {
		return creationTimestamp;
	}

	public void setCreationTimestamp(Date creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}

	public Date getUpdateTimestamp() {
		return updateTimestamp;
	}

	public void setUpdateTimestamp(Date updateTimestamp) {
		this.updateTimestamp = updateTimestamp;
	}

	public Integer getProcessed() {
		return processed;
	}

	public void setProcessed(Integer processed) {
		this.processed = processed;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSourceEventId() {
		return sourceEventId;
	}

	public void setSourceEventId(String sourceEventId) {
		this.sourceEventId = sourceEventId;
	}

	public List<RecurringPattern> getRecurringPatterns() {
		return recurringPatterns;
	}

	public void setRecurringPatterns(List<RecurringPattern> recurringPatterns) {
		this.recurringPatterns = recurringPatterns;
	}
}
