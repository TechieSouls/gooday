package com.cg.bo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="user_stats")
public class UserStat {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="user_stat_id")
	private Long userStatId;
	
	@Column(name="cenes_member_counts")
	private long cenesMemberCounts;
	
	@Column(name="events_hosted_counts")
	private long eventsHostedCounts;
	
	@Column(name="events_attended_counts")
	private long eventsAttendedCounts;
	
	@Column(name="user_id")
	private Long userId;

	public Long getUserStatId() {
		return userStatId;
	}

	public void setUserStatId(Long userStatId) {
		this.userStatId = userStatId;
	}

	public Long getCenesMemberCounts() {
		return cenesMemberCounts;
	}

	public void setCenesMemberCounts(Long cenesMemberCounts) {
		this.cenesMemberCounts = cenesMemberCounts;
	}

	public Long getEventsHostedCounts() {
		return eventsHostedCounts;
	}

	public void setEventsHostedCounts(Long eventsHostedCounts) {
		this.eventsHostedCounts = eventsHostedCounts;
	}

	public Long getEventsAttendedCounts() {
		return eventsAttendedCounts;
	}

	public void setEventsAttendedCounts(Long eventsAttendedCounts) {
		this.eventsAttendedCounts = eventsAttendedCounts;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
}
