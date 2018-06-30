package com.cg.bo;

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
@Table(name="refresh_tokens")
public class RefreshToken {

	public enum AccountType {Google,Outlook}
	
	public RefreshToken(Long userId, AccountType accountType) {
		this.userId = userId;
		this.accountType = accountType;
	}
	
	public RefreshToken(Long userId, AccountType accountType, String refreshTokenString) {
		this.userId = userId;
		this.accountType = accountType;
		this.refreshTokenString = refreshTokenString;
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="refresh_token_id")
	private Long refreshTokenId;
	
	@Column(name="user_id")
	private Long userId;
	
	@Column(name="refresh_token_str")
	private String refreshTokenString;
	
	@Enumerated(EnumType.STRING)
	@Column(name="account_type")
	private AccountType accountType;
	
	@Column(name="email_id")
	private String emailId;
	
	@ManyToOne
	@JoinColumn(name="user_id")
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

	public String getRefreshTokenString() {
		return refreshTokenString;
	}

	public void setRefreshTokenString(String refreshTokenString) {
		this.refreshTokenString = refreshTokenString;
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
}
