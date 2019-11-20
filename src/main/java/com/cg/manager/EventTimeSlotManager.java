package com.cg.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.cg.events.bo.Event;
import com.cg.events.bo.Event.EventProcessedStatus;
import com.cg.events.bo.EventMember;
import com.cg.events.bo.EventTimeSlot;
import com.cg.events.bo.EventTimeSlot.TimeSlotStatus;
import com.cg.events.repository.EventMemberRepository;
import com.cg.events.repository.EventRepository;
import com.cg.events.repository.EventTimeSlotRepository;
import com.cg.service.EventService;
import com.cg.utils.CenesUtils;

@Service
public class EventTimeSlotManager {

	@Autowired
	EventRepository eventRepository;
	
	@Autowired
	EventMemberRepository eventMemberRepository;
	
	@Autowired
	EventTimeSlotRepository eventTimeSlotRepository;
	
	@Autowired
	EventService eventService;
	
	public List<Event> findEventsToProcessed() {
		Pageable pageable = new PageRequest(0, 100);
		List<Event> events = eventRepository.findByEventProcessedOrNot(EventProcessedStatus.UnProcessed.ordinal(),pageable);
		for (Event event : events) {
			event.setProcessed(EventProcessedStatus.Waiting.ordinal());
			eventRepository.save(event);
		}
		return events;
	}
	
	public List<Event> findEventMembersToProcessed() {
		List<Event> events= eventRepository.findByEventMemberUnProcessedAndStatus(EventProcessedStatus.UnProcessed.ordinal(),EventMember.MemberStatus.Going.toString());
		return events;
	}
	
