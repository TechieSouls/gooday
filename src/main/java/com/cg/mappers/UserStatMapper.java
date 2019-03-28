package com.cg.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.cg.bo.UserStat;

public class UserStatMapper implements RowMapper<UserStat> {
	public UserStat mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		UserStat userStat = new UserStat();

		userStat.setUserStatId(rs.getLong("user_stat_id"));
		userStat.setUserId(rs.getLong("user_id"));
		userStat.setCenesMemberCounts(rs.getLong("cenes_member_counts"));
		userStat.setEventsAttendedCounts(rs.getLong("events_hosted_counts"));
		userStat.setEventsHostedCounts(rs.getLong("events_attended_counts"));

		return userStat;
	}
}