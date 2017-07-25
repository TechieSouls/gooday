package com.cg.user.bo;

import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.cg.bo.CgGeneral;
import com.cg.enums.CgEnums.AuthenticateType;


@Entity
@Table(name="users")
public class User extends CgGeneral{

	@Id
	@GeneratedValue (strategy=GenerationType.AUTO)
	@Column(name="user_id")
	@ApiModelProperty(hidden=true,readOnly=true)
	private Long userId;
	
	
	@ApiModelProperty(required=true)
	@Column(unique=true,nullable=true)
	private String username;
	
	@ApiModelProperty(required=true)
	@Column(unique=true,nullable=true)
	private String email;
	//@JsonIgnore
	@ApiModelProperty(required=true)
	@Column(nullable=true)
	private String password;
	
	
	@ApiModelProperty(required=true)
	@Column(name="facebook_id")
	private String facebookID;
	
	
	@Transient
	private AuthenticateType authType;
	
	
	@ApiModelProperty(required=true)
	@Column(name="facebook_auth_token")
	private String facebookAuthToken;
	
	@ApiModelProperty(required=true)
	@Column(nullable=false)
	private String name;

	
	public String getToken() {
		return token;
	}

	public String getFacebookID() {
		return facebookID;
	}

	public void setFacebookID(String facebookID) {
		this.facebookID = facebookID;
	}

	public AuthenticateType getAuthType() {
		return authType;
	}

	public void setAuthType(AuthenticateType authType) {
		this.authType = authType;
	}

	public String getFacebookAuthToken() {
		return facebookAuthToken;
	}

	public void setFacebookAuthToken(String facebookAuthToken) {
		this.facebookAuthToken = facebookAuthToken;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@ApiModelProperty(hidden=true)
	private String token;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	

}
