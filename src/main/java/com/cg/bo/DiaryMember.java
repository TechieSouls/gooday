package com.cg.bo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="diary_members")
public class DiaryMember {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="diary_member_id")
	private Long diaryMemberId;
	
	@Column(name="name")
	private String name;
	
	@Column(name="photo")
	private String photo;
	
	@Column(name="user_id")
	private Long userId;
	
	@Column(name="diary_id")
	private Long diaryId;

	public Long getDiaryMemberId() {
		return diaryMemberId;
	}

	public void setDiaryMemberId(Long diaryMemberId) {
		this.diaryMemberId = diaryMemberId;
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

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getDiaryId() {
		return diaryId;
	}

	public void setDiaryId(Long diaryId) {
		this.diaryId = diaryId;
	}
}