	/**
	 * Lets save user event info in 15 minute slots mentioning which event is
	 * free and which are booked
	 * */
	public List<Event> saveEventsInSlots(List<Event> events) {
		long startTime = new Date().getTime();
		System.out.println("saveEventsInSlots STARTS");
		try {
			for (Event event : events) {
				List<EventTimeSlot> eventTimeSlots = getTimeSlots(event,event.getCreatedById());
				eventTimeSlotRepository.save(eventTimeSlots);
				
				//Releasing space allocated to time slots list
				eventTimeSlots = null;

				event.setProcessed(EventProcessedStatus.Processed.ordinal());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		long endTime = new Date().getTime();
		System.out.println("Matching Slots ENDS : Time in saving datatbase : "+(endTime - startTime)/1000);
		return events;
	}
	
	public List<EventTimeSlot> getTimeSlots(Event event,Long userId) {
		
		System.out.println("Start Time : "+event.getStartTime()+", End Time : "+event.getEndTime());
		
		List<EventTimeSlot> eventTimeSlots = new ArrayList<>();
		Long eventDayStartTimeValue = CenesUtils.getStartOfDay(event.getStartTime()).getTime();
		Long eventDayEndTimeValue = null;
		try {
			eventDayEndTimeValue = CenesUtils.getEndOfDay(event.getEndTime()).getTime();
		} catch(Exception e) {
			System.out.println("Exception : getTimeSlots() : eventDayEndTimeValue : "+ e.getMessage());
			eventDayEndTimeValue = eventDayStartTimeValue;
		}
		 		
		int minutesToAdd = 5;

		// Lets divide the events start and end time duration into slots
		// of 15 minutes.
		// We will then use this list to compare with event creator time
		List<Long> eventStarEndTimeInSlots = CenesUtils.divideTimeIntoMinuteSlots(event.getStartTime(),event.getEndTime(), minutesToAdd);
		if (eventStarEndTimeInSlots.size() == 0) {
			return eventTimeSlots;
		}
		
		// Now we need to create 15 minutes slots for the whole day
		// And we will check if any slot is booked and mark it as booked
		// Otherwise free slot
		
		/*List<EventTimeSlot> eventTimeSlotsFromDb = eventService.findEventTimeSlotByEventDateAndUserId(event.getStartTime().getTime(),event.getEndTime().getTime(),event.getCreatedById());
		List<Long> eventStarEndTimeInSlotsUsed = new ArrayList<>();
		
		if (eventTimeSlotsFromDb != null && eventTimeSlotsFromDb.size() > 0) {
			for (EventTimeSlot eventTimeSlot : eventTimeSlotsFromDb) {
				if (eventStarEndTimeInSlots.contains(eventTimeSlot.getStartTime())) {
					eventTimeSlot.setStatus(TimeSlotStatus.Booked.toString());
					eventStarEndTimeInSlots.remove(eventTimeSlot.getStartTime());
					eventStarEndTimeInSlotsUsed.add(eventTimeSlot.getStartTime());
				}
			}
			//event.setProcessed(EventProcessedStatus.Processed.ordinal());
			
			eventTimeSlotRepository.save(eventTimeSlotsFromDb);
		} else {
		if (eventStarEndTimeInSlots.size() > 0) {*/
			while (eventDayStartTimeValue < eventDayEndTimeValue) {
				
				try {
					long incrementedTime = 0l;
					incrementedTime = CenesUtils.getDateAfterAddingMinutes(eventDayStartTimeValue, minutesToAdd);

					
						EventTimeSlot eventTimeSlot = new EventTimeSlot();
						eventTimeSlot.setStartTime(eventDayStartTimeValue);

						// It is time to get the slot time by passing current date
						// and minutes to add
						// So we will start from Day's start time upto end of the
						// day
						// We will check for each timeslot created if its booked or
						// free and mark
						// them as well
						/*if (eventStarEndTimeInSlotsUsed.contains(eventDayStartTimeValue)) {
							eventDayStartTimeValue = incrementedTime;
							continue;
						}*/
						
						if (eventStarEndTimeInSlots.contains(eventDayStartTimeValue)) {
							eventTimeSlot.setStatus(TimeSlotStatus.Booked.toString());
						} else {
							//eventTimeSlot.setStatus(TimeSlotStatus.Free.toString());
							eventDayStartTimeValue = incrementedTime;
							continue;
						}

						//System.out.println(event.toString());
						eventTimeSlot.setSource(event.getSource());
						eventTimeSlot.setScheduleAs(event.getScheduleAs());
						eventTimeSlot.setEndTime(incrementedTime);
						eventTimeSlot.setEventStartTime(event.getStartTime());
						try {
							if (event.getRecurringEventId() != null) {
								eventTimeSlot.setRecurringEventId(Long.parseLong(event.getRecurringEventId()));
							}
						} catch(Exception e) {
							System.out.println("Exception : getTimeSlots() : eventTimeSlot.setRecurringEventId : " + e.getMessage());
						}

						
						String eventSlotDateOnlyWithNoTime = CenesUtils.yyyyMMddTHHmmss.format(new Date(eventDayStartTimeValue));
						eventTimeSlot.setEventDate(CenesUtils.yyyyMMddTHHmmss.parse(eventSlotDateOnlyWithNoTime));

						eventTimeSlot.setUserId(userId);
						eventTimeSlot.setEventId(event.getEventId());
						eventDayStartTimeValue = incrementedTime;
						if (eventDayStartTimeValue != eventDayEndTimeValue) {
							eventTimeSlots.add(eventTimeSlot);
						}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		//}
			//Releasing space occupied by List
			eventStarEndTimeInSlots = null;
			return eventTimeSlots;
	}
	
	
	public List<Event> saveEventMemberSlots(List<Event> events) {
		System.out.println("saveEventMemberSlots STARTS");
		System.out.println("Events List : "+events.size());
		try {
			for (Event event : events) {
				
				System.out.println("Event Details : Event Members Size "+event.getEventMembers().size());
				
				for (EventMember eventMember : event.getEventMembers()) {
					if (eventMember.getProcessed() == Event.EventProcessedStatus.Processed.ordinal()) {
						continue;
					}
					List<EventTimeSlot> eventTimeSlots = getTimeSlots(event,eventMember.getUserId());
					if (eventTimeSlots != null && eventTimeSlots.size() > 0) {
						eventTimeSlotRepository.save(eventTimeSlots);
					}
					
					//Releasing space allocated to time slots list
					eventTimeSlots = null;
					
					eventMember.setProcessed(Event.EventProcessedStatus.Processed.ordinal());
					eventMemberRepository.save(eventMember);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("saveEventMemberSlots END");
		return events;
	}
	
	public void deleteEventTimeSlotsByUserIdScheduleAs(Long createdById,String scheduleAs) {
		eventTimeSlotRepository.deleteByUserIdAndScheduleAs(createdById, scheduleAs);
	}
	
	public void deleteEventTimeSlotsByUserIdSource(Long createdById,String source) {
		eventTimeSlotRepository.deleteByUserIdAndSource(createdById, source);
	}
	public void deleteEventTimeSlotsByUserIdSourceScheduleAs(Long createdById,String source,String scheduleAs) {
		eventTimeSlotRepository.deleteByUserIdAndSourceAndScheduleAs(createdById, source,scheduleAs);
	}
	
	public void deleteEventTimeSlotsByStartTimeGreaterThanAndUserIdAndSourceAndScheduleAs(Long startTime, Long createdById,String source,String scheduleAs) {
		eventTimeSlotRepository.deleteByStartTimeGreaterThanAndUserIdAndSourceAndScheduleAs(startTime, createdById, source,scheduleAs);
	}
	
	public void deleteEventTimeSlotsByUserId(Long userId) {
		eventTimeSlotRepository.deleteByUserId(userId);
	}
	
	public void deleteEventTimeSlotsByRecurringEventId(Long recurringEventId) {
		eventTimeSlotRepository.deleteByRecurringEventId(recurringEventId);
	}
	
	public void deleteEventTimeSlotsByEventId(Long eventId) {
		eventTimeSlotRepository.deleteByEventId(eventId);
	}
}
