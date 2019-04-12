package com.cg.mappers;

import java.sql.ResultSet;
import java.util.Date;

import com.cg.events.bo.Event;
import com.cg.events.bo.EventMember;
import com.cg.user.bo.User;
import com.cg.user.bo.UserContact;
import com.cg.utils.CenesUtils;

public class BaseMapper {

	public User populateUserData(ResultSet rs) {
		User user = new User();
		
		try {
			rs.findColumn("user_id");
			
			if (rs.getLong("user_id") == 0) {
				user = null;
				return user;
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			user = null;
			return user;
		}
		
		try {
			user.setUserId(rs.getLong("user_id"));
			user.setPhoto(rs.getString("photo"));
			
			if (rs.getString("name") != null) {
				user.setName(rs.getString("name"));
			} else if (rs.getString("nameuser") != null) {
				user.setName(rs.getString("nameuser"));
			}
			
			user.setPhone(rs.getString("phone"));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return user;
	}
	
	public UserContact populateUserContactData(ResultSet rs) {
		UserContact userContact = new UserContact();
		
		try {
			rs.findColumn("phonebookName");
			
			if (rs.getString("phonebookName") == null) {
				userContact = null;
				return userContact;
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			userContact = null;
			return userContact;
		}
		
		try {
						
			if (rs.getString("phonebookName") != null) {
				userContact.setName(rs.getString("phonebookName"));
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return userContact;
	}
	
	public Event populateEventBo(ResultSet rs) {
		Event event = new Event();
		
		try {
			int index = rs.findColumn("event_id");
			if (rs.getLong("event_id") == 0) {
				event = null;
				return event;
			}
		} catch (Exception e) {
			// TODO: handle exception
			//e.printStackTrace();
			event = null;
			return event;
		}
		
		try {
			event.setEventId(rs.getLong("event_id"));
			event.setCreatedById(rs.getLong("created_by_id"));
			event.setTitle(rs.getString("title"));
			event.setRecurringEventId(rs.getString("recurring_event_id"));
			event.setLocation(rs.getString("location"));
			event.setLatitude(rs.getString("latitude"));
			event.setLongitude(rs.getString("longitude"));
			event.setDescription(rs.getString("description"));
			event.setSource(rs.getString("source"));
			event.setSourceEventId(rs.getString("source_event_id"));
			event.setSourceUserId(rs.getString("source_user_id"));
			event.setScheduleAs(rs.getString("schedule_as"));
			event.setEventPicture(rs.getString("event_picture"));
			event.setThumbnail(rs.getString("thumbnail"));
			event.setStartTime(new Date(rs.getDate("start_time").getTime()));
			event.setEndTime(new Date(rs.getDate("end_time").getTime()));
			event.setTimezone(rs.getString("timezone"));
			event.setIsFullDay(rs.getBoolean("is_full_day"));
			event.setCreatedAt(rs.getDate("created_at"));

			//If its a full day event then we are sending the Date String as yyyyMMdd format
			if (event.getIsFullDay()) {
				event.setFullDayStartTime(CenesUtils.yyyyMMdd.format(event.getStartTime()));
			}

			event.setIsPredictiveOn(rs.getBoolean("is_predictive_on"));
			event.setPredictiveData(rs.getString("predictive_data"));
			event.setKey(rs.getString("private_key"));
			event.setExpired(rs.getBoolean("expired"));
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return event;
	}
	
	public EventMember populateEventMembers(ResultSet rs) {

		EventMember eventMember = new EventMember();

		try {
			int index = rs.findColumn("event_member_id");
		} catch (Exception e) {
			// TODO: handle exception
			// e.printStackTrace();
			eventMember = null;
			return eventMember;
		}

		try {

			eventMember.setEventMemberId(rs.getLong("event_member_id"));
			eventMember.setEventId(rs.getLong("event_id"));
			eventMember.setSource(rs.getString("source"));
			eventMember.setSourceEmail(rs.getString("source_email"));
			eventMember.setSourceEmail(rs.getString("source_email"));
			eventMember.setSourceId(rs.getString("source_id"));
			eventMember.setStatus(rs.getString("status"));
			eventMember.setUserId(rs.getLong("user_id"));
			eventMember.setProcessed(rs.getInt("processed"));
			eventMember.setUserContactId(rs.getLong("user_contact_id"));
			eventMember.setUser(populateUserData(rs));
			eventMember.setUserContact(populateUserContactData(rs));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return eventMember;

	}
}
