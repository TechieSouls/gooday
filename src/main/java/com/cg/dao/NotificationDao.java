package com.cg.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.cg.bo.Notification;
import com.cg.events.bo.Event;
import com.cg.events.bo.EventMember;
import com.cg.mappers.NotificationDataMapper;

@Service
public class NotificationDao {

	@Autowired
	EventServiceDao eventServiceDao;
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	public int findTotalNotificationCountsByRecepientId(Long recepientId) {
		
		String query = "select count(*) from notifications where recepient_id = "+recepientId+"";
		
		int counts = jdbcTemplate.queryForInt(query);
		
		return counts;
	}
	
	public List<Notification> findPageableNotificationsByUserId(Long recepientId, int pageNumber, int offSet) {
		String query = "select * from notifications noti JOIN users us on noti.sender_id = us.user_id "
				+ "and noti.recepient_id = "+recepientId+"  order by read_status desc,noti.created_at desc limit "+pageNumber+","+offSet+" ";
		System.out.println(query);
		
		List<Notification> notifications = jdbcTemplate.query(query, new NotificationDataMapper());
		
		if (notifications != null && notifications.size() > 0) {
			
			//Populating Events in Notifications
			List<Event> events = eventServiceDao.findEventsByNotifications(notifications, recepientId);
			Map<Long, Event> eventMap = new HashMap<>();
			if (events != null && events.size() > 0) {
				
				for (Event event: events) {
					
					
					Event eventTemp = null;
					if (event.getEventId() != null && eventMap.containsKey(event.getEventId())) {
						eventTemp = eventMap.get(event.getEventId());
						
						List<EventMember> members = eventTemp.getEventMembers();
						if (members == null) {
							members = new ArrayList<>();
						} else {
							boolean memberExists = false;
							for (EventMember mem: members) {
								
								if (mem.getEventMemberId().equals(event.getEventMembers().get(0).getEventMemberId())) {
									memberExists = true;
									break;
								}
							}
							if (memberExists == true) {
								continue;
							}
						}
						members.add(event.getEventMembers().get(0));
						eventTemp.setEventMembers(members);
						
					} else {
						eventTemp = event;
					}
					eventMap.put(event.getEventId(), eventTemp);
				}
			}
			
			for (Notification notification: notifications) {
				if (eventMap.containsKey(notification.getNotificationTypeId()) && notification.getType().equals(Notification.NotificationType.Gathering)) {
					notification.setEvent(eventMap.get(notification.getNotificationTypeId()));
				}
			}
		}
		
		return notifications;
	}
}
