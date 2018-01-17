package com.cg.bo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="alarms")
public class Alarm extends CgGeneral {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="alarm_id")
	private Long alarmId;
	
	@Column(name="label")
	private String label;
	
	@Column(name="sound")
	private String sound;
	
	@Column(name="hour")
	private Integer hour;
	
	@Column(name="minute")
	private Integer minute;

	@Column(name="recurrence")
	private String recurrence;
	
	@Column(name="user_id")
	private Long userId;
	
	@Column(name="alarm_on")
	private Boolean alarmOn = true;

	public Long getAlarmId() {
		return alarmId;
	}

	public void setAlarmId(Long alarmId) {
		this.alarmId = alarmId;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getSound() {
		return sound;
	}

	public void setSound(String sound) {
		this.sound = sound;
	}

	public Integer getHour() {
		return hour;
	}

	public void setHour(Integer hour) {
		this.hour = hour;
	}

	public Integer getMinute() {
		return minute;
	}

	public void setMinute(Integer minute) {
		this.minute = minute;
	}

	public String getRecurrence() {
		return recurrence;
	}

	public void setRecurrence(String recurrence) {
		this.recurrence = recurrence;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Boolean getAlarmOn() {
		return alarmOn;
	}

	public void setAlarmOn(Boolean alarmOn) {
		this.alarmOn = alarmOn;
	}
}
