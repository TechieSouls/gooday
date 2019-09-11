package com.cg.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cg.bo.CalendarSyncToken;
import com.cg.bo.CenesProperty;
import com.cg.bo.CenesProperty.PropertyOwningEntity;
import com.cg.bo.CenesPropertyValue;
import com.cg.dao.EventServiceDao;
import com.cg.events.bo.Event;
import com.cg.events.bo.EventMember;
import com.cg.events.bo.EventTimeSlot;
import com.cg.events.bo.RecurringEvent;
import com.cg.events.bo.RecurringEvent.RecurringEventProcessStatus;
import com.cg.events.bo.RecurringPattern;
import com.cg.events.repository.EventMemberRepository;
import com.cg.events.repository.EventRepository;
import com.cg.events.repository.EventTimeSlotRepository;
import com.cg.events.repository.RecurringPatternRepository;
import com.cg.repository.CalendarSyncTokenRepository;
import com.cg.repository.CenesPropertyRepository;
import com.cg.repository.CenesPropertyValueRepository;
import com.cg.repository.RecurringEventRepository;

@Service
public class EventService {

	@Autowired
	EventRepository eventRepository;
	
	@Autowired
	EventMemberRepository eventMemberRepository;
	
	@Autowired
	RecurringEventRepository recurringEventRepository;
	
	@Autowired
	RecurringPatternRepository recurringPatternRepository;
	
	@Autowired
	EventServiceDao eventServiceDao;
	
	@Autowired
	CenesPropertyRepository cenesPropertyRepository;
	
	@Autowired
	CenesPropertyValueRepository cenesPropertyValueRepository;
	
	@Autowired
	EventTimeSlotRepository eventTimeSlotRepository;
	
	@Autowired
	CalendarSyncTokenRepository calendarSyncTokenRepository;
	
	public List<RecurringEvent> findUnProcessRecurringEvents() {
		List<RecurringEvent> recurringEvents = recurringEventRepository.findUnprocessedEvents(RecurringEventProcessStatus.unprocessed.ordinal());
		return recurringEvents;
	}
	
	public RecurringPattern findRecurringPatternByRecurringEventId(Long recurringEventId) {
		RecurringPattern recurringPattern = recurringPatternRepository.findByRecurringEventId(recurringEventId);
		return recurringPattern;
	}
	
	public RecurringEvent saveUpdateRecurringEvent(RecurringEvent recurringEvent) {
		return recurringEventRepository.save(recurringEvent);
	}
	
	public void deleteEventsBatch(List<Event> events) {
		eventRepository.delete(events);
	}

	public EventMember saveEventMember(EventMember eventMember) {
		return eventMemberRepository.save(eventMember);
	}
	
	public Event saveEvent(Event event) {
		return eventRepository.save(event);
	}
	
	public void saveEventsBatch(List<Event> events) {
		eventRepository.save(events);
	}
	
	public void saveAndUpdateEventTimeSlot(List<EventTimeSlot> timeSlots) {
		eventTimeSlotRepository.save(timeSlots);
	}
	
	public void deleteGathering(Long gatheringId) {
		eventRepository.delete(gatheringId);
	}
	
	public void deleteEventsByRecurringId(String recurringEventId) {
		eventServiceDao.deleteEventsByRecurringEventId(recurringEventId);
	}
	
	public void saveUpdateRecurringPattern(RecurringPattern recurringPattern) {
		recurringPatternRepository.save(recurringPattern);
	}
	
	public RecurringEvent findRecurrungEventBySourceEventIdAndCreatedById(String sourceEventId,Long userId) {
		return recurringEventRepository.findBySourceEventIdAndCreatedById(sourceEventId,userId);
	}
	
	public Map<String,Object> findConfirmedGatherings(String startDate,Long userId,String status) {
		Map<String,Object> eventMembersList = eventServiceDao.findUserGatheringsByDateAndUserIdAndStatus(startDate,userId,status);
		return eventMembersList;
	}
	
