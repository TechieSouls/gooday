package com.cg.bo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="gathering_previous_locations")
public class GatheringPreviousLocation extends CgGeneral {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="gathering_previous_location_id")
	private Long gatheringPreviousLocationId;
	
	@Column(name="photo")
	private String photo;
	
	@Column(name="location")
	private String location;
	
	@Column(name="latitude")
	private String latitude;
	
	@Column(name="longitude")
	private String longitude;
	
	@Column(name="event_id")
	private Long eventId;
	
	@Column(name="user_id")
	private Long userId;
	
	@Column(name="place_id")
	private String placeId;

	public Long getGatheringPreviousLocationId() {
		return gatheringPreviousLocationId;
	}

	public void setGatheringPreviousLocationId(Long gatheringPreviousLocationId) {
		this.gatheringPreviousLocationId = gatheringPreviousLocationId;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
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

	public String getPlaceId() {
		return placeId;
	}

	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}
}
