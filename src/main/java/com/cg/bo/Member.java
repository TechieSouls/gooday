package com.cg.bo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.cg.user.bo.User;

@Entity
@Table(name="members")
public class Member {

	public enum MemberType {Event,Reminder,Gathering,Diary};
	public enum MemberStatus {Accept,Pending,Decline};
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="member_id")
	private Long memberId;
	
	@Column(name="name")
	private String name;
	
	@Column(name="picture")
	private String picture;
	
	@Column(name="type")
	private String type;
	
	@Column(name="type_id")
	private Long typeId;

	@Column(name="user_id")
	private Long userId;
	
	@Column(name="status")
	private String status;

	@Transient
	private User user;

	@Transient
	private Boolean owner = false;
	
	public Long getMemberId() {
		return memberId;
	}

	public void setMemberId(Long memberId) {
		this.memberId = memberId;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Long getTypeId() {
		return typeId;
	}

	public void setTypeId(Long typeId) {
		this.typeId = typeId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Boolean getOwner() {
		return owner;
	}

	public void setOwner(Boolean owner) {
		this.owner = owner;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
}
