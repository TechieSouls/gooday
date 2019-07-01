package com.cg.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.cg.bo.Notification;
import com.cg.dto.LocationDto;
import com.cg.events.bo.Event;
import com.cg.events.bo.EventMember;
import com.cg.events.bo.EventTimeSlot;
import com.cg.mappers.EventDataMapper;
import com.cg.user.bo.User;
import com.cg.user.bo.UserContact;
import com.cg.utils.CenesUtils;

@Service
public class EventServiceDao {

	@Autowired
	JdbcTemplate jdbcTemplate;

	/*public List<Map<String, Object>> getFreeTimeSlotsByDateAndUserId(String userIds,String startDate,String endDate) {

		String query = "SELECT DATE(event_date) as event_date,user_id,event_id,GROUP_CONCAT(start_time) as free_slots,status FROM event_time_slots WHERE "
				+ "event_date >= '"+startDate+"' and event_date <= '"+endDate+"' and "
				+ "user_id in ("+userIds+") and status = 'Free' GROUP BY user_id,DATE(event_date) order by DATE(event_date) asc";
		String query = "SELECT DATE(event_date) as event_date,user_id,event_id,start_time as time_slot,status FROM event_time_slots WHERE "
				+ "event_date >= '"+startDate+"' and event_date <= '"+endDate+"' and "
				+ "user_id in ("+userIds+") order by DATE(event_date) asc";
		List<Map<String, Object>> eventTimeSlotResults = jdbcTemplate
				.queryForList(query);
		return eventTimeSlotResults;
	}*/
	
	public List<EventTimeSlot> getFreeTimeSlotsByUserIdAndStartDateAndEndDate(Long userId,String startDate,String endDate) {
		
		List<String> paramenets = new ArrayList<>();
		paramenets.add(startDate);
		paramenets.add(endDate);
		paramenets.add(String.valueOf(userId));
		
		return jdbcTemplate.query("select * from event_time_slots WHERE "
				+ "event_date >= ? and event_date <= ? and user_id = ?",
				new RowMapper<EventTimeSlot>() {
					@Override
					public EventTimeSlot mapRow(ResultSet rs, int rownumber)
							throws SQLException {
						EventTimeSlot ets = new EventTimeSlot();
						ets.setEventDate(rs.getDate("event_date"));
						ets.setStartTime(rs.getLong("start_time"));
						ets.setUserId(rs.getLong("user_id"));
						ets.setStatus(rs.getString("status"));
						ets.setEventStartTime(rs.getDate("event_start_time"));

						return ets;
					}
				},paramenets.toArray());
	}
	
	public List<EventTimeSlot> getFreeTimeSlotsByDateAndUserId(String userIds,String startDate,String endDate) {
		
		List<String> paramenets = new ArrayList<>();
		paramenets.add(startDate);
		paramenets.add(endDate);
		
		StringBuilder builder = new StringBuilder();
		for( int i = 0 ; i < userIds.split(",").length; i++ ) {
		    builder.append("?,");
		    paramenets.add(userIds.split(",")[i]);
		}
		
		return jdbcTemplate.query("select * from event_time_slots WHERE "
				+ "event_date >= ? and event_date <= ? and user_id in ("+ builder.deleteCharAt( builder.length() -1 ).toString()+")",
				new RowMapper<EventTimeSlot>() {
					@Override
					public EventTimeSlot mapRow(ResultSet rs, int rownumber)
							throws SQLException {
						EventTimeSlot ets = new EventTimeSlot();
						ets.setEventDate(rs.getDate("event_date"));
						ets.setStartTime(rs.getLong("start_time"));
						ets.setUserId(rs.getLong("user_id"));
						ets.setStatus(rs.getString("status"));
						ets.setEventStartTime(rs.getDate("event_start_time"));

						return ets;
					}
				},paramenets.toArray());
	}
	
	public List<Event> findByCreatedByIdAndStartDateAndEventMemberStatus(Long createdById, String eventDate) {
		
		/*String query = "select *, event_temp.source as event_source,  em.source as member_source, em.name as non_cenes_member_name, u.name as origname from (select e.* from events e JOIN event_members em on e.event_id = em.event_id where "
				+ "e.start_time >= '"+eventDate+"' and e.start_time <=  '"+endDate+"' and  em.user_id = "+createdById+" and em.status = 'Going' "
				+ "and e.schedule_as in ('Event','Holiday','Gathering')) as event_temp JOIN event_members em on event_temp.event_id = em.event_id "
				+ "LEFT JOIN users u on em.user_id = u.user_id order by event_temp.start_time asc";*/
		
		
		String query = "select *, event_temp.source as event_source,  em.source as member_source, em.name as non_cenes_member_name, "
				+ "u.name as origname from (select e.* from events e JOIN event_members em on e.event_id = em.event_id where "
				+ "e.start_time >= '"+eventDate+"' and  em.user_id = "+createdById+" and em.status = 'Going' "
				+ "and e.schedule_as in ('Event','Holiday','Gathering') and e.is_active = "+Event.EventStatus.Active.ordinal()+" ) as event_temp JOIN event_members em on event_temp.event_id = em.event_id "
				+ "LEFT JOIN users u on em.user_id = u.user_id order by event_temp.start_time asc limit 100";
	
		System.out.println("Home Events Query : "+query);
		List<Map<String, Object>> userGatheringsMapList = jdbcTemplate.queryForList(query);
		
		
		Map<Long, Event> eventIdMap = new HashMap<Long, Event>();
		for (Map<String, Object> userGatheringMap: userGatheringsMapList) {
			Event event = null;
			if (eventIdMap.containsKey(Long.valueOf(userGatheringMap.get("event_id").toString()))) {
				event = eventIdMap.get(Long.valueOf(userGatheringMap.get("event_id").toString()));
				
				List<EventMember> eventMmembers = event.getEventMembers();
				eventMmembers.add(populateEventMembers(userGatheringMap));
				event.setEventMembers(eventMmembers);
			} else {
				event = populateEventBo(userGatheringMap);
				List<EventMember> eventMmembers = null;
				if (event.getEventMembers() == null) {
					eventMmembers = new ArrayList<>();
				} else {
					eventMmembers = event.getEventMembers();
				}
				
				eventMmembers.add(populateEventMembers(userGatheringMap));
				event.setEventMembers(eventMmembers);
			}
			eventIdMap.put(event.getEventId(), event);
		}
		
		List<Event> events = new ArrayList<>();
		for (Entry<Long, Event> eventEntrySet: eventIdMap.entrySet()) {
			events.add(eventEntrySet.getValue());
		}
		return events;
	}
	
