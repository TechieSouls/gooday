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
	private String message;

	@ApiModelProperty(required=true)
	@Column(name="created_by_id")
	private Long createdById;
	
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
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
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
}
