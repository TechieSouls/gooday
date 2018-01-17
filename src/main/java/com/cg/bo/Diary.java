package com.cg.bo;

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
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;

@Entity
@Table(name="diaries")
public class Diary extends CgGeneral {

	public enum DiarySource{Diary,Gathering,Reminder}; 
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="diary_id")
	private Long diaryId;
	
	@Column(name="title")
	private String title;
	
	@Column(name="location")
	private String location;
	
	@Column(name="pictures")
	private String pictures;
	
	@Column(name="detail")
	private String detail;
	
	@Column(name="diary_time")
	private Date diaryTime = new Date();
	
	@Column(name="created_by_id")
	private Long createdById;
	
	@OneToMany(cascade = CascadeType.ALL,fetch=FetchType.EAGER)
	@JoinColumn(name="type_id")
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	private List<Member> members = new ArrayList<>();

	
	@Column(name="source")
	private String source = DiarySource.Diary.toString();
	
	public Long getDiaryId() {
		return diaryId;
	}

	public void setDiaryId(Long diaryId) {
		this.diaryId = diaryId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getPictures() {
		return pictures;
	}

	public void setPictures(String pictures) {
		this.pictures = pictures;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public List<Member> getMembers() {
		return members;
	}

	public void setMembers(List<Member> members) {
		this.members = members;
	}

	public Long getCreatedById() {
		return createdById;
	}

	public void setCreatedById(Long createdById) {
		this.createdById = createdById;
	}

	public Date getDiaryTime() {
		return diaryTime;
	}

	public void setDiaryTime(Date diaryTime) {
		this.diaryTime = diaryTime;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
}