	public String findSourcesQueryString(Long userId) {
		
		String sourcesQuery = "";

		StringBuffer sources = new StringBuffer();
		sources.append("'Cenes',");
		
		StringBuffer scheduleAs = new StringBuffer();
		scheduleAs.append("'Gathering',");
		
		String calendarsQuery = "select account_type from calendar_sync_tokens where user_id = "+userId+" and is_active = 1";
		List<Map<String, Object>> results = jdbcTemplate.queryForList(calendarsQuery);
		if (results != null && results.size() > 0) {
			for (Map<String, Object> result: results) {
				if (result.get("account_type").toString().equals("Google") ||  result.get("account_type").toString().equals("Outlook") || result.get("account_type").toString().equals("Apple")) {
					sources.append("'"+result.get("account_type").toString()+"'").append(",");
				}
			}			
			
			if (sources.indexOf("Google") != -1 || sources.indexOf("Outlook") != -1 || sources.indexOf("Apple") != -1) {
				scheduleAs.append("'Event'").append(",");
			}
		}
		//Check if user has Holidays Synced.?
		if (results != null && results.size() > 0) {
			for (Map<String, Object> result: results) {
				if (result.get("account_type").toString().equals("Holiday")) {
					scheduleAs.append("'Holiday'").append(",");
					sources.append("'GoogleHoliday'").append(",");
				}
			}
		}
		
		sourcesQuery += " and e.source in ("+sources.substring(0, sources.length()-1)+") ";

		sourcesQuery += " and e.schedule_as in ("+scheduleAs.substring(0, scheduleAs.length()-1)+") ";
		
		return sourcesQuery;
	}
	
	
	public int findCountByGatheringsByUserIdAndDate(Long userId, String startDate) {

		//String countQuery = "select count(*) from events e JOIN event_members em on e.event_id = em.event_id where e.start_time >= '"+startDate+"' and  "
			//	+ "em.user_id = "+userId+" and em.status = 'Going' and e.schedule_as in ('Event','Holiday','Gathering')";
		
		String sourcesQuery = findSourcesQueryString(userId);

		String query = "select count(*) from events e JOIN event_members em on e.event_id = em.event_id where "
				+ "e.start_time >= '"+startDate+"' and  em.user_id = "+userId+" and em.status = 'Going' and e.is_active = "+Event.EventStatus.Active.ordinal()+" "
				+ " "+sourcesQuery+"";
		System.out.println("Home Events Query : "+query);

		
		int numberOfTotalCounts = jdbcTemplate.queryForInt(query);
		return numberOfTotalCounts;
	}
	
	
	public int findCountByGatheringsByUserIdAndStartDateAndEndDate(Long userId, String startDate, String endDate) {

		//String countQuery = "select count(*) from events e JOIN event_members em on e.event_id = em.event_id where e.start_time >= '"+startDate+"' and  "
			//	+ "em.user_id = "+userId+" and em.status = 'Going' and e.schedule_as in ('Event','Holiday','Gathering')";
		
		String sourcesQuery = findSourcesQueryString(userId);

		String query = "select count(*) from events e JOIN event_members em on e.event_id = em.event_id where "
				+ "e.start_time >= '"+startDate+"' and and e.start_time >= '"+endDate+"' and  em.user_id = "+userId+" and em.status = 'Going' and e.is_active = "+Event.EventStatus.Active.ordinal()+" "
				+ " "+sourcesQuery+"";
		System.out.println("Home Events Query : "+query);

		
		int numberOfTotalCounts = jdbcTemplate.queryForInt(query);
		return numberOfTotalCounts;
	}
	
	
	public List<Event> getPastEventsByCreatedByIdAndStartDateAndEndDate(Long createdById, String startDate, String endDate) {
		
		String sourcesQuery = findSourcesQueryString(createdById);
	
		String query =  "select *, event_temp.source as event_source,  em.source as member_source, em.name as non_cenes_member_name, u.name as origname from "
					+ "(select e.* from events e JOIN event_members em on e.event_id = em.event_id where "
					+ "e.start_time >= '"+startDate+"' and e.start_time < '"+endDate+"' and em.user_id = "+createdById+" and "
					+ "e.is_active = "+Event.EventStatus.Active.ordinal()+" and em.status = 'Going' "
					+ " "+sourcesQuery+" order by e.start_time asc) as event_temp JOIN event_members em on event_temp.event_id = em.event_id "
					+ "LEFT JOIN users u on em.user_id = u.user_id order by event_temp.start_time asc";
		
		System.out.println("Home Past Events Query : "+query);
		
		List<Map<String, Object>> userGatheringsMapList = jdbcTemplate.queryForList(query);
		
		
		Map<Long, Event> eventIdMap = new HashMap<Long, Event>();
		for (Map<String, Object> userGatheringMap: userGatheringsMapList) {
			Event event = null;
			if (eventIdMap.containsKey(Long.valueOf(userGatheringMap.get("event_id").toString()))) {
				event = eventIdMap.get(Long.valueOf(userGatheringMap.get("event_id").toString()));
				
				List<EventMember> eventMmembers = event.getEventMembers();
				eventMmembers.add(populateEventMembers(userGatheringMap));
				event.setEventMembers(eventMmembers);
			} else {
				event = populateEventBo(userGatheringMap);
				List<EventMember> eventMmembers = null;
				if (event.getEventMembers() == null) {
					eventMmembers = new ArrayList<>();
				} else {
					eventMmembers = event.getEventMembers();
				}
				
				eventMmembers.add(populateEventMembers(userGatheringMap));
				event.setEventMembers(eventMmembers);
			}
			eventIdMap.put(event.getEventId(), event);
		}
		
		List<Event> events = new ArrayList<>();
		for (Entry<Long, Event> eventEntrySet: eventIdMap.entrySet()) {
			events.add(eventEntrySet.getValue());
		}
		return events;
	}
	
	
	public List<Event> findPaginationByCreatedByIdAndStartDate(Long createdById, String eventDate, int pageNumber, int offSet) {
		
		String sourcesQuery = findSourcesQueryString(createdById);
				
		/*String query = "select *, event_temp.source as event_source,  em.source as member_source, em.name as non_cenes_member_name, u.name as origname from "
				+ "(select e.* from events e JOIN event_members em on e.event_id = em.event_id where "
				+ "e.start_time >= '"+eventDate+"' and  em.user_id = "+createdById+" and em.status = 'Going' "
				+ "and e.schedule_as in ('Event','Holiday','Gathering')) as event_temp JOIN event_members em on event_temp.event_id = em.event_id "
				+ "LEFT JOIN users u on em.user_id = u.user_id order by event_temp.start_time asc limit "+pageNumber+","+offSet+"";*/
	
		String query =  "select *, event_temp.source as event_source,  em.source as member_source, em.name as non_cenes_member_name, u.name as origname, "
					+ "uc.name as phonebookName from "
					+ "(select e.* from events e JOIN event_members em on e.event_id = em.event_id where "
					+ "e.start_time >= '"+eventDate+"' and e.is_active = "+Event.EventStatus.Active.ordinal()+" and em.user_id = "+createdById+" and em.status = 'Going' "
					+ " "+sourcesQuery+" order by e.start_time asc limit "+pageNumber+","+offSet+") as event_temp JOIN event_members em on event_temp.event_id = em.event_id "
					+ "LEFT JOIN users u on em.user_id = u.user_id LEFT JOIN user_contacts uc on em.user_contact_id = uc.user_contact_id "
					+ "order by event_temp.start_time asc";
		
			System.out.println("Home Events Query : "+query);
				
		
		List<Map<String, Object>> userGatheringsMapList = jdbcTemplate.queryForList(query);
		
		
		Map<Long, Event> eventIdMap = new HashMap<Long, Event>();
		for (Map<String, Object> userGatheringMap: userGatheringsMapList) {
			Event event = null;
			if (eventIdMap.containsKey(Long.valueOf(userGatheringMap.get("event_id").toString()))) {
				event = eventIdMap.get(Long.valueOf(userGatheringMap.get("event_id").toString()));
				
				List<EventMember> eventMmembers = event.getEventMembers();
				eventMmembers.add(populateEventMembers(userGatheringMap));
				event.setEventMembers(eventMmembers);
			} else {
				event = populateEventBo(userGatheringMap);
				List<EventMember> eventMmembers = null;
				if (event.getEventMembers() == null) {
					eventMmembers = new ArrayList<>();
				} else {
					eventMmembers = event.getEventMembers();
				}
				
				eventMmembers.add(populateEventMembers(userGatheringMap));
				event.setEventMembers(eventMmembers);
			}
			eventIdMap.put(event.getEventId(), event);
		}
		
		List<Event> events = new ArrayList<>();
		for (Entry<Long, Event> eventEntrySet: eventIdMap.entrySet()) {
			events.add(eventEntrySet.getValue());
		}
		return events;
	}
	
public List<Event> findMonthWiseByCreatedByIdAndStartDate(Long createdById, String eventDate, String endDate) {
		
		String sourcesQuery = findSourcesQueryString(createdById);
				
		/*String query = "select *, event_temp.source as event_source,  em.source as member_source, em.name as non_cenes_member_name, u.name as origname from "
				+ "(select e.* from events e JOIN event_members em on e.event_id = em.event_id where "
				+ "e.start_time >= '"+eventDate+"' and  em.user_id = "+createdById+" and em.status = 'Going' "
				+ "and e.schedule_as in ('Event','Holiday','Gathering')) as event_temp JOIN event_members em on event_temp.event_id = em.event_id "
				+ "LEFT JOIN users u on em.user_id = u.user_id order by event_temp.start_time asc limit "+pageNumber+","+offSet+"";*/
	
		String query =  "select *, event_temp.source as event_source,  em.source as member_source, em.name as non_cenes_member_name, u.name as origname, "
					+ "uc.name as phonebookName from "
					+ "(select e.* from events e JOIN event_members em on e.event_id = em.event_id where "
					+ "e.start_time >= '"+eventDate+"' and e.start_time <= '"+endDate+"' and e.is_active = "+Event.EventStatus.Active.ordinal()+" "
					+ "and em.user_id = "+createdById+" and em.status = 'Going' "
					+ " "+sourcesQuery+" order by e.start_time asc) as event_temp JOIN event_members em on event_temp.event_id = em.event_id "
					+ "LEFT JOIN users u on em.user_id = u.user_id LEFT JOIN user_contacts uc on em.user_contact_id = uc.user_contact_id "
					+ "order by event_temp.start_time asc";
		
			System.out.println("Home Events Query : "+query);
				
		
		List<Map<String, Object>> userGatheringsMapList = jdbcTemplate.queryForList(query);
		
		
		Map<Long, Event> eventIdMap = new HashMap<Long, Event>();
		for (Map<String, Object> userGatheringMap: userGatheringsMapList) {
			Event event = null;
			if (eventIdMap.containsKey(Long.valueOf(userGatheringMap.get("event_id").toString()))) {
				event = eventIdMap.get(Long.valueOf(userGatheringMap.get("event_id").toString()));
				
				List<EventMember> eventMmembers = event.getEventMembers();
				eventMmembers.add(populateEventMembers(userGatheringMap));
				event.setEventMembers(eventMmembers);
			} else {
				event = populateEventBo(userGatheringMap);
				List<EventMember> eventMmembers = null;
				if (event.getEventMembers() == null) {
					eventMmembers = new ArrayList<>();
				} else {
					eventMmembers = event.getEventMembers();
				}
				
				eventMmembers.add(populateEventMembers(userGatheringMap));
				event.setEventMembers(eventMmembers);
			}
			eventIdMap.put(event.getEventId(), event);
		}
		
		List<Event> events = new ArrayList<>();
		for (Entry<Long, Event> eventEntrySet: eventIdMap.entrySet()) {
			events.add(eventEntrySet.getValue());
		}
		return events;
	}
	
