package com.cg.user.bo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.cg.bo.CgGeneral;

@Entity
@Table(name="user_devices")
public class UserDevice extends CgGeneral {
	
	@Id
	@GeneratedValue (strategy=GenerationType.AUTO)
	@Column(name="user_token_id")
	private Long userTokenId;
	
	@Column(name="device_token")
	private String deviceToken;
	
	@Column(name="device_type")
	private String deviceType;
	
	@Column(name="user_id")
	private Long userId;

	public Long getUserTokenId() {
		return userTokenId;
	}

	public void setUserTokenId(Long userTokenId) {
		this.userTokenId = userTokenId;
	}

	public String getDeviceToken() {
		return deviceToken;
	}

	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
}
