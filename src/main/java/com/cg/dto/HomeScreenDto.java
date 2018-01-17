package com.cg.dto;

import java.util.ArrayList;
import java.util.List;

import com.cg.bo.Member;
import com.cg.events.bo.EventMember;

public class HomeScreenDto {

	private Long id;
	private String title;
	private String location;
	private String description;
	private Long createdById;
	private String source;
	private String scheduleAs;
	private String picture;
	private Long startTime;
	private Long endTime;
	private String type;
	private List<Member> members = new ArrayList<>();
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Long getCreatedById() {
		return createdById;
	}
	public void setCreatedById(Long createdById) {
		this.createdById = createdById;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getScheduleAs() {
		return scheduleAs;
	}
	public void setScheduleAs(String scheduleAs) {
		this.scheduleAs = scheduleAs;
	}
	public String getPicture() {
		return picture;
	}
	public void setPicture(String picture) {
		this.picture = picture;
	}
	public Long getStartTime() {
		return startTime;
	}
	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}
	public Long getEndTime() {
		return endTime;
	}
	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}
	
	public List<Member> getMembers() {
		return members;
	}
	public void setMembers(List<Member> members) {
		this.members = members;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	@Override
	public String toString() {
		return "HomeScreenDto [id=" + id + ", title=" + title + ", location="
				+ location + ", description=" + description + ", createdById="
				+ createdById + ", source=" + source + ", scheduleAs="
				+ scheduleAs + ", picture=" + picture + ", startTime="
				+ startTime + ", endTime=" + endTime + ", members=" + members
				+ "]";
	}
}