	public List<Event> findEventsByNotifications(List<Notification> notifications, Long recepientId) {
		
		
		StringBuffer eventIds = new StringBuffer();
		for (Notification notifiacton: notifications) {
			eventIds.append(notifiacton.getNotificationTypeId()+",");
		}
		String eventIdsStr = eventIds.toString().substring(0, eventIds.toString().length() - 1);
		String query = "select *, us.name as nameuser, uc.name as phonebookName from events ev INNER JOIN event_members em on ev.event_id = em.event_id and ev.event_id in ("+eventIdsStr.toString()+")"
				+ " LEFT JOIN users us on em.user_id = us.user_id LEFT JOIN user_contacts uc on em.user_id = uc.friend_id and uc.uc_user_id = "+recepientId+"";
		System.out.println("Notification : "+query);
		
		List<Event> events = jdbcTemplate.query(query, new EventDataMapper());
		return events;
	}
	
	
	public List<Event> findPageableEventsByCreatedByIdAndStartDate(Long userId, String eventDate, int offset) {
	
		String query = "select *, event_temp.source as event_source,  em.source as member_source, em.name as non_cenes_member_name, "
				+ "u.name as origname, uc.name as phonebookName from (select e.* from events e JOIN event_members em on e.event_id = em.event_id where "
				+ "e.start_time >= '"+eventDate+"' and  em.user_id = "+userId+" and em.status = 'Going' "
				+ "and e.schedule_as in ('Event','Holiday','Gathering') and e.is_active = "+Event.EventStatus.Active.ordinal()+") as event_temp JOIN event_members em on event_temp.event_id = em.event_id "
				+ "LEFT JOIN users u on em.user_id = u.user_id LEFT JOIN user_contacts uc on em.user_id = uc.friend_id and uc.uc_user_id = "+userId+" "
				+ "order by event_temp.start_time asc limit "+offset+", 50";
	
		System.out.println("Gatherings Events Query : "+query);
		List<Map<String, Object>> userGatheringsMapList = jdbcTemplate.queryForList(query);
		
		
		Map<Long, Event> eventIdMap = new HashMap<Long, Event>();
		for (Map<String, Object> userGatheringMap: userGatheringsMapList) {
			Event event = null;
			if (eventIdMap.containsKey(Long.valueOf(userGatheringMap.get("event_id").toString()))) {
				event = eventIdMap.get(Long.valueOf(userGatheringMap.get("event_id").toString()));
				
				List<EventMember> eventMmembers = event.getEventMembers();
				eventMmembers.add(populateEventMembers(userGatheringMap));
				event.setEventMembers(eventMmembers);
			} else {
				event = populateEventBo(userGatheringMap);
				List<EventMember> eventMmembers = null;
				if (event.getEventMembers() == null) {
					eventMmembers = new ArrayList<>();
				} else {
					eventMmembers = event.getEventMembers();
				}
				
				eventMmembers.add(populateEventMembers(userGatheringMap));
				event.setEventMembers(eventMmembers);
			}
			eventIdMap.put(event.getEventId(), event);
		}
		
		List<Event> events = new ArrayList<>();
		for (Entry<Long, Event> eventEntrySet: eventIdMap.entrySet()) {
			events.add(eventEntrySet.getValue());
		}
		return events;
	}
	
	
	public Map<String,Object> findUserGatheringsByDateAndUserIdAndStatus(String startDate,Long userId,String status) {
		
		String gatheringQuery = "select GROUP_CONCAT(e.event_id) as eventIds from "
				+ "events e join event_members em on e.event_id = em.event_id where em.user_id = "+userId+" and e.schedule_as = 'Gathering' "
				+ "and e.source = 'Cenes' and em.status = '"+status+"' "
				+ "order by e.start_time asc";
		Map<String, Object> userGatherings = jdbcTemplate.queryForMap(gatheringQuery);
		return userGatherings;
	}
	