	public List<Event> findPendingInvitations(Long userId) {
		//List<Map<String,Object>> eventMembersList = eventServiceDao.findPendingInvitationsByUserd(userId);
		//List<Event> pendings = eventRepository.findPendingEvents(userId);
		return eventServiceDao.findGatheringsByStatusNull(userId);
	}
	public Map<String,Object> findDeclinedGatherings(Long userId,String status) {
		Map<String,Object> eventMembersList = eventServiceDao.findUserDeclinedGatheringssByDateAndUserIdAndStatus(userId,status);
		return eventMembersList;
	}
	public List<Event> findUserHolidayEvents(Long userId,List<String> indicatorOptions) {
		return eventRepository.findByCreatedByIdAndScheduleAs(userId,indicatorOptions);
	}
	
	public List<Event> findEventsByStartDateAndUserId(Long createdById) {
		return eventRepository.findByCreatedByIdAndStartDateOnlyAndEventMemberStatus(createdById);
	}
	
	public void updateEventMemberStatus(String status,Long eventMemberId) {
		eventMemberRepository.updateEventMemberForStatusByEventMemberId(status, eventMemberId);
	}
	
	public Event findEventById(Long eventId) {
		return eventRepository.findOne(eventId);
	}
	
	public List<Event> findUserPastGatherings() {
		return eventRepository.findPastUserGatherings();
	}
	
	public List<Event> findUserFutureGatherings(Long userId,String status) {
		//return eventRepository.findFutureGatherings(userId, status);
		return eventServiceDao.findGatheringsByUserIdAndStatus(userId, status);
	}
	public List<Event> findEventByEventIds(List<Long> eventIds) {
		return eventRepository.findByEventIds(eventIds);
	}
	
	public CenesProperty findCenesPropertyByNameAndOwner(String name,PropertyOwningEntity propertyOwner) {
		return cenesPropertyRepository.findByNameAndPropertyOwner(name, propertyOwner);
	}
	
	public CenesPropertyValue saveCenesPropertyValue(CenesPropertyValue cenesPropertyValue) {
		return cenesPropertyValueRepository.save(cenesPropertyValue);
	}
	
	public List<EventTimeSlot> findEventTimeSlotByEventDateAndUserId(long startTime,long endTime,Long userId) {
		return eventTimeSlotRepository.findByStartAndEndTimeAndUserId(startTime,endTime,userId);
	}
	
	public List<EventTimeSlot> findEventTimeSlotByEventDateAndEventId(long startTime,long endTime,Long eventId) {
		return eventTimeSlotRepository.findByStartAndEndTimeAndEventId(startTime,endTime,eventId);
	}
	
	public void deleteEventsByCreatedByIdAndSource(Long createdById,String source) {
		eventRepository.deleteEventsByCreatedByIdAndSource(createdById, source);
	}
	public void deleteEventsByCreatedByIdAndSourceAndScheduleAs(Long createdById,String source,String scheduleAs) {
		eventRepository.deleteEventsByCreatedByIdAndSourceAndScheduleAs(createdById, source,scheduleAs);
	}	
	
	public void deleteEventsByCreatedById(Long createdById) {
		eventRepository.deleteByCreatedById(createdById);
	}
	
	public EventMember findEventMemeberByEventMemberId(Long eventMemberId) {
		return eventMemberRepository.findOne(eventMemberId);
	}
	
	public void updateEventMemberPictureByUserId(String picture,Long userId) {
		eventMemberRepository.updateEventMemberForPictureByEventMemberId(picture,userId);
	}
	
	public void deleteEventTimeSlotsByEventId(Long eventId) {
		eventTimeSlotRepository.deleteByEventId(eventId);
	}
	
	public CalendarSyncToken findCalendarSyncTokenByUserIdAndAccountType(Long userId, CalendarSyncToken.AccountType accountType) {
		return calendarSyncTokenRepository.findByUserIdAndAccountType(userId, accountType);
	}
	
}
