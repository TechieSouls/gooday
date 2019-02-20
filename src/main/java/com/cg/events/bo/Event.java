package com.cg.events.bo;

import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;

import com.cg.bo.CgGeneral;

@Entity
@Table(name="events")
public class Event extends CgGeneral {
	
	public enum ScheduleEventAs{Event,MeTime,Holiday,Gathering}
	public enum EventType{Sport,Cafe,Entertainment,Travel,Birthday,Food,Seasonal};
	public enum EventSource{Cenes,Facebook,Google,Outlook,Apple}
	public enum EventProcessedStatus{UnProcessed,Waiting,Processed}
	
	@Id
	@GeneratedValue (strategy=GenerationType.AUTO)
	@Column(name="event_id")
	@ApiModelProperty(hidden=true,readOnly=true)
	private Long eventId;
	
	@ApiModelProperty(required=true)
	@Column(unique=false,nullable=true)
	private String title;
	
	@ApiModelProperty(required=true)
	@Column(name="type")
	private String type;

	/**
	 * This is set to string to hold the recurring id of Google Events
	 * */
	@Column(name="recurring_event_id")
	private String recurringEventId;
	
	@ApiModelProperty(required=true)
	@Column(nullable=true)
	private String location;

	@Column(nullable=true)
	private String latitude;
	
	@Column(nullable=true)
	private String longitude;
	
	@ApiModelProperty(required=true)
	@Column(nullable=true)
	private String description;

	@ApiModelProperty(required=true)
	@Column(name="created_by_id")
	private Long createdById;
	
	@ApiModelProperty(required=true)
	@Column(name="source")
	private String source;
	
	@ApiModelProperty(required=true)
	@Column(name="source_event_id")
	private String sourceEventId;
	
	@ApiModelProperty(required=true)
	@Column(name="source_user_id")
	private String sourceUserId;
	
	@ApiModelProperty(required=true)
	@Column(name="timezone")
	private String timezone;

	@Column(name="schedule_as")
	private String scheduleAs;
	
	@Column(name="event_picture")
	private String eventPicture;
	
	@Column(name="thumbnail")
	private String thumbnail;
	
	@OneToMany(cascade = CascadeType.ALL,fetch=FetchType.EAGER,orphanRemoval=true )
	@JoinColumn(name="event_id")
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	private List<EventMember> eventMembers;
	
	@Column(name="start_time")
	private Date startTime;

	/***
	 * This is needed in case of holiday or full day events as the 
	 * dates from third party calendards does not contain hours or minutes.. 
	 * That is the date is only as 2019-02-12 
	 */
	@Transient
	private String fullDayStartTime;
	
	@Column(name="end_time")
	private Date endTime;
	
	@Column(name="is_predictive_on")
	private Boolean isPredictiveOn = false;
	
	@Column(name="is_full_day")
	private Boolean isFullDay = false;
	
	@Column(name="predictive_data",columnDefinition="TEXT")
	private String predictiveData;
	
	@Column(name="place_id")
	private String placeId;
	
	
	@Transient
	private Map<String,Object> predictiveDataForIos;
	
	@Column(name="processed")
	private Integer processed = EventProcessedStatus.UnProcessed.ordinal();
	
	public Long getEventId() {
		return eventId;
	}
	public void setEventId(Long eventId) {
		this.eventId = eventId;
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
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Long getCreatedById() {
		return createdById;
	}
	public void setCreatedById(Long createdById) {
		this.createdById = createdById;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public List<EventMember> getEventMembers() {
		return eventMembers;
	}
	public void setEventMembers(List<EventMember> eventMembers) {
		this.eventMembers = eventMembers;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getSourceEventId() {
		return sourceEventId;
	}
	public void setSourceEventId(String sourceEventId) {
		this.sourceEventId = sourceEventId;
	}
	public String getSourceUserId() {
		return sourceUserId;
	}
	public void setSourceUserId(String sourceUserId) {
		this.sourceUserId = sourceUserId;
	}
	public String getTimezone() {
		return timezone;
	}
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}
	public String getScheduleAs() {
		return scheduleAs;
	}
	public void setScheduleAs(String scheduleAs) {
		this.scheduleAs = scheduleAs;
	}
	public String getEventPicture() {
		return eventPicture;
	}
	public void setEventPicture(String eventPicture) {
		this.eventPicture = eventPicture;
	}
	public Integer getProcessed() {
		return processed;
	}
	public void setProcessed(Integer processed) {
		this.processed = processed;
	}
	public String getRecurringEventId() {
		return recurringEventId;
	}
	public void setRecurringEventId(String recurringEventId) {
		this.recurringEventId = recurringEventId;
	}
	public Boolean getIsPredictiveOn() {
		return isPredictiveOn;
	}
	public void setIsPredictiveOn(Boolean isPredictiveOn) {
		this.isPredictiveOn = isPredictiveOn;
	}
	public String getPredictiveData() {
		return predictiveData;
	}
	public void setPredictiveData(String predictiveData) {
		this.predictiveData = predictiveData;
	}
	
	public Map<String, Object> getPredictiveDataForIos() {
		return predictiveDataForIos;
	}
	public void setPredictiveDataForIos(Map<String, Object> predictiveDataForIos) {
		this.predictiveDataForIos = predictiveDataForIos;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	
	public Boolean getIsFullDay() {
		return isFullDay;
	}
	public void setIsFullDay(Boolean isFullDay) {
		this.isFullDay = isFullDay;
	}

	public String getThumbnail() {
		return thumbnail;
	}
	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}
	
	public String getPlaceId() {
		return placeId;
	}
	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}
	
	public String getFullDayStartTime() {
		return fullDayStartTime;
	}
	public void setFullDayStartTime(String fullDayStartTime) {
		this.fullDayStartTime = fullDayStartTime;
	}
	@Override
	public String toString() {
		return "Event [eventId=" + eventId + ", title=" + title + ", type=" + type + ", recurringEventId="
				+ recurringEventId + ", location=" + location + ", latitude=" + latitude + ", longitude=" + longitude
				+ ", description=" + description + ", createdById=" + createdById + ", source=" + source
				+ ", sourceEventId=" + sourceEventId + ", sourceUserId=" + sourceUserId + ", timezone=" + timezone
				+ ", scheduleAs=" + scheduleAs + ", eventPicture=" + eventPicture + ", eventMembers=" + eventMembers
				+ ", startTime=" + startTime + ", endTime=" + endTime + ", isPredictiveOn=" + isPredictiveOn
				+ ", isFullDay=" + isFullDay + ", predictiveData=" + predictiveData + ", predictiveDataForIos="
				+ predictiveDataForIos + ", processed=" + processed + "]";
	}
}
