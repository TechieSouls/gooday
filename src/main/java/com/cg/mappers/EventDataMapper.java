package com.cg.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.cg.events.bo.Event;
import com.cg.events.bo.EventMember;

public class EventDataMapper extends BaseMapper implements RowMapper<Event> {
	
	public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
		Event event = populateEventBo(rs);
		
		EventMember eventMember = populateEventMembers(rs);
		if (eventMember != null && event != null) {
			
			eventMember.setUser(populateUserData(rs));
			if (event.getEventMembers() == null) {
				
				List<EventMember> eventMembers = new ArrayList<>();
				eventMembers.add(eventMember);
				event.setEventMembers(eventMembers);
			}
		}
		
		return event;
	}
}