	public Map<String,Object> findUserDeclinedGatheringssByDateAndUserIdAndStatus(Long userId,String status) {
		
		String gatheringQuery = "select GROUP_CONCAT(e.event_id) as eventIds from "
				+ "events e join event_members em on e.event_id = em.event_id where em.user_id = "+userId+" and e.schedule_as = 'Gathering' "
				+ "and e.source = 'Cenes' and em.status = '"+status+"' "
				+ "order by e.start_time asc";
		Map<String, Object> userGatherings = jdbcTemplate.queryForMap(gatheringQuery);
		return userGatherings;
	}

	public List<Map<String,Object>> findPendingInvitationsByUserd(Long userId) {
		
		String pendingInvitationsQuery = "select e.event_id as eventId,e.title,e.location,e.description,e.event_picture,e.start_time as startTime,e.end_time as endTime,"
				+ "u.name as sender,em.event_member_id from events e join event_members em on e.event_id = em.event_id join users u on e.created_by_id = u.user_id "
				+ "where e.schedule_as = 'Gathering' and e.source = 'Cenes' and em.user_id = "+userId+" and "
				+ "em.status is null order by e.start_time asc";
		List<Map<String, Object>> pendingInvitations = jdbcTemplate.queryForList(pendingInvitationsQuery);
		return pendingInvitations;
	}
	
