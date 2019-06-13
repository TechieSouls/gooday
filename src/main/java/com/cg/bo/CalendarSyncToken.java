package com.cg.bo;

import java.util.Date;

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
@Table(name="calendar_sync_tokens")
public class CalendarSyncToken {

	public enum AccountType {Google,Outlook, Apple, Holiday}
	
	public CalendarSyncToken() {
		
	}
	
	public CalendarSyncToken(Long userId, AccountType accountType) {
		this.userId = userId;
		this.accountType = accountType;
	}
	
	public CalendarSyncToken(Long userId, AccountType accountType, String refreshToken) {
		this.userId = userId;
		this.accountType = accountType;
		this.refreshToken = refreshToken;
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="calendar_sync_token_id")
	private Long refreshTokenId;
	
	@Column(name="user_id")
	private Long userId;
	
	@Column(name="refresh_token")
	private String refreshToken;
	
	@Enumerated(EnumType.STRING)
	@Column(name="account_type")
	private AccountType accountType;
	
	@Column(name="email_id")
	private String emailId;
	
	@Column(name="subscription_id")
	private String subscriptionId;
	
	@Column(name="resource_id")
	private String resourceId;
	
	@Column(name="sub_expiry_date")
	private Date subExpiryDate;
	
	@ManyToOne
	@JoinColumn(name="user_id",updatable=false,insertable=false)
	private User user;
	

	public Long getRefreshTokenId() {
		return refreshTokenId;
	}

	public void setRefreshTokenId(Long refreshTokenId) {
		this.refreshTokenId = refreshTokenId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public AccountType getAccountType() {
		return accountType;
	}

	public void setAccountType(AccountType accountType) {
		this.accountType = accountType;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getSubscriptionId() {
		return subscriptionId;
	}

	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

	public Date getSubExpiryDate() {
		return subExpiryDate;
	}

	public void setSubExpiryDate(Date subExpiryDate) {
		this.subExpiryDate = subExpiryDate;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	
}
