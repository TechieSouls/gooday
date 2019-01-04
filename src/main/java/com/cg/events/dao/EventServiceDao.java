package com.cg.events.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.cg.dto.LocationDto;
import com.cg.events.bo.EventTimeSlot;

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
				distinctLocations.add(dto);
			}
		}
		return distinctLocations;
	}
}
