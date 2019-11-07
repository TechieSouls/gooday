package com.cg.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.springframework.jdbc.core.RowMapper;

import com.cg.bo.Notification;

public class NotificationDataMapper extends BaseMapper implements RowMapper<Notification> {
	
	public Notification mapRow(ResultSet rs, int rowNum) throws SQLException {

		Notification notification = new Notification();
		notification.setNotificationId(rs.getLong("notification_id"));
		String readStatus = rs.getString("read_status");
		if ("UnRead".equals(readStatus)) {
			notification.setReadStatus(Notification.NotificationReadStatus.UnRead);
		} else {
			notification.setReadStatus(Notification.NotificationReadStatus.Read);
		}
		notification.setSenderId(rs.getLong("sender_id"));
		notification.setMessage(rs.getString("message"));
		notification.setTitle(rs.getString("title"));
		notification.setRecepientId(rs.getLong("recepient_id"));
		
		String type = rs.getString("type");
		if ("Event".equals(type)) {
			notification.setType(Notification.NotificationType.Event);
		} else if ("Gathering".equals(type)) {
			notification.setType(Notification.NotificationType.Gathering);
		} else if ("Reminder".equals(type)) {
			notification.setType(Notification.NotificationType.Reminder);
		} else if ("Welcome".equals(type)) {
			notification.setType(Notification.NotificationType.Welcome);
		}
		
		
		String typeAction = rs.getString("notification_type_action");
		if ("Create".equals(typeAction)) {
			notification.setAction(Notification.NotificationTypeAction.Create);
		} else if ("AcceptDecline".equals(typeAction)) {
			notification.setAction(Notification.NotificationTypeAction.AcceptDecline);
		} else if ("AcceptDecline".equals(typeAction)) {
			notification.setAction(Notification.NotificationTypeAction.AcceptDecline);
		} else if ("Delete".equals(typeAction)) {
			notification.setAction(Notification.NotificationTypeAction.Delete);
		} else if ("Update".equals(typeAction)) {
			notification.setAction(Notification.NotificationTypeAction.Update);
		}
		
		notification.setNotificationTypeId(rs.getLong("notification_type_id"));
		notification.setUser(populateUserData(rs));
		notification.setCreatedAt(new Date(rs.getTimestamp("created_at").getTime()));		
		return notification;
	}
}
