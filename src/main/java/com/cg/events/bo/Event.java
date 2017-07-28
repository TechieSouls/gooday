package com.cg.events.bo;

import io.swagger.annotations.ApiModelProperty;

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

import com.cg.bo.CgGeneral;

@Entity
@Table(name="events")
public class Event extends CgGeneral {
	
	public enum EventType{Sport,Cafe,Entertainment,Travel,Birthday,Food,Seasonal};
	public enum EventSource{Cenes,Facebook,Google,Outlook}
	
	@Id
	@GeneratedValue (strategy=GenerationType.AUTO)
	@Column(name="event_id")
	@ApiModelProperty(hidden=true,readOnly=true)
	private Long eventId;
	
	@ApiModelProperty(required=true)
	@Column(unique=false,nullable=true)
	private String title;
	
	@ApiModelProperty(required=true)
	@Column(name="type")
	private String type;

	@ApiModelProperty(required=true)
	@Column(nullable=true)
	private String location;

	@ApiModelProperty(required=true)
	@Column(nullable=true)
	private String decription;

	@ApiModelProperty(required=true)
	@Column(name="created_by_id")
	private Long createdById;
	
	@ApiModelProperty(required=true)
	@Column(name="source")
	private String source;
	
	@ApiModelProperty(required=true)
	@Column(name="source_event_id")
	private String sourceEventId;
	
	@ApiModelProperty(required=true)
	@Column(name="source_user_id")
	private String sourceUserId;
	
	@ApiModelProperty(required=true)
	@Column(name="timezone")
	private String timezone;
	
	@OneToMany(cascade = CascadeType.ALL,fetch=FetchType.EAGER)
	@JoinColumn(name="event_id")
	private List<EventMember> eventMembers;
	
	@ApiModelProperty(required=true)
	@Column(name="start_time")
	private Date startTime;
	
	@ApiModelProperty(required=true)
	@Column(name="end_time")
	private Date endTime;
	
	public Long getEventId() {
		return eventId;
	}
	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getDecription() {
		return decription;
	}
	public void setDecription(String decription) {
		this.decription = decription;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Long getCreatedById() {
		return createdById;
	}
	public void setCreatedById(Long createdById) {
		this.createdById = createdById;
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
	public List<EventMember> getEventMembers() {
		return eventMembers;
	}
	public void setEventMembers(List<EventMember> eventMembers) {
		this.eventMembers = eventMembers;
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
	public String getSourceUserId() {
		return sourceUserId;
	}
	public void setSourceUserId(String sourceUserId) {
		this.sourceUserId = sourceUserId;
	}
	public String getTimezone() {
		return timezone;
	}
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}
	
}
