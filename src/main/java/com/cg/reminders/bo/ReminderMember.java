package com.cg.reminders.bo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.cg.bo.CgGeneral;

@Entity
@Table(name="reminder_members")
public class ReminderMember extends CgGeneral {
		
		public enum ReminderMemberStatus{Accept,Pending,Declined};
		
		@Id
		@GeneratedValue(strategy=GenerationType.AUTO)
		@Column(name="reminder_member_id")
		private Long reminderMemberId;
		
		@Column(name="reminder_id")
		private Long reminderId;
		
		@Column(name="member_id")
		private Long memberId;
		
		@Column(name="name")
		private String name;
		
		@Column(name="picture")
		private String picture;
		
		@Column(name="status")
		private String status;

		public Long getReminderMemberId() {
			return reminderMemberId;
		}

		public void setReminderMemberId(Long reminderMemberId) {
			this.reminderMemberId = reminderMemberId;
		}

		public Long getReminderId() {
			return reminderId;
		}

		public void setReminderId(Long reminderId) {
			this.reminderId = reminderId;
		}

		public Long getMemberId() {
			return memberId;
		}

		public void setMemberId(Long memberId) {
			this.memberId = memberId;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getPicture() {
			return picture;
		}

		public void setPicture(String picture) {
			this.picture = picture;
		}
}
