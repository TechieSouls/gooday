package com.cg.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.cg.events.bo.EventTimeSlot;

@Service
public class EventTimeSlotDao {

	@Autowired
	JdbcTemplate jdbcTemplate;


	public void saveEventTimeSlotBatch(final List<EventTimeSlot> eventTimeSlots) {

	    String sql = "INSERT INTO "
	        + "event_time_slots "
	        + "(end_time, event_date, event_id, start_time, status, user_id, source, event_start_time, schedule_as, recurring_event_id) "
	        + "VALUES " + "(?,?,?,?,?,?,?,?,?,?)";

	    jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				// TODO Auto-generated method stub
				EventTimeSlot eventTimeSlot = eventTimeSlots.get(i);
				ps.setLong(1, eventTimeSlot.getEndTime());
				ps.setDate(2, new java.sql.Date(eventTimeSlot.getEventDate().getTime()));
				ps.setLong(3, eventTimeSlot.getEventId());
				ps.setLong(4, eventTimeSlot.getStartTime());
				ps.setString(5, eventTimeSlot.getStatus());
				ps.setLong(6, eventTimeSlot.getUserId());
				ps.setString(7, eventTimeSlot.getSource());
				ps.setDate(8, new java.sql.Date(eventTimeSlot.getEventStartTime().getTime()));
				ps.setString(9, eventTimeSlot.getScheduleAs());
				ps.setLong(10, eventTimeSlot.getRecurringEventId());

			}
			
			@Override
			public int getBatchSize() {
				// TODO Auto-generated method stub
				return eventTimeSlots.size();
			}
		});

	}
}
