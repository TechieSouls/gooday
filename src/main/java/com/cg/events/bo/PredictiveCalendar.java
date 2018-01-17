package com.cg.events.bo;

public class PredictiveCalendar {

	private Long date;
	private Integer totalFriends;
	private Integer attendingFriends;
	private Integer predictivePercentage;
	public Long getDate() {
		return date;
	}
	public void setDate(Long date) {
		this.date = date;
	}
	public Integer getTotalFriends() {
		return totalFriends;
	}
	public void setTotalFriends(Integer totalFriends) {
		this.totalFriends = totalFriends;
	}
	public Integer getAttendingFriends() {
		return attendingFriends;
	}
	public void setAttendingFriends(Integer attendingFriends) {
		this.attendingFriends = attendingFriends;
	}
	public Integer getPredictivePercentage() {
		return predictivePercentage;
	}
	public void setPredictivePercentage(Integer predictivePercentage) {
		this.predictivePercentage = predictivePercentage;
	}
	@Override
	public String toString() {
		return "PredictiveCalendar [date=" + date + ", totalFriends="
				+ totalFriends + ", attendingFriends=" + attendingFriends
				+ ", predictivePercentage=" + predictivePercentage + "]";
	}
}
