package com.cg.reminders.bo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.cg.bo.CgGeneral;
import com.cg.user.bo.User;


@Entity
@Table(name="reminders")
public class Reminder extends CgGeneral{
	
	public enum ReminderStatus {Start,Finish};
	
	@Id
	@GeneratedValue (strategy=GenerationType.AUTO)
	@Column(name="reminder_id")
	private Long reminderId;
	
	@Column(name="title")	
	private String title;
	
	@Column(name="category")
	private String category;

	@Column(name="reminder_time")	
	private Date reminderTime;
	
	@Column(name="location")	
	private String location;
	
	@Column(name="created_by_id")	
	private Long createdById;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="created_by_id",insertable=false,updatable=false)
	private User from;
	
	@Column(name="status")
	private String status = ReminderStatus.Start.toString();
	
	@OneToMany(cascade = CascadeType.ALL,fetch=FetchType.EAGER,orphanRemoval=true)
	@JoinColumn(name="reminder_id")
	private List<ReminderMember> reminderMembers = new ArrayList<>();
	
	public Long getReminderId() {
		return reminderId;
	}

	public void setReminderId(Long reminderId) {
		this.reminderId = reminderId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Date getReminderTime() {
		return reminderTime;
	}

	public void setReminderTime(Date reminderTime) {
		this.reminderTime = reminderTime;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Long getCreatedById() {
		return createdById;
	}

	public void setCreatedById(Long createdById) {
		this.createdById = createdById;
	}

	public List<ReminderMember> getReminderMembers() {
		return reminderMembers;
	}

	public void setReminderMembers(List<ReminderMember> reminderMembers) {
		this.reminderMembers = reminderMembers;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public User getFrom() {
		return from;
	}

	public void setFrom(User from) {
		this.from = from;
	}
}
