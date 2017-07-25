package com.cg.events;

import java.util.Date;

import org.junit.Test;

import com.cg.events.bo.Event;
import com.cg.events.bo.Event.EventType;
import com.cg.events.bo.EventMember;
import com.cg.events.bo.EventMember.MemberStatus;

import junit.framework.TestCase;

public class TestEventController extends TestCase{

	@Test
	public void testSaveEvent() {
		Event event = new Event();
		event.setTitle("FootBall Match");
		event.setMessage("Football match in ABC stadium");
		event.setLocation("Soccerr Field, Chicago");
		event.setStartTime(new Date());
		event.setEndTime(new Date());
		event.setCreatedById(1l);
		event.setType(EventType.Sport.toString());
		
		EventMember eventMember = new EventMember();
		eventMember.setEventId(event.getEventId());
		eventMember.setMemberStatus(MemberStatus.Going.toString());
		eventMember.setMemberId(2l);
		
	}
}