	public void deleteEventsByRecurringEventId(String recurringEventId) {
		
		String deleteEvents = "delete from events where recurring_event_id = '"+recurringEventId+"'";
		 jdbcTemplate.execute(deleteEvents);

	}
	
	
	public void deleteEventTimeSlotsByEventId(Long eventId) {
		
		String deleteEvents = "delete from event_time_slots where event_id = "+eventId+"";
		 jdbcTemplate.execute(deleteEvents);

	}
	
	public Event findGatheringByEventId(Long eventId) {
		
		String gatheringQuery = "select *,e.source as event_source,  em.source as member_source, "
				+ "em.name as non_cenes_member_name, u.name as origname from events e "
				+ "JOIN event_members em on e.event_id = em.event_id LEFT JOIN users u on em.user_id = u.user_id "
				+ "where e.event_id = "+eventId;
		
		System.out.println("Query : "+gatheringQuery);
		List<Map<String, Object>> userGatherings = jdbcTemplate.queryForList(gatheringQuery);
		
		Map<Long, Event> eventIdMap = new HashMap<Long, Event>();
		if (userGatherings != null) {
			
			try {
				for (Map<String, Object> userGatheringMap: userGatherings) {
					//System.out.println(userGatheringMap.toString());
					Event event = null;
					if (eventIdMap.containsKey(Long.valueOf(userGatheringMap.get("event_id").toString()))) {
						event = eventIdMap.get(Long.valueOf(userGatheringMap.get("event_id").toString()));
						
						List<EventMember> eventMmembers = event.getEventMembers();
						eventMmembers.add(populateEventMembers(userGatheringMap));
						event.setEventMembers(eventMmembers);
					} else {
						event = populateEventBo(userGatheringMap);
						List<EventMember> eventMmembers = null;
						if (event.getEventMembers() == null) {
							eventMmembers = new ArrayList<>();
						} else {
							eventMmembers = event.getEventMembers();
						}
						
						eventMmembers.add(populateEventMembers(userGatheringMap));
						event.setEventMembers(eventMmembers);
					}
					eventIdMap.put(event.getEventId(), event);
				}
				
				List<Event> events = new ArrayList<>();
				for (Entry<Long, Event> eventEntrySet: eventIdMap.entrySet()) {
					events.add(eventEntrySet.getValue());
				}
				if (events.size() > 0) {
					return events.get(0);
				} else {
					return null;
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	public Event findGatheringByKey(String key) {
		
		String gatheringQuery = "select *,e.source as event_source,  em.source as member_source, "
				+ "em.name as non_cenes_member_name, u.name as origname from events e "
				+ "JOIN event_members em on e.event_id = em.event_id LEFT JOIN users u on em.user_id = u.user_id "
				+ "where e.private_key = '"+key+"'";
		
		System.out.println("Query : "+gatheringQuery);
		List<Map<String, Object>> userGatherings = jdbcTemplate.queryForList(gatheringQuery);
		
		Map<Long, Event> eventIdMap = new HashMap<Long, Event>();
		if (userGatherings != null) {
			
			try {
				for (Map<String, Object> userGatheringMap: userGatherings) {
					//System.out.println(userGatheringMap.toString());
					Event event = null;
					if (eventIdMap.containsKey(Long.valueOf(userGatheringMap.get("event_id").toString()))) {
						event = eventIdMap.get(Long.valueOf(userGatheringMap.get("event_id").toString()));
						
						List<EventMember> eventMmembers = event.getEventMembers();
						eventMmembers.add(populateEventMembers(userGatheringMap));
						event.setEventMembers(eventMmembers);
					} else {
						event = populateEventBo(userGatheringMap);
						List<EventMember> eventMmembers = null;
						if (event.getEventMembers() == null) {
							eventMmembers = new ArrayList<>();
						} else {
							eventMmembers = event.getEventMembers();
						}
						
						eventMmembers.add(populateEventMembers(userGatheringMap));
						event.setEventMembers(eventMmembers);
					}
					eventIdMap.put(event.getEventId(), event);
				}
				
				List<Event> events = new ArrayList<>();
				for (Entry<Long, Event> eventEntrySet: eventIdMap.entrySet()) {
					events.add(eventEntrySet.getValue());
				}
				if (events.size() > 0) {
					return events.get(0);
				} else {
					return null;
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	
	public List<Event> findGatheringsByUserIdAndStatus(Long userId, String status) {
		
		String query = "select *, event_temp.source as event_source,  em.source as member_source, em.name as non_cenes_member_name, u.name as origname from (select e.* from events e JOIN event_members em on e.event_id = em.event_id where "
				+ "DATE(e.end_time) >= DATE(now()) and  e.schedule_as = 'Gathering' and em.user_id = "+userId+" and em.status = '"+status+"' and e.is_active = "+Event.EventStatus.Active.ordinal()+") as event_temp "
				+ "JOIN event_members em on event_temp.event_id = em.event_id LEFT JOIN users u on em.user_id = u.user_id order by event_temp.start_time asc";
		
		System.out.println(query);
		
		List<Map<String, Object>> userGatheringsMapList = jdbcTemplate.queryForList(query);
		
		
		Map<Long, Event> eventIdMap = new HashMap<Long, Event>();
		for (Map<String, Object> userGatheringMap: userGatheringsMapList) {
			Event event = null;
			if (eventIdMap.containsKey(Long.valueOf(userGatheringMap.get("event_id").toString()))) {
				event = eventIdMap.get(Long.valueOf(userGatheringMap.get("event_id").toString()));
				
				List<EventMember> eventMmembers = event.getEventMembers();
				eventMmembers.add(populateEventMembers(userGatheringMap));
				event.setEventMembers(eventMmembers);
			} else {
				event = populateEventBo(userGatheringMap);
				List<EventMember> eventMmembers = null;
				if (event.getEventMembers() == null) {
					eventMmembers = new ArrayList<>();
				} else {
					eventMmembers = event.getEventMembers();
				}
				
				eventMmembers.add(populateEventMembers(userGatheringMap));
				event.setEventMembers(eventMmembers);
			}
			eventIdMap.put(event.getEventId(), event);
		}
		
		List<Event> events = new ArrayList<>();
		for (Entry<Long, Event> eventEntrySet: eventIdMap.entrySet()) {
			events.add(eventEntrySet.getValue());
		}
		

		events.sort(Comparator.comparing(Event::getStartTime, (star1, star2) -> {
		    if(star1 == star2){
		         return 0;
		    }
		    return (star1.getTime() > star2.getTime()) ? -1 : 1;
		}));
                      
		
		return events;
	}
	
	public int findCountByGatheringsByUserIdAndStatus(Long userId, String status, String startDate) {
		String query = "select count(*) from events e JOIN event_members em on e.event_id = em.event_id where "
				+ "e.end_time >= '"+startDate+"' and  e.schedule_as = 'Gathering' and em.user_id = "+userId+" and ";
				
				if (status == null) {
					query += "em.status is NULL";
				} else {
					query += "em.status = '"+status+"'";
				}
			System.out.println(query);
			
		int numberOfTotalCounts = jdbcTemplate.queryForInt(query);
		return numberOfTotalCounts;
	}
	
public List<Event> findPageableGatheringsByUserIdAndStatus(Long userId, String status, String startDate, int pageNumber, int offSet) {
		
	String query = "select *, event_temp.source as event_source, em.user_id as em_user_id, em.source as member_source, em.name as non_cenes_member_name, u.name as origname, "
			+ "uc.name as phonebookName from (select e.* from events e JOIN event_members em on e.event_id = em.event_id where "
			+ "e.end_time >= '"+startDate+"' and  e.schedule_as = 'Gathering' and em.user_id = "+userId+" and ";
	
			if (status == null) {
				query += "em.status is NULL) as event_temp ";
			} else {
				query += "em.status = '"+status+"') as event_temp ";
			}
					
			query += "JOIN event_members em on event_temp.event_id = em.event_id LEFT JOIN users u on em.user_id = u.user_id "
			+ "LEFT JOIN user_contacts uc on em.user_id = uc.friend_id and uc.uc_user_id = "+userId+" "
			+ "order by event_temp.start_time asc limit "+pageNumber+","+offSet+" ";
		
		System.out.println(query);
		
	List<Map<String, Object>> userGatheringsMapList = jdbcTemplate.queryForList(query);
		
		
		Map<Long, Event> eventIdMap = new HashMap<Long, Event>();
		for (Map<String, Object> userGatheringMap: userGatheringsMapList) {
			Event event = null;
			if (eventIdMap.containsKey(Long.valueOf(userGatheringMap.get("event_id").toString()))) {
				event = eventIdMap.get(Long.valueOf(userGatheringMap.get("event_id").toString()));
				
				List<EventMember> eventMmembers = event.getEventMembers();
				eventMmembers.add(populateEventMembers(userGatheringMap));
				event.setEventMembers(eventMmembers);
			} else {
				event = populateEventBo(userGatheringMap);
				List<EventMember> eventMmembers = null;
				if (event.getEventMembers() == null) {
					eventMmembers = new ArrayList<>();
				} else {
					eventMmembers = event.getEventMembers();
				}
				
				eventMmembers.add(populateEventMembers(userGatheringMap));
				event.setEventMembers(eventMmembers);
			}
			eventIdMap.put(event.getEventId(), event);
		}
		
		List<Event> events = new ArrayList<>();
		for (Entry<Long, Event> eventEntrySet: eventIdMap.entrySet()) {
			events.add(eventEntrySet.getValue());
		}
		

		events.sort(Comparator.comparing(Event::getStartTime, (star1, star2) -> {
		    if(star1 == star2){
		         return 0;
		    }
		    return (star1.getTime() < star2.getTime()) ? -1 : 1;
		}));
                      
		
		//List<Event> events = jdbcTemplate.query(query, new EventDataMapper());
		
		return events;
	}
	
	public List<Event> findGatheringsByStatusNull(Long userId) {
		String query = "select *, event_temp.source as event_source,  em.source as member_source, em.name as non_cenes_member_name, u.name as origname from (select e.* from events e JOIN event_members em on e.event_id = em.event_id where "
				+ "DATE(e.end_time) >= DATE(now()) and  e.schedule_as = 'Gathering' and em.user_id = "+userId+" and em.status is null) as event_temp "
				+ "JOIN event_members em on event_temp.event_id = em.event_id JOIN users u on em.user_id = u.user_id order by event_temp.start_time asc";

		List<Map<String, Object>> userGatheringsMapList = jdbcTemplate.queryForList(query);
		
		
		Map<Long, Event> eventIdMap = new HashMap<Long, Event>();
		for (Map<String, Object> userGatheringMap: userGatheringsMapList) {
			Event event = null;
			if (eventIdMap.containsKey(Long.valueOf(userGatheringMap.get("event_id").toString()))) {
				event = eventIdMap.get(Long.valueOf(userGatheringMap.get("event_id").toString()));
				
				List<EventMember> eventMmembers = event.getEventMembers();
				eventMmembers.add(populateEventMembers(userGatheringMap));
				event.setEventMembers(eventMmembers);
			} else {
				event = populateEventBo(userGatheringMap);
				
				List<EventMember> eventMmembers = null;
				if (event.getEventMembers() == null) {
					eventMmembers = new ArrayList<>();
				} else {
					eventMmembers = event.getEventMembers();
				}				eventMmembers.add(populateEventMembers(userGatheringMap));
				event.setEventMembers(eventMmembers);
			}
			eventIdMap.put(event.getEventId(), event);
		}
		
		List<Event> events = new ArrayList<>();
		for (Entry<Long, Event> eventEntrySet: eventIdMap.entrySet()) {
			events.add(eventEntrySet.getValue());
		}
		return events;
	}
	
	public List<Map<String, Object>> findHomeCalendarEventsString(Long userId, String startDate, String endDate) {
		
		String sourcesQuery = "";
		
		StringBuffer sources = new StringBuffer();
		sources.append("'Cenes',");
		
		StringBuffer scheduleAs = new StringBuffer();
		scheduleAs.append("'Gathering',");
		
		String calendarsQuery = "select account_type from calendar_sync_tokens where user_id = "+userId+"  and is_active = 1";
		List<Map<String, Object>> results = jdbcTemplate.queryForList(calendarsQuery);
		if (results != null && results.size() > 0) {
			for (Map<String, Object> result: results) {
				if (result.get("account_type").toString().equals("Google") ||  result.get("account_type").toString().equals("Outlook") || result.get("account_type").toString().equals("Apple")) {
					sources.append("'"+result.get("account_type").toString()+"'").append(",");
				}
			}			
			
			if (sources.indexOf("Google") != -1 || sources.indexOf("Outlook") != -1 || sources.indexOf("Apple") != -1) {
				scheduleAs.append("'Event'").append(",");
			}
		}
		//Check if user has Holidays Synced.?
		if (results != null && results.size() > 0) {
			for (Map<String, Object> result: results) {
				if (result.get("account_type").toString().equals("Holiday")) {
					scheduleAs.append("'Holiday'").append(",");
					sources.append("'GoogleHoliday'").append(",");
				}
			}
		}
		
		sourcesQuery += " and e.source in ("+sources.substring(0, sources.length()-1)+") ";

		sourcesQuery += " and e.schedule_as in ("+scheduleAs.substring(0, scheduleAs.length()-1)+") ";

		
		String query = "select start_time, schedule_as from events e where e.created_by_id = "+userId+" and e.start_time >= '"+startDate+"' and "
				+ "e.start_time <= '"+endDate+"' "+sourcesQuery+" ";
		System.out.println(query);
		
		List<Map<String, Object>> userGatheringsMapList = jdbcTemplate.queryForList(query);
		return userGatheringsMapList;
	}
	
	
	
	public Event populateEventBo(Map<String, Object> eventMap) {
		Event event = new Event();
		
		try {
			if (eventMap.get("event_id") != null) {
				event.setEventId(Long.valueOf(eventMap.get("event_id").toString()));
			}
			if (eventMap.get("created_by_id") != null) {
				event.setCreatedById(Long.valueOf(eventMap.get("created_by_id").toString()));
			}
			if (eventMap.get("title") != null) {
				event.setTitle(eventMap.get("title").toString());
			}
			if (eventMap.get("recurring_event_id") != null) {
				event.setRecurringEventId(eventMap.get("recurring_event_id").toString());
			}
			if (eventMap.get("location") != null) {
				event.setLocation(eventMap.get("location").toString());
			}
			if (eventMap.get("latitude") != null) {
				event.setLatitude(eventMap.get("latitude").toString());
			}
			if (eventMap.get("longitude") != null) {
				event.setLongitude(eventMap.get("longitude").toString());
			}
			if (eventMap.get("description") != null) {
				event.setDescription(eventMap.get("description").toString());
			}
			if (eventMap.get("event_source") != null) {
				event.setSource(eventMap.get("event_source").toString());
			}
			if (eventMap.get("source_event_id") != null) {
				event.setSourceEventId(eventMap.get("source_event_id").toString());
			}
			if (eventMap.get("source_user_id") != null) {
				event.setSourceUserId(eventMap.get("source_user_id").toString());
			}
			if (eventMap.get("schedule_as") != null) {
				event.setScheduleAs(eventMap.get("schedule_as").toString());
			}
			if (eventMap.get("event_picture") != null) {
				event.setEventPicture(eventMap.get("event_picture").toString());
			}
			if (eventMap.get("thumbnail") != null) {
				event.setThumbnail(eventMap.get("thumbnail").toString());
			}
			if (eventMap.get("start_time") != null) {
				event.setStartTime((Date)eventMap.get("start_time"));
			}
			if (eventMap.get("timezone") != null) {
				event.setTimezone(eventMap.get("timezone").toString());
			}
			if (eventMap.get("end_time") != null) {
				event.setEndTime((Date)eventMap.get("end_time"));
			}
			
			//If its a full day event then we are sending the Date String as yyyyMMdd format
			if (eventMap.get("is_full_day") != null && (Boolean)eventMap.get("is_full_day")) {
				event.setFullDayStartTime(CenesUtils.yyyyMMdd.format(event.getStartTime()));
			}
			
			if (eventMap.get("is_full_day") != null) {
				event.setIsFullDay((Boolean)eventMap.get("is_full_day"));
			}
			if (eventMap.get("is_predictive_on") != null) {
				event.setIsPredictiveOn((Boolean)eventMap.get("is_predictive_on"));
			}
			if (eventMap.get("predictive_data") != null) {
				event.setPredictiveData(eventMap.get("predictive_data").toString());
			}
			if (eventMap.get("private_key") != null) {
				event.setKey(eventMap.get("private_key").toString());
			}
			if (eventMap.get("expired") != null) {
				event.setExpired(Boolean.valueOf(eventMap.get("expired").toString()));
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return event;
	}
	
	
	public EventMember populateEventMembers(Map<String, Object> eventMembersMap) {
		
		EventMember eventMember = new EventMember();
		//System.out.println(eventMembersMap.toString());
		try {
			if  (eventMembersMap.get("event_member_id") != null) {
				eventMember.setEventMemberId(Long.valueOf(eventMembersMap.get("event_member_id").toString()));
			}
			if  (eventMembersMap.get("event_id") != null) {
				eventMember.setEventId(Long.valueOf(eventMembersMap.get("event_id").toString()));
			}
			if  (eventMembersMap.get("member_source") != null) {
				eventMember.setSource(eventMembersMap.get("member_source").toString());
			}
			if  (eventMembersMap.get("source_email") != null) {
				eventMember.setSourceEmail(eventMembersMap.get("source_email").toString());
			}
			if  (eventMembersMap.get("source_id") != null) {
				eventMember.setSourceId(eventMembersMap.get("source_id").toString());
			}
			
			if (eventMembersMap.get("origname") != null) {
				eventMember.setName(eventMembersMap.get("origname").toString());
			} else if  (eventMembersMap.get("name") != null) {
				eventMember.setName(eventMembersMap.get("name").toString());
			} else {
				if (eventMembersMap.get("non_cenes_member_name") != null) {
					eventMember.setName(eventMembersMap.get("non_cenes_member_name").toString());
				} else {
					eventMember.setName("Guest");
				}
			}
			if  (eventMembersMap.get("picture") != null) {
				eventMember.setPicture(eventMembersMap.get("picture").toString());
			}
			if  (eventMembersMap.get("status") != null) {
				eventMember.setStatus(eventMembersMap.get("status").toString());
			}
			
			if (eventMembersMap.get("em_user_id") != null) {
				eventMember.setUserId(Long.valueOf(eventMembersMap.get("em_user_id").toString()));
			} else if  (eventMembersMap.get("user_id") != null) {
				eventMember.setUserId(Long.valueOf(eventMembersMap.get("user_id").toString()));
			}
			
			
			if  (eventMembersMap.get("processed") != null) {
				eventMember.setProcessed(Integer.valueOf(eventMembersMap.get("processed").toString()));
			}
			if  (eventMembersMap.get("user_contact_id") != null) {
				eventMember.setUserContactId(Long.valueOf(eventMembersMap.get("user_contact_id").toString()));
			}
			
			if  (eventMembersMap.get("em_user_id") != null || eventMembersMap.get("user_id") != null) {
				eventMember.setUser(populateUser(eventMembersMap));
			}
			
			if (eventMembersMap.get("user_id") != null) {
				try {
					UserContact userContact = null;
					if (eventMembersMap.get("phonebookName") != null) {
						userContact =  new UserContact();
						userContact.setName(eventMembersMap.get("phonebookName").toString());
					}
					eventMember.setUserContact(userContact);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return eventMember;
		
	}
	
	public User populateUser(Map<String, Object> userMap) {
		User user = new User();
		if (userMap.get("user_id") != null) {
			user.setUserId(Long.valueOf(userMap.get("user_id").toString()));
		}
		if (userMap.get("origname") != null) {
			user.setName(userMap.get("origname").toString());
		} else if (userMap.get("nameuser") != null) {
			user.setName(userMap.get("nameuser").toString());
		}
		
		if (userMap.get("photo") != null) {
			user.setPhoto(userMap.get("photo").toString());
		}
		if (userMap.get("phone") != null) {
			user.setPhone(userMap.get("phone").toString());
		}		
		return user;
	}
	
	public List<LocationDto> findDistinctEventLocations(Long userId) {
		
		List<LocationDto> distinctLocations = new ArrayList<>();
		
		List<Object> paramenets = new ArrayList<>();
		paramenets.add(userId);
		paramenets.add("Gathering");
		
		String query = "select location, event_picture as photo from events "
				+ "where created_by_id = ? and schedule_as = ? and "
				+ "location is not null and location != '' limit 20";
		
		List<LocationDto> locations = jdbcTemplate.query(query,
				new RowMapper<LocationDto>() {
					@Override
					public LocationDto mapRow(ResultSet rs, int rownumber)
							throws SQLException {
						LocationDto ldto = new LocationDto();
						ldto.setLocation(rs.getString("location"));
						ldto.setPhoto(rs.getString("photo"));
						return ldto;
					}
				},paramenets.toArray());
		
		Map<String, LocationDto> ldtoMap = new HashMap<>();
		
		for (LocationDto dto: locations) {
			if (!ldtoMap.containsKey(dto.getLocation())) {
				ldtoMap.put(dto.getLocation(), dto);
				distinctLocations.add(dto);
			}
		}
		return distinctLocations;
	}
}
