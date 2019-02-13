package com.cg.events.bo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.authy.api.User;
import com.cg.bo.CgGeneral;
import com.cg.events.bo.Event.EventProcessedStatus;

import io.swagger.annotations.ApiModelProperty;

@Entity
@Table(name="event_members")
public class EventMember extends CgGeneral {
	
	public enum MemberStatus{Going,Maybe,NotGoing};
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="event_member_id")
	@ApiModelProperty(hidden=true,readOnly=true)
	private Long eventMemberId;
	
	@ApiModelProperty(required=true)
	@Column(name="event_id")
	private Long eventId;
	
	@ApiModelProperty(required=true)
	@Column(name="source")
	private String source;
	
	@ApiModelProperty(required=true)
	@Column(name="source_email")
	private String sourceEmail;
	
	@ApiModelProperty(required=true)
	@Column(name="source_id")
	private String sourceId;
	
	@ApiModelProperty(required=true)
	@Column(name="name")
	private String name;
	
	@ApiModelProperty(required=true)
	@Column(name="picture")
	private String picture;
	
	@ApiModelProperty(required=true)
	@Column(name="status")
	private String status;
	
	@Column(name="user_id")
	private Long userId;
	
	@Column(name="processed")
	private Integer processed = EventProcessedStatus.Processed.ordinal();	
	
	@ManyToOne
	@JoinColumn(name="user_id")
	private User user;
	
	@Transient
	private Boolean owner = false;
	
	public Long getEventMemberId() {
		return eventMemberId;
	}
	public void setEventMemberId(Long eventMemberId) {
		this.eventMemberId = eventMemberId;
	}
	public Long getEventId() {
		return eventId;
	}
	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getSourceId() {
		return sourceId;
	}
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPicture() {
		return picture;
	}
	public void setPicture(String picture) {
		this.picture = picture;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getSourceEmail() {
		return sourceEmail;
	}
	public void setSourceEmail(String sourceEmail) {
		this.sourceEmail = sourceEmail;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Boolean getOwner() {
		return owner;
	}
	public void setOwner(Boolean owner) {
		this.owner = owner;
	}
	public Integer getProcessed() {
		return processed;
	}
	public void setProcessed(Integer processed) {
		this.processed = processed;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
}
