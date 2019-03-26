package com.cg.dao;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.cg.bo.UserStat;
import com.cg.events.bo.EventMember;
import com.cg.mappers.UserStatMapper;
import com.cg.user.bo.UserContact;

@Service
public class UserDao {

	@Autowired
	JdbcTemplate jdbcTemplate;

	public List<UserStat> getUserStatsByUserContacts(List<UserContact> userContacts) {

		StringBuffer userIdBuffer = new StringBuffer();
		Iterator<UserContact> contacts = userContacts.iterator();
		while (contacts.hasNext()) {
			UserContact contact = contacts.next();
			userIdBuffer.append(contact.getUserId());
			if (contacts.hasNext()) {
				userIdBuffer.append(",");
			}
		}
		
		String query = "select * from user_stats where user_id in ("+userIdBuffer.toString()+")";
		System.out.println(query);
		
		List <UserStat> usersStat = jdbcTemplate.query(query, new UserStatMapper());
	    return usersStat;
	}
	
	public List<UserStat> getUserStatByEventMembers(List<EventMember> eventMembers) {

		StringBuffer userIdBuffer = new StringBuffer();
		Iterator<EventMember> members = eventMembers.iterator();
		while (members.hasNext()) {
			EventMember mem = members.next();
			userIdBuffer.append(mem.getUserId());
			if (members.hasNext()) {
				userIdBuffer.append(",");
			}
		}
		
		String query = "select * from user_stats where user_id in ("+userIdBuffer.toString()+")";
		System.out.println(query);
		
		List <UserStat> usersStat = jdbcTemplate.query(query, new UserStatMapper());
	    return usersStat;
	}
	
}
