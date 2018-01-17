package com.cg.bo;

import io.swagger.annotations.ApiModelProperty;

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

import com.cg.user.bo.User;


@Entity
@Table(name="user_friends")
public class UserFriend extends  CgGeneral{
	
	public enum FriendSource {
		Cenes,
		Facebook,
		Google,
		Outlook
	}
	
	public enum UserStatus {
		Requested,
		Invitation,
		Accepted,
		Block
	}
	
	
	@Id
	@Column(name="user_friend_id")
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long userFriendId;

	@Column(name="user_id")
	Long userId;
	
	@ApiModelProperty(hidden = true)
	@ManyToOne(targetEntity = User.class)
	@JoinColumn(name = "user_id", insertable = false, updatable = false)
	User user;
	
	
	@Column(name="friend_id")
	Long friendId;
	
	@ApiModelProperty(hidden = true)
	@ManyToOne(targetEntity = User.class)
	@JoinColumn(name = "friend_id", insertable = false, updatable = false)
	User friend;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name="status")
	UserStatus status;

	@Column(name="source")
	private String source;
	
	@Column(name="source_id")
	private Long sourceId;
	
	public Long getUserFriendId() {
		return userFriendId;
	}

	public void setUserFriendId(Long userFriendId) {
		this.userFriendId = userFriendId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Long getFriendId() {
		return friendId;
	}

	public void setFriendId(Long friendId) {
		this.friendId = friendId;
	}

	public User getFriend() {
		return friend;
	}

	public void setFriend(User friend) {
		this.friend = friend;
	}

	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Long getSourceId() {
		return sourceId;
	}

	public void setSourceId(Long sourceId) {
		this.sourceId = sourceId;
	}
	
}
