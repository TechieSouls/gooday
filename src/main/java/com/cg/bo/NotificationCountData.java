package com.cg.bo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="notification_count_data")
public class NotificationCountData {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="notification_count_data_id")
	private Long notificationCountDataId;
	
	@Column(name="badge_count")
	private int badgeCount;
	
	@Column(name="user_id")
	private Long userId;

	public Long getNotificationCountDataId() {
		return notificationCountDataId;
	}

	public void setNotificationCountDataId(Long notificationCountDataId) {
		this.notificationCountDataId = notificationCountDataId;
	}

	public int getBadgeCount() {
		return badgeCount;
	}

	public void setBadgeCount(int badgeCount) {
		this.badgeCount = badgeCount;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
}