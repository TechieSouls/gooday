package com.cg.user.bo;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.cg.bo.CgGeneral;
import com.cg.enums.CgEnums.AuthenticateType;

import io.swagger.annotations.ApiModelProperty;


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
	//@Column(unique=true,nullable=true)
	private String email;
	//@JsonIgnore
	@ApiModelProperty(required=true)
	@Column(nullable=true)
	private String password;
	
	
	@Column(name="facebook_id")
	private String facebookId;
	
	@Column(name="google_id")
	private String googleId;
	
	@Transient
	private AuthenticateType authType;
	
	
	@Column(name="facebook_auth_token")
	private String facebookAuthToken;
	
	@Column(name="google_auth_token")
	private String googleAuthToken;
	
	@Column(nullable=false)
	private String name;
	
	@Column(name="photo")
	private String photo;
	
	@Column(name="gender")
	private String gender;

	@Column(name="phone")
	private String phone;	
	
	@Column(name="reset_token")
	private String resetToken;
	
	@Column(name="reset_token_created_at")
	private Date resetTokenCreatedAt;
	
	@Column(name="country")
	private String country;
	
	@Column(name="birth_date")
	private Long birthDate;

	@Column(name="birth_day_str")
	private String birthDayStr;
	
	@Transient
	private Boolean isNew;
	
	public String getToken() {
		return token;
	}

	public String getFacebookId() {
		return facebookId;
	}


	public void setFacebookId(String facebookId) {
		this.facebookId = facebookId;
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

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public Boolean getIsNew() {
		return isNew;
	}

	public void setIsNew(Boolean isNew) {
		this.isNew = isNew;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getResetToken() {
		return resetToken;
	}

	public void setResetToken(String resetToken) {
		this.resetToken = resetToken;
	}

	public Date getResetTokenCreatedAt() {
		return resetTokenCreatedAt;
	}

	public void setResetTokenCreatedAt(Date resetTokenCreatedAt) {
		this.resetTokenCreatedAt = resetTokenCreatedAt;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Long getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Long birthDate) {
		this.birthDate = birthDate;
	}

	public String getBirthDayStr() {
		return birthDayStr;
	}

	public void setBirthDayStr(String birthDayStr) {
		this.birthDayStr = birthDayStr;
	}

	public String getGoogleId() {
		return googleId;
	}

	public void setGoogleId(String googleId) {
		this.googleId = googleId;
	}

	public String getGoogleAuthToken() {
		return googleAuthToken;
	}

	public void setGoogleAuthToken(String googleAuthToken) {
		this.googleAuthToken = googleAuthToken;
	}

	@Override
	public String toString() {
		return "User [userId=" + userId + ", username=" + username + ", email=" + email + ", password=" + password
				+ ", facebookId=" + facebookId + ", googleId=" + googleId + ", authType=" + authType
				+ ", facebookAuthToken=" + facebookAuthToken + ", googleAuthToken=" + googleAuthToken + ", name=" + name
				+ ", photo=" + photo + ", gender=" + gender + ", phone=" + phone + ", resetToken=" + resetToken
				+ ", resetTokenCreatedAt=" + resetTokenCreatedAt + ", country=" + country + ", birthDate=" + birthDate
				+ ", birthDayStr=" + birthDayStr + ", isNew=" + isNew + ", token=" + token + "]";
	}
	
}
