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
	@Column(name="member_id")
	private Long memberId;
	
	@ApiModelProperty(required=true)
	@Column(name="member_status")
	private String memberStatus;
	
	@OneToOne(cascade = CascadeType.ALL,targetEntity=User.class,fetch=FetchType.EAGER)
	@JoinColumn(name="member_id",insertable=false,updatable=false)
	private User member;
	
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
	public Long getMemberId() {
		return memberId;
	}
	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}
	public String getMemberStatus() {
		return memberStatus;
	}
	public void setMemberStatus(String memberStatus) {
		this.memberStatus = memberStatus;
	}
	public User getMember() {
		return member;
	}
	public void setMember(User member) {
		this.member = member;
	}
	
}
