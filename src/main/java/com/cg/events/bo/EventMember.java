package com.cg.events.bo;

import io.swagger.annotations.ApiModelProperty;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.cg.bo.CgGeneral;
import com.cg.user.bo.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
}
