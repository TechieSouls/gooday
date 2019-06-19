package com.cg.user.bo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.cg.bo.CgGeneral;

@Entity
@Table(name="user_contacts")
public class UserContact extends CgGeneral {

	public enum CenesMember {no,yes};
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="user_contact_id")
	private Long userContactId;
	
	@Column(name="phone")
	private String phone;
	
	@Column(name="uc_user_id")
	private Long userId;
	
	@Column(name="name")
	private String name;
	
	@Column(name="cenes_name")
	private String cenesName;
	
	@Column(name="friend_id")
	private Long friendId;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name="cenes_member")
	private CenesMember cenesMember = CenesMember.no;

	@ManyToOne
	@JoinColumn(name="friend_id",insertable=false, updatable=false)
	private User user;
	
	public Long getUserContactId() {
		return userContactId;
	}

	public void setUserContactId(Long userContactId) {
		this.userContactId = userContactId;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public CenesMember getCenesMember() {
		return cenesMember;
	}

	public void setCenesMember(CenesMember cenesMember) {
		this.cenesMember = cenesMember;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getFriendId() {
		return friendId;
	}

	public void setFriendId(Long friendId) {
		this.friendId = friendId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getCenesName() {
		return cenesName;
	}

	public void setCenesName(String cenesName) {
		this.cenesName = cenesName;
	}
}
