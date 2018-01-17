package com.cg.bo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="notifications")
public class Notification extends CgGeneral{
	
	public enum NotificationType {Event,Gathering,Reminder};
	public enum NotificationTypeStatus {New,Old}
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="notification_id")
	private Long notificationId;
	
	@Column(name="sender_id")
	private Long senderId;

	@Column(name="sender_picture")
	private String senderPicture;

	@Column(name="sender")
	private String sender;

	@Column(name="message")
	private String message; 
	
	@Column(name="title")
	private String title;
	
	@Column(name="recepient_id")
	private Long recepientId;
	
	@Column(name="notification_type_id")
	private Long notificationTypeId;
	
	@Enumerated(EnumType.STRING)
	@Column(name="notification_type_status")
	private NotificationTypeStatus notificationTypeStatus = NotificationTypeStatus.New;
	
	@Column(name="type")
	@Enumerated(EnumType.STRING)
	private NotificationType type;
	
	public Long getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(Long notificationId) {
		this.notificationId = notificationId;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Long getRecepientId() {
		return recepientId;
	}

	public void setRecepientId(Long recepientId) {
		this.recepientId = recepientId;
	}

	public Long getNotificationTypeId() {
		return notificationTypeId;
	}

	public void setNotificationTypeId(Long notificationTypeId) {
		this.notificationTypeId = notificationTypeId;
	}

	public NotificationType getType() {
		return type;
	}

	public void setType(NotificationType type) {
		this.type = type;
	}

	public Long getSenderId() {
		return senderId;
	}

	public void setSenderId(Long senderId) {
		this.senderId = senderId;
	}

	public String getSenderPicture() {
		return senderPicture;
	}

	public void setSenderPicture(String senderPicture) {
		this.senderPicture = senderPicture;
	}

	public NotificationTypeStatus getNotificationTypeStatus() {
		return notificationTypeStatus;
	}

	public void setNotificationTypeStatus(
			NotificationTypeStatus notificationTypeStatus) {
		this.notificationTypeStatus = notificationTypeStatus;
	}
}
