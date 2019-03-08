package com.cg.manager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.primefaces.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cg.bo.CalendarSyncToken;
import com.cg.bo.CalendarSyncToken.AccountType;
import com.cg.bo.CenesProperty;
import com.cg.bo.CenesProperty.PropertyOwningEntity;
import com.cg.bo.CenesPropertyValue;
import com.cg.bo.GatheringPreviousLocation;
import com.cg.bo.Member;
import com.cg.bo.Member.MemberType;
import com.cg.constant.CgConstants.ErrorCodes;
import com.cg.dto.HomeScreenDto;
import com.cg.dto.LocationDto;
import com.cg.events.bo.Event;
import com.cg.events.bo.Event.EventProcessedStatus;
import com.cg.events.bo.Event.EventSource;
import com.cg.events.bo.Event.ScheduleEventAs;
import com.cg.events.bo.EventMember;
import com.cg.events.bo.EventMember.MemberStatus;
import com.cg.events.bo.EventTimeSlot;
import com.cg.events.bo.EventTimeSlot.TimeSlotStatus;
import com.cg.events.bo.FacebookEventItem;
import com.cg.events.bo.FacebookEvents;
import com.cg.events.bo.GoogleEventAttendees;
import com.cg.events.bo.GoogleEventItem;
import com.cg.events.bo.GoogleEvents;
import com.cg.events.bo.OutlookEventAttendees;
import com.cg.events.bo.OutlookEventItem;
import com.cg.events.bo.OutlookEvents;
import com.cg.events.dao.EventServiceDao;
import com.cg.events.repository.EventMemberRepository;
import com.cg.events.repository.EventRepository;
import com.cg.events.repository.GatheringPreviousLocationRepository;
import com.cg.reminders.bo.Reminder;
import com.cg.reminders.bo.ReminderMember;
import com.cg.repository.CalendarSyncTokenRepository;
import com.cg.repository.ReminderRepository;
import com.cg.service.EventService;
import com.cg.service.FacebookService;
import com.cg.service.GoogleService;
import com.cg.service.OutlookService;
import com.cg.service.UserService;
import com.cg.threads.EventThread;
import com.cg.user.bo.User;
import com.cg.utils.CenesUtils;

import okhttp3.internal.framed.ErrorCode;

@Service
public class EventManager {
	
	@Autowired
	EventService eventService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	NotificationManager notificationManager;
	
	@Autowired
	ReminderRepository reminderRepository;
	
	@Autowired
	EventRepository eventRepository;
	
	@Autowired
	CalendarSyncTokenRepository refreshTokenRepository;
	
	@Autowired
	EventTimeSlotManager eventTimeSlotManager;
	
	@Autowired
	EventThread eventThread;
	
	@Autowired
	EventServiceDao eventServiceDao;
	
	@Autowired
	EventMemberRepository eventMemberRepository;
	
	@Autowired
	GatheringPreviousLocationRepository gatheringPreviousLocationRepository;
	
	
	public Event saveUpdateEvent(Event event) {
		return eventService.saveEvent(event);
	}
	
	public GatheringPreviousLocation saveUpdateGatheringPreviousLocation(GatheringPreviousLocation gatheringPreviousLocation) {
		return gatheringPreviousLocationRepository.save(gatheringPreviousLocation);
	}
	
	public GatheringPreviousLocation findGatheringPreviousLocationByEventId(Long eventId) {
		return gatheringPreviousLocationRepository.findByEventId(eventId);
	}
	
	public GatheringPreviousLocation findGatheringPreviousLocationByUserIdAndPlaceId(Long userId, String placeId) {
		return gatheringPreviousLocationRepository.findByUserIdAndPlaceId(userId, placeId);
	}
	
	public Event createEvent(Event event) {

		Boolean ownerExists = false;
		if (event.getEventMembers() != null) {
			for (EventMember member : event.getEventMembers()) {
				if (member.getUserId() != null && event.getCreatedById().equals(member.getUserId())) {
					ownerExists = true;
					break;
				}
			}
		}
		if (!ownerExists) {
			User user = userService.findUserById(event.getCreatedById());
	
			List<EventMember> members = null;
			if (event.getEventMembers() != null) {
				members = event.getEventMembers();
			} else {
				members = new ArrayList<>();
			}
			
			for (EventMember mem: members) {
				mem.setUser(null);
			}
			
			EventMember eventMember = new EventMember();
			eventMember.setName(user.getName());
			eventMember.setPicture(user.getPhoto());
			eventMember.setStatus("Going");
			eventMember.setUserId(user.getUserId());
			members.add(eventMember);
			event.setEventMembers(members);
		}
		
		
		if (event.getEventId() != null) {
			for (EventMember eventMem: event.getEventMembers()) {
				eventMem.setProcessed(Event.EventProcessedStatus.UnProcessed.ordinal());
			}
		}
		if (event.getPredictiveData() != null) {
	        try{
	        	String validatedPropertiesJsonString = event.getPredictiveData().replaceAll("\\\"","\"");
	        	event.setPredictiveData(validatedPropertiesJsonString);
	        }
	        catch(Exception ex){
	        	System.out.println(ex);
	        }
		}
		
		System.out.println("Before Saving : "+event.toString());
		event = eventService.saveEvent(event);

		System.out.println("After Saving : "+event.toString());
		notificationManager.sendGatheringNotification(event);
		
		System.out.println("[CreateEvent : "+new Date()+", ENDS]");
		// saveEventsInSlots(event);
		return event;
	}
	
	public List<HomeScreenDto> getEventsAndRemindersMergedDataByUserIdStartDateEndDate(Long userId,Date startDate,Date endDate) {
		List<HomeScreenDto> responseDataToSend = new ArrayList<>();
		
		User user = userService.findUserById(userId);
		
		Map<String,List<HomeScreenDto>> homeScreenDataMap = new HashMap<>();
		//List<Event> events = eventRepository.findByCreatedByIdAndStartDateAndEventMemberStatus(userId, startDate, endDate);
		List<Event> events = eventServiceDao.findByCreatedByIdAndStartDateAndEventMemberStatus(userId, CenesUtils.yyyyMMddTHHmmss.format(startDate), CenesUtils.yyyyMMddTHHmmss.format(endDate));
		if (events != null && events.size() > 0) {
			for (Event event : events) {
				String dateKey = CenesUtils.yyyyMMdd.format(event.getStartTime());
				List<HomeScreenDto> homeScreenDtos = null;
				if (homeScreenDataMap.containsKey(dateKey)) {
					homeScreenDtos = homeScreenDataMap.get(dateKey);
				} else {
					homeScreenDtos = new ArrayList<>();
				}
				
				HomeScreenDto homeScreenDto =  new HomeScreenDto();
				homeScreenDto.setId(event.getEventId());
				homeScreenDto.setTitle(event.getTitle());
				homeScreenDto.setDescription(event.getDescription());
				homeScreenDto.setPicture(event.getEventPicture());
				if (event.getStartTime() != null) {
					homeScreenDto.setStartTime(event.getStartTime().getTime());
				}
				if (event.getEndTime() != null) {
					homeScreenDto.setEndTime(event.getEndTime().getTime());
				}
				if (event.getFullDayStartTime() != null) {
					homeScreenDto.setFullDayStartTime(event.getFullDayStartTime());
				}
				homeScreenDto.setScheduleAs(event.getScheduleAs());
				homeScreenDto.setLocation(event.getLocation());
				homeScreenDto.setSource(event.getSource());
				homeScreenDto.setCreatedById(event.getCreatedById());
				homeScreenDto.setType("Event");
				homeScreenDto.setIsFullDay(event.getIsFullDay());
				if (event.getEventMembers() != null && event.getEventMembers().size() > 0) {
					List<Member> members = new ArrayList<>();
					for (EventMember eventMember : event.getEventMembers()) {
						if (eventMember.getUserId() != null && eventMember.getUserId().equals(event.getCreatedById())) {
							Member member = new Member();
							member.setName(eventMember.getName());
							member.setStatus(eventMember.getStatus());
							member.setType(MemberType.Event.toString());
							member.setTypeId(eventMember.getEventId());
							member.setUserId(eventMember.getUserId());
							member.setPicture(user.getPhoto());
							if (eventMember.getUser() != null) {
								member.setUser(eventMember.getUser());
							}
							member.setOwner(true);
							member.setMemberId(eventMember.getEventMemberId());
							members.add(member);
							break;
						}
					}
					for (EventMember eventMember : event.getEventMembers()) {
						if (eventMember.getUserId() != null && !eventMember.getUserId().equals(event.getCreatedById())) {
							Member member = new Member();
							member.setName(eventMember.getName());
							member.setPicture(eventMember.getPicture());
							member.setStatus(eventMember.getStatus());
							member.setType(MemberType.Event.toString());
							member.setTypeId(eventMember.getEventId());
							member.setUserId(eventMember.getUserId());
							member.setMemberId(eventMember.getEventMemberId());
							if (eventMember.getUser() != null) {
								member.setUser(eventMember.getUser());
							}
							members.add(member);
						} else if (eventMember.getUserId() == null) {
							Member member = new Member();
							member.setName(eventMember.getName());
							member.setPicture(eventMember.getPicture());
							member.setStatus(eventMember.getStatus());
							member.setType(MemberType.Event.toString());
							member.setTypeId(eventMember.getEventId());
							member.setUserId(eventMember.getUserId());
							member.setMemberId(eventMember.getEventMemberId());
							if (eventMember.getUser() != null) {
								member.setUser(eventMember.getUser());
							}
							members.add(member);
						}
					}
					homeScreenDto.setMembers(members);
				}
				
				homeScreenDtos.add(homeScreenDto);
				homeScreenDataMap.put(dateKey, homeScreenDtos);
			}
		}
		
		List<Reminder> reminders = reminderRepository.findAllRemindersByAcceptedReminderMemberStatusAsc(userId, endDate);
		if (reminders != null && reminders.size() > 0) {
			for (Reminder reminder : reminders) {
				String dateKey = null;
				if (reminder.getReminderTime() != null) {
					dateKey = CenesUtils.yyyyMMdd.format(reminder.getReminderTime());
				}
				List<HomeScreenDto> homeScreenDtos = null;
				if (homeScreenDataMap.containsKey(dateKey)) {
					homeScreenDtos = homeScreenDataMap.get(dateKey);
				} else {
					homeScreenDtos = new ArrayList<>();
				}
				
				HomeScreenDto homeScreenDto =  new HomeScreenDto();
				homeScreenDto.setId(reminder.getReminderId());
				homeScreenDto.setTitle(reminder.getTitle());
				if (reminder.getReminderTime() != null) {
					homeScreenDto.setStartTime(reminder.getReminderTime().getTime());
				}
				homeScreenDto.setLocation(reminder.getLocation());
				homeScreenDto.setSource("Cenes");
				homeScreenDto.setCreatedById(reminder.getCreatedById());
				if (reminder.getReminderMembers() != null && reminder.getReminderMembers().size() > 0) {
					List<Member> members = new ArrayList<>();
					for (ReminderMember reminderMember : reminder.getReminderMembers()) {
						Member member = new Member();
						member.setName(reminderMember.getName());
						member.setPicture(reminderMember.getPicture());
						member.setStatus(reminderMember.getStatus());
						member.setType(MemberType.Reminder.toString());
						member.setTypeId(reminderMember.getReminderId());
						member.setUserId(reminderMember.getMemberId());
						member.setMemberId(reminderMember.getReminderMemberId());
						members.add(member);
					}
					homeScreenDto.setMembers(members);
				}
				homeScreenDto.setType("Reminder");
				homeScreenDtos.add(homeScreenDto);
				homeScreenDataMap.put(dateKey, homeScreenDtos);
			}
		}
		
		Map<String,List<HomeScreenDto>> sortedMap = new LinkedHashMap<>();
		if (homeScreenDataMap.size() > 0) {
			List<String> keysList = new ArrayList<>();
			for (String key : homeScreenDataMap.keySet()) {
				keysList.add(key);
			}
			
			Comparator<String> cmp = new Comparator<String>() {
		        public int compare(String o1, String o2) {
		        	if (o1 == null) {
		        		return -1;
		        	} else if (o2 == null) {
		        		return 1;
		        	} else {
		        		return o1.compareTo(o2);
		        	}
		            
		        }
		    };
		    Collections.sort(keysList, cmp);
			
			for (String key : keysList)  {
				sortedMap.put(key, homeScreenDataMap.get(key));
			}
		}

		
		for (Entry<String,List<HomeScreenDto>> entryMap : sortedMap.entrySet()) {
			responseDataToSend.addAll(entryMap.getValue());
		}
	
	return responseDataToSend;
	}

	public void deleteEventsByCreatedByIdSource(Long createdById,String source) {
		eventService.deleteEventsByCreatedByIdAndSource(createdById, source);
	}
	public void deleteEventsByCreatedByIdSourceScheduleAs(Long createdById,String source,String scheduleAs) {
		eventService.deleteEventsByCreatedByIdAndSourceAndScheduleAs(createdById, source,scheduleAs);
	}
	
	public void deleteEventsByCreatedByIdScheduleAs(Long createdById,String scheduleAs) {
		eventRepository.deleteEventsByCreatedByIdAndScheduleAs(createdById, scheduleAs);
	}
	
	public void deleteEventsByCreatedById(Long createdById) {
		eventService.deleteEventsByCreatedById(createdById);
	}
	
	public void deleteEventsByRecurringEventId(Long recurringEventId) {
		this.eventRepository.deleteByRecurringEventId(String.valueOf(recurringEventId));
	}
	
	public List<Event> syncFacebookEvents(String facebookId,String accessToken,User user) {
		List<Event> events = new ArrayList<>();
		
		FacebookService fs = new FacebookService();
		FacebookEvents facebookEvents = fs.facebookEvents(facebookId,
				accessToken);
		for (FacebookEventItem eventItem : facebookEvents.getData()) {

			List<Event> dbevents = this.eventRepository.findBySourceEventIdAndCreatedById(eventItem.getId(), user.getUserId());
			Event event = null;
			if (dbevents == null || dbevents.size() == 0) {
				event = new Event();
			} else {
				event = dbevents.get(0);
			}
			event.setCreatedById(user.getUserId());
			event.setSourceUserId(facebookId);
			event.setSourceEventId(eventItem.getId());
			event.setTitle(eventItem.getName());
			if (eventItem.getDescription() != null) {
				event.setDescription(eventItem.getDescription());
			}
			event.setScheduleAs(ScheduleEventAs.Event.toString());
			event.setSource(EventSource.Facebook.toString());
			event.setTimezone(eventItem.getTimezone());

			if (eventItem.getStart_time() != null) {
				try {
					Date startDate = CenesUtils.yyyyMMddTHHmmssX.parse(eventItem
							.getStart_time());
					String startDateStr = CenesUtils.yyyyMMddTHHmmss.format(startDate);
					event.setStartTime(CenesUtils.yyyyMMddTHHmmss.parse(startDateStr));

					if (eventItem.getEnd_time() != null) {
						Date endDate = CenesUtils.yyyyMMddTHHmmssX.parse(eventItem
								.getEnd_time());
						String endDateStr = CenesUtils.yyyyMMddTHHmmss.format(endDate);
						event.setEndTime(CenesUtils.yyyyMMddTHHmmss.parse(endDateStr));
					} else {
						event.setEndTime(CenesUtils.yyyyMMddTHHmmss.parse(startDateStr));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (eventItem.getPlace() != null
					&& eventItem.getPlace().containsKey("name")) {
				event.setLocation((String) eventItem.getPlace().get("name"));
			}
			if (eventItem.getPicture() != null
					&& ((Map<String, Object>) eventItem.getPicture())
							.containsKey("data")) {
				Map<String, String> eventPicture = (Map<String, String>) eventItem
						.getPicture().get("data");
				event.setEventPicture(eventPicture.get("url"));
			} /*else {
				event.setEventPicture("http://cenes.test2.redblink.net/assets/default_images/default_event_image.png");
			}*/
			List<EventMember> members = new ArrayList<>();
			if (eventItem.getAttending() != null) {
				List<Map<String, String>> attendees = (List) eventItem
						.getAttending().get("data");
				members.addAll(parseFacebookEventMembers(attendees,facebookId,user.getUserId()));
			}
			if (eventItem.getMaybe() != null) {
				List<Map<String, String>> attendees = (List) eventItem
						.getMaybe().get("data");
				members.addAll(parseFacebookEventMembers(attendees,facebookId,user.getUserId()));
			}
			if (eventItem.getDeclined() != null) {
				List<Map<String, String>> attendees = (List) eventItem
						.getDeclined().get("data");
				members.addAll(parseFacebookEventMembers(attendees,facebookId,user.getUserId()));
			}
			if (event.getEventMembers() != null && event.getEventMembers().size() > 0) {
				event.getEventMembers().clear();
				event.getEventMembers().addAll(members);
			} else {
				event.setEventMembers(members);
			}
			event.setProcessed(EventProcessedStatus.UnProcessed.ordinal());
			events.add(event);
		}
		return eventRepository.save(events);
	}
	
	public List<EventMember> parseFacebookEventMembers(
			List<Map<String, String>> attendees,String facebookId,Long userId) {
		List<EventMember> members = new ArrayList<>();
		for (Map<String, String> attendee : attendees) {
			EventMember eventMember = new EventMember();
			eventMember.setName(attendee.get("name"));
			eventMember.setPicture("https://graph.facebook.com/v2.10/"
					+ attendee.get("id")
					+ "/picture?height=200&type=normal&width=200");
			if (attendee.get("rsvp_status").equals("attending")) {
				eventMember.setStatus(MemberStatus.Going.toString());
			} else if (attendee.get("rsvp_status").equals("maybe")) {
				eventMember.setStatus(MemberStatus.Maybe.toString());
			} else if (attendee.get("rsvp_status").equals("declined")) {
				eventMember.setStatus(MemberStatus.NotGoing.toString());
			}
			eventMember.setSource(EventSource.Facebook.toString());
			eventMember.setSourceId(attendee.get("id"));
			if (facebookId.equals(attendee.get("id"))) {
				eventMember.setUserId(userId);
			} else {
				User user = userService.findUserByFacebookId(facebookId);
				if (user != null) {
					eventMember.setUserId(user.getUserId());
				}
			}
			members.add(eventMember);
		}
		return members;
	}

	public List<Event> syncGoogleEvents(boolean isNextSyncRequest, String accessToken,User user) {
		List<Event> events = new ArrayList<>();
		
		try {
			GoogleService gs = new GoogleService();
			List<GoogleEvents> googleEventsCalendarList = gs.getCalenderEvents(isNextSyncRequest,accessToken);
			if (googleEventsCalendarList != null
					&& googleEventsCalendarList.size() > 0) {
				for (GoogleEvents googleEvents : googleEventsCalendarList) {
					if (googleEvents.getItems() != null
							&& googleEvents.getItems() != null
							&& googleEvents.getItems().size() > 0) {
						for (GoogleEventItem eventItem : googleEvents.getItems()) {
							if ("cancelled".equals(eventItem.getStatus())) {
								continue;
							}
							
							//Lets check first if the creator Exists in our DB or Not.
							//If it exists in db then we will set the created by id as its user id
							//Otherewise we can make the syncing user as creator.
							User creatorExistsInDb = userService.findUserByEmail(eventItem.getCreatorEmail());
							if (creatorExistsInDb != null) {
								user = creatorExistsInDb;
							}
							
							Event event = null;
							System.out.println("Event Title : "+eventItem.getSummary());
							System.out.println("[Event : "+eventItem.toString()+" ]");
							/*if (isUserInvitee) {
								List<Event> eventsTemp = this.eventRepository.findBySourceEventId(eventItem.getId());
								if (eventsTemp != null && eventsTemp.size() > 0) {
									userId = eventsTemp.get(0).getCreatedById();
								}
							}*/
							//if (event == null) {
							
							//Now check if creator has already synced the calendar, then there will be an event already existing.
							//We will fetch that event by created by id and google event id.
							//If creator has not synced the event then we will create new event. 
							//If creator is different then he will see the event without syncing the google calendar.
							List<Event> dbevents = this.eventRepository.findBySourceEventIdAndCreatedById(eventItem.getId(), user.getUserId());
							if (dbevents == null || dbevents.size() == 0) {
								event = new Event();
							} else {
								event = dbevents.get(0);
							}
							//}
							event.setSourceEventId(eventItem.getId());
							event.setSource(EventSource.Google.toString());
							event.setTitle(eventItem.getSummary());
							event.setCreatedById(user.getUserId());
							if (eventItem.getDescription() != null) {
								event.setDescription(eventItem.getDescription());
							}
							//event.setEventPicture("http://cenes.test2.redblink.net/assets/default_images/default_event_image.png");
							if (eventItem.getLocation() != null) {
								event.setLocation(eventItem.getLocation());
							}

							event.setScheduleAs(ScheduleEventAs.Event.toString());
							event.setTimezone(googleEvents.getTimeZone());
							if (eventItem.getStart() != null) {
								Date startDate = null;
								if (eventItem.getStart().containsKey("dateTime")) {
									startDate = CenesUtils.yyyyMMddTHHmmssX.parse((String) eventItem.getStart().get("dateTime"));
								} else if (eventItem.getStart().containsKey("date")) {
									//Events with no hours and minutes
									//We will mark them full day events.
									startDate = CenesUtils.yyyyMMdd.parse((String) eventItem.getStart().get("date"));
									event.setIsFullDay(true);
								}
								if (startDate != null) {
									String startDateStr = CenesUtils.yyyyMMddTHHmmss.format(startDate);
									event.setStartTime(CenesUtils.yyyyMMddTHHmmss.parse(startDateStr));
								}
							} else {
								event.setStartTime(new Date());
							}
							
							if (eventItem.getEnd() != null) {
								Date endDate = null;
								if (eventItem.getEnd().containsKey("dateTime")) {
									endDate = CenesUtils.yyyyMMddTHHmmssX.parse((String) eventItem.getEnd().get("dateTime"));
								} else if (eventItem.getEnd().containsKey("date")) {
									endDate = CenesUtils.yyyyMMdd.parse((String) eventItem.getEnd().get("date"));
								}
								if (endDate != null) {
									String endDateStr = CenesUtils.yyyyMMddTHHmmss.format(endDate);
									event.setEndTime(CenesUtils.yyyyMMddTHHmmss.parse(endDateStr));
								}
							} else {
								event.setStartTime(new Date());
							}
							
							//Lets populate the Invitees.
							List<EventMember> eventMembersTemp  = new ArrayList<>();
							if (eventItem.getAttendees() != null && eventItem.getAttendees().size() > 0) {
								eventMembersTemp = parseGoogleEventMembers(eventItem.getAttendees());
							} 
							
							//Now we need to check if creator has invited himself in the event.
							//if not then we will add him in the invitee list
							boolean isUserInvitee = false;
							if (eventMembersTemp != null && eventMembersTemp.size() > 0) {
								for (EventMember attendeeMember : eventMembersTemp) {
									if (attendeeMember.getUserId() != null && attendeeMember.getUserId().equals(user.getUserId())) {
										isUserInvitee = true;
										break;
									}
								}
							}
							if (!isUserInvitee) {
								EventMember eventMember = new EventMember();
								eventMember.setName(user.getName());
								eventMember.setStatus(MemberStatus.Going.toString());
								eventMember.setSource(EventSource.Google.toString());
								if (user.getEmail() != null) {
									eventMember.setSourceEmail(user.getEmail());
								}
								
								eventMember.setUserId(user.getUserId());
								eventMembersTemp.add(eventMember);
							}
							
							//}
							
							//Clearing old users and adding new one.
							if (event.getEventMembers() != null && event.getEventMembers().size() > 0) {
								event.getEventMembers().clear();
								event.getEventMembers().addAll(eventMembersTemp);
							} else {
								event.setEventMembers(eventMembersTemp);
							}
							
							if (eventItem.getRecurringEventId() != null) {
								event.setRecurringEventId(eventItem.getRecurringEventId());
							}
							event.setProcessed(EventProcessedStatus.UnProcessed.ordinal());
							events.add(event);
						}
						this.eventRepository.save(events);
						System.out.println("[ Syncing Google Events - User Id : "
										+ user.getUserId() + ", Total Events to Sync : "
										+ events.size() + "]");
						
					} else {
						/*if (googleEvents.getErrorDetail() != null) {
							Event event = new Event();
							event.setErrorCode(googleEvents.getErrorCode());
							event.setErrorDetail(googleEvents.getErrorDetail());
							events.add(event);
						}*/
					}
				}
			}
			CenesProperty cenesProperty = eventService.findCenesPropertyByNameAndOwner("google_calendar", PropertyOwningEntity.User);
			if (cenesProperty != null) {
				CenesPropertyValue cenesPropertyValue = new CenesPropertyValue();
				cenesPropertyValue.setCenesProperty(cenesProperty);
				cenesPropertyValue.setDateValue(new Date());
				cenesPropertyValue.setEntityId(user.getUserId());
				cenesPropertyValue.setOwningEntity(PropertyOwningEntity.User);
				cenesPropertyValue.setValue("true");
				eventService.saveCenesPropertyValue(cenesPropertyValue);
			}
			return events;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<EventMember> parseGoogleEventMembers(
			List<GoogleEventAttendees> attendees) {
			List<EventMember> members = new ArrayList<>();
			for (GoogleEventAttendees attendee : attendees) {
				EventMember eventMember = new EventMember();
				if (attendee.getDisplayName() != null) {
					eventMember.setName(attendee.getDisplayName());
				}
				if (attendee.getResponseStatus().equals("accepted")) {
					eventMember.setStatus(MemberStatus.Going.toString());
				} else if (attendee.getResponseStatus().equals("needsAction")) {
					eventMember.setStatus(MemberStatus.Going.toString());
				} else if (attendee.getResponseStatus().equals("declined")) {
					eventMember.setStatus(MemberStatus.NotGoing.toString());
				}
				eventMember.setSource(EventSource.Google.toString());
				eventMember.setSourceEmail(attendee.getEmail());
				
				//if (attendee.getSelf() && attendee.getOrganizer()) {
					//eventMember.setUserId(userId);
				//} else {
					User user = userService.findUserByEmail(attendee.getEmail());
					if (user != null) {
						eventMember.setUserId(user.getUserId());
						eventMember.setName(user.getName());
						eventMember.setPicture(user.getPhoto());
					}
				//}
				members.add(eventMember);
			}
			return members;
		}
	
	public List<Event> populateOutlookEventsInCenes(List<OutlookEvents> outlookEventList,User user) {
		List<Event> events = new ArrayList<>();

		try {
			SimpleDateFormat outlookFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			for (OutlookEvents outlookEvents : outlookEventList) {
				if (outlookEvents.getValue() != null && outlookEvents.getValue().size() > 0) {
					for (OutlookEventItem eventItem : outlookEvents.getValue()) {
						List<Event> dbevents = this.eventRepository.findBySourceEventIdAndCreatedById(eventItem.getId(), user.getUserId());
						Event event = null;
						if (dbevents == null || dbevents.size() == 0) {
							event = new Event();
						} else {
							event = dbevents.get(0);
						}
						
						event.setSourceEventId(eventItem.getId());
						event.setSource(EventSource.Outlook.toString());
						event.setTitle(eventItem.getSubject());
						event.setCreatedById(user.getUserId());
						if (eventItem.getLocation() != null) {
							event.setLocation(eventItem.getLocation().get("DisplayName"));
						}
						event.setScheduleAs(ScheduleEventAs.Event.toString());
						event.setTimezone(eventItem.getStart().get("TimeZone"));
						
						if (eventItem.getIsAllDay() != null) {
							event.setIsFullDay(eventItem.getIsAllDay());
						}
						
						Date startDate = null;
						if (eventItem.getStart() != null) {
							
							if (eventItem.getStart().containsKey("DateTime")) {
								startDate = outlookFormat.parse((String) eventItem.getStart().get("DateTime"));
								System.out.println("Actual Date : "+startDate);
							}
							if (startDate != null) {
								event.setStartTime(startDate);
							}
						}
						
						if (startDate == null) {
							event.setStartTime(new Date());
						}
						
						Date endDate = null;
						if (eventItem.getEnd() != null) {
							if (eventItem.getEnd().containsKey("DateTime")) {
								endDate = outlookFormat.parse((String) eventItem.getEnd().get("DateTime"));
							}
							if (endDate != null) {
								event.setEndTime(endDate);
							} else {
								if (startDate != null) {
									endDate = startDate;
								}
							}
						}
						/*if (endDate == null && startDate != null) {
							event.setEndTime(startDate);
						} else {
							event.setEndTime(new Date());
						}*/
						
						List<EventMember> membersTemp = new ArrayList<>();
						if (eventItem.getAttendees() != null
								&& eventItem.getAttendees().size() > 0) {
							membersTemp = parseOutlookEventMembers(eventItem.getAttendees());
						} else {
							EventMember eventMember = new EventMember();
							eventMember.setName(user.getName());
							eventMember.setStatus(MemberStatus.Going.toString());
							eventMember.setSource(EventSource.Outlook.toString());
							if (user.getEmail() != null) {
								eventMember.setSourceEmail(user.getEmail());
							}
							eventMember.setUserId(user.getUserId());
							membersTemp.add(eventMember);
						}
						
						//Clearing old users and adding new one.
						if (event.getEventMembers() != null && event.getEventMembers().size() > 0) {
							event.getEventMembers().clear();
							event.getEventMembers().addAll(membersTemp);
						} else {
							event.setEventMembers(membersTemp);
						}
						
						events.add(event);
					}
					this.eventRepository.save(events);
				} else {
					if (outlookEvents.getErrorDetail() != null) {
						Event event = new Event();
						event.setErrorCode(outlookEvents.getErrorCode());
						event.setErrorDetail(outlookEvents.getErrorDetail());
						events.add(event);
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return events;
	}

	public List<EventMember> parseOutlookEventMembers(
			List<OutlookEventAttendees> attendees) {
		List<EventMember> members = new ArrayList<>();
		for (OutlookEventAttendees attendee : attendees) {
			EventMember eventMember = new EventMember();
			if (attendee.getEmailAddress().containsKey("Name")) {
				eventMember.setName(attendee.getEmailAddress().get("Name"));
			}
			eventMember.setStatus(attendee.getStatus().get("Response"));

			eventMember.setSource(EventSource.Outlook.toString());
			eventMember.setSourceEmail(attendee.getEmailAddress().get("Name"));
			members.add(eventMember);
		}
		return members;
	}
	
	public List<Event> syncHolidays(String calendarId,User user) {
		List<Event> events = new ArrayList<>();
		try {
			GoogleService gs = new GoogleService();
			GoogleEvents googleEvents = gs.getCountryHolidayEvents(calendarId);

			if (googleEvents != null && googleEvents.getItems() != null
					&& googleEvents.getItems().size() > 0) {
				for (GoogleEventItem eventItem : googleEvents.getItems()) {
					Event event = null;
					List<Event> dbevents = this.eventRepository.findBySourceEventIdAndCreatedById(eventItem.getId(), user.getUserId());
					if (dbevents == null || dbevents.size() == 0) {
						event = new Event();
					} else {
						event = dbevents.get(0);
					}
					
					if (eventItem.getStart() != null) {
						Date startDate = null;
						if (eventItem.getStart().containsKey("dateTime")) {
							startDate = CenesUtils.yyyyMMddTHHmmssX.parse((String) eventItem
									.getStart().get("dateTime"));
						} else if (eventItem.getStart().containsKey("date")) {
							startDate = CenesUtils.yyyyMMdd
									.parse((String) eventItem.getStart().get(
											"date"));
						}
						if (startDate != null
								&& startDate.getTime() < new Date().getTime()) {
							continue;
						}
					}

					event.setSourceEventId(eventItem.getId());
					event.setSource(EventSource.Google.toString());
					event.setTitle(eventItem.getSummary());
					event.setCreatedById(user.getUserId());
					event.setIsFullDay(true);
					if (eventItem.getDescription() != null) {
						event.setDescription(eventItem.getDescription());
					}
					//event.setEventPicture("http://cenes.test2.redblink.net/assets/default_images/default_event_image.png");
					if (eventItem.getLocation() != null) {
						event.setLocation(eventItem.getLocation());
					}

					event.setScheduleAs(ScheduleEventAs.Holiday.toString());
					event.setTimezone(googleEvents.getTimeZone());
					if (eventItem.getStart() != null) {
						Date startDate = null;
						if (eventItem.getStart().containsKey("dateTime")) {
							startDate = CenesUtils.yyyyMMddTHHmmssX.parse((String) eventItem.getStart().get("dateTime"));
						} else if (eventItem.getStart().containsKey("date")) {
							startDate = CenesUtils.yyyyMMdd.parse((String) eventItem.getStart().get("date"));
						}
						if (startDate != null) {
							String startDateStr = CenesUtils.yyyyMMddTHHmmss.format(startDate);
							event.setStartTime(CenesUtils.yyyyMMddTHHmmss.parse(startDateStr));
						}
					}
					if (eventItem.getEnd() != null) {
						Date endDate = null;
						if (eventItem.getEnd().containsKey("dateTime")) {
							endDate = CenesUtils.yyyyMMddTHHmmssX.parse((String) eventItem.getEnd().get("dateTime"));
						} else if (eventItem.getEnd().containsKey("date")) {
							endDate = CenesUtils.yyyyMMdd.parse((String) eventItem.getEnd().get("date"));
						}
						if (endDate != null) {
							String endDateStr = CenesUtils.yyyyMMddTHHmmss.format(endDate);
							event.setEndTime(CenesUtils.yyyyMMddTHHmmss.parse(endDateStr));
						}
					}
					event.setProcessed(EventProcessedStatus.Processed.ordinal());
					
					List<EventMember> members = new ArrayList<>();
					EventMember eventMember = new EventMember();
					eventMember.setUserId(user.getUserId());
					eventMember.setName(user.getName());
					eventMember.setPicture(user.getPhoto());
					eventMember.setStatus("Going");
					eventMember.setSource("Google");
					members.add(eventMember);
					try {
						if (event.getEventMembers() != null) {
							event.getEventMembers().clear();
						}
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
					
					event.setEventMembers(members);
					
					events.add(event);
				}
				eventService.saveEventsBatch(events);
				System.out.println("[ Holiday Calendar Events Sync - User Id : "+ user.getUserId() + " ,Calendar Id : " + calendarId+ "  ENDS");
			} else {
				if (googleEvents.getErrorDetail() != null) {
					Event event = new Event();
					event.setErrorCode(googleEvents.getErrorCode());
					event.setErrorDetail(googleEvents.getErrorDetail());
					events.add(event);
				}
			}

		} catch(Exception e) {
			e.printStackTrace();
		}
		return events;
	}
	
	public Map<String,Object> syncDeviceCalendar(Map<String,List<Event>> eventMap) {
		System.out.println("[Syncing Device Calendar : Date : "+new Date()+" STARTS]");
		System.out.println("syncdevicecalendar : "+eventMap);
		Map<String,Object> response = new HashMap<>();
		try {
			if (eventMap.containsKey("data")) {
				Event event = eventMap.get("data").get(0);
				User user = userService.findUserById(event.getCreatedById());
				
				deleteEventsByCreatedByIdSource(user.getUserId(), Event.EventSource.Apple.toString());
				eventTimeSlotManager.deleteEventTimeSlotsByUserIdSource(user.getUserId(), Event.EventSource.Apple.toString());
				
				List<Event> deviceEvents = eventMap.get("data");
				for (Event deviceEvent : deviceEvents) {
					List<EventMember> members = new ArrayList<>();
					
					EventMember eventMember = new EventMember();
					eventMember.setName(user.getName());
					
					//This has to be removed later.
					eventMember.setPicture(user.getPhoto());
					
					eventMember.setStatus(MemberStatus.Going.toString());
					eventMember.setSource(EventSource.Apple.toString());
					if (user.getEmail() != null) {
						eventMember.setSourceEmail(user.getEmail());
					}
					eventMember.setUserId(user.getUserId());
					members.add(eventMember);
					deviceEvent.setEventMembers(members);
				}
				eventService.saveEventsBatch(eventMap.get("data"));
			}
			response.put("success", true);
			response.put("errorCode", 0);
			response.put("errorDetail", null);
		} catch(Exception e){
			e.printStackTrace();
			response.put("success", false);
			response.put("errorCode", ErrorCodes.InternalServerError.ordinal());
			response.put("errorDetail", ErrorCodes.InternalServerError.toString());
		}
		System.out.println("[Syncing Device Calendar : Date : "+new Date()+" ENDS]");
		return response;
	}
	
	public void refreshGoogleEvents(Long userId) {

		// TODO Auto-generated method stub
		System.out.println("[Google Refresh : User ID : " + userId + "]");
		CalendarSyncToken calendarSyncToken = findCalendarSyncTokenByUserIdAndAccountType(userId,
				CalendarSyncToken.AccountType.Google);

		if (calendarSyncToken != null) {
			System.out.println(
					"[Google Sync] Date : " + new Date() + " Getting Access Token Response from RefreshToken");
			GoogleService googleService = new GoogleService();
			JSONObject refreshTokenResponse = googleService
					.getAccessTokenFromRefreshToken(calendarSyncToken.getRefreshToken());
			System.out.println("[Google Sync] Date : " + new Date() + " Response from Refresh Token : "
					+ refreshTokenResponse.toString());
			try {
				if (refreshTokenResponse != null) {

					String accessToken = refreshTokenResponse.getString("access_token");

					User user = userService.findUserById(userId);
					deleteEventsByCreatedByIdSourceScheduleAs(userId,
							Event.EventSource.Google.toString(), Event.ScheduleEventAs.Event.toString());
					eventTimeSlotManager.deleteEventTimeSlotsByUserIdSourceScheduleAs(userId,
							Event.EventSource.Google.toString(), Event.ScheduleEventAs.Event.toString());

					System.out.println("[ Syncing Google Refreshing Events - User Id : " + userId
							+ ", Access Token : " + accessToken + "]");
					List<Event> events = syncGoogleEvents(false, accessToken, user);
					if (events == null) {
						Event errorEvent = new Event();
						errorEvent.setErrorCode(ErrorCode.INTERNAL_ERROR.ordinal());
						errorEvent.setErrorDetail(ErrorCode.INTERNAL_ERROR.toString());
						events = new ArrayList<>();
						events.add(errorEvent);
					}
					System.out.println("[ Refreshing Google Events - User Id : " + userId + ", Access Token : "
							+ accessToken + "]");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("[ Syncing Google Events ENDS]");
	}
	
	public void refreshOutlookEvent(Long userId) {

		// TODO Auto-generated method stub
		System.out.println("[ Refreshing Outlook Events - User Id : " + userId + "]");

		CalendarSyncToken calendarSyncToken = findCalendarSyncTokenByUserIdAndAccountType(userId,
				CalendarSyncToken.AccountType.Outlook);

		if (calendarSyncToken != null) {
			System.out.println(
					"[Outlook Sync] Date : " + new Date() + " Getting Access Token Response from RefreshToken");
			OutlookService outlookService = new OutlookService();
			JSONObject refreshTokenResponse = outlookService
					.getAccessTokenFromRefreshToken(calendarSyncToken.getRefreshToken());
			System.out.println("[Outlook Sync] Date : " + new Date() + " Response from Refresh Token : "
					+ refreshTokenResponse.toString());
			if (refreshTokenResponse != null) {
				try {
					String accessToken = refreshTokenResponse.getString("access_token");

					User user = userService.findUserById(userId);
					List<Event> events = null;
					try {
						deleteEventsByCreatedByIdSource(userId, Event.EventSource.Outlook.toString());
						eventTimeSlotManager.deleteEventTimeSlotsByUserIdSource(userId,
								Event.EventSource.Outlook.toString());

						OutlookService os = new OutlookService();
						List<OutlookEvents> outlookEventList = os.getOutlookCalenderEvents(accessToken);
						if (outlookEventList != null && outlookEventList.size() > 0) {
							System.out.println("Outlook Calendar events size : " + outlookEventList.size());
							events = populateOutlookEventsInCenes(outlookEventList, user);
							System.out.println("Events to Sync : " + events.size());

						} else {
							List<OutlookEvents> iosOutlookEvents = os.getIosOutlookEvents(accessToken);
							if (iosOutlookEvents != null && iosOutlookEvents.size() > 0) {
								System.out.println("Outlook IOS Calendar events size : " + iosOutlookEvents.size());
								events = populateOutlookEventsInCenes(iosOutlookEvents, user);
								System.out.println("Outlook IOS Events to Sync : " + events.size());
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						Event errorEvent = new Event();
						errorEvent.setErrorCode(ErrorCode.INTERNAL_ERROR.ordinal());
						errorEvent.setErrorDetail(ErrorCode.INTERNAL_ERROR.toString());
						List<Event> errorEvents = new ArrayList<>();
						errorEvents.add(errorEvent);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("[ Syncing Outlook Events ENDS]");
	}
	
	public void runSyncThread(Long userId, Map<String, List<Event>> eventMap, Map<String, Object> phoneContacts) {
		eventThread.runEventThread(userId, eventMap, phoneContacts);
	}
	
	public void updateTimeSlotsToFreeByEvent(Event event) {
		List<EventTimeSlot> timeSlots  = eventService.findEventTimeSlotByEventDateAndEventId(event.getStartTime().getTime(),event.getEndTime().getTime(),event.getEventId());
		if (timeSlots != null && timeSlots.size() > 0) {
			for (EventTimeSlot ets : timeSlots) {
				ets.setStatus(TimeSlotStatus.Free.toString());
			}
		}
		eventService.saveAndUpdateEventTimeSlot(timeSlots);
	}
	
	public void updateEventMemberTimeSlot(Event event, EventMember eventMember) {
		List<EventTimeSlot> timeSlots  = eventService.findEventTimeSlotByEventDateAndUserId(event.getStartTime().getTime(),event.getEndTime().getTime(),eventMember.getUserId());
		if (timeSlots != null && timeSlots.size() > 0) {
			for (EventTimeSlot ets : timeSlots) {
				ets.setStatus(TimeSlotStatus.Free.toString());
			}
		}
		eventService.saveAndUpdateEventTimeSlot(timeSlots);
	}
	
	public void updateEventMemberPicture(String picture,Long userId) {
		eventService.updateEventMemberPictureByUserId(picture, userId);
	}
	
	
	public EventMember findEventMemberByEventIdAndUserId(Long eventId, Long userId) {
		return eventMemberRepository.findByEventIdAndUserId(eventId, userId);
	}
	
	public EventMember findEventMemberByEventMemberId(Long eventMemberId) {
		return eventService.findEventMemeberByEventMemberId(eventMemberId);
	}
	
	public Event findEventByEventId(Long eventId) {
		return eventService.findEventById(eventId);
	}	
	
	public List<Event> findEventsByEventMemberId(Long userId) {
		return eventService.findEventsByStartDateAndUserId(userId);
	}
	
	public List<CalendarSyncToken> getAllGoogleSyncTokens() {
		return refreshTokenRepository.findByAccountType(AccountType.Google);
	}
	
	public List<CalendarSyncToken> getAllOutlookGoogleSyncTokens() {
		return refreshTokenRepository.findByAccountType(AccountType.Outlook);
	}
	
	public CalendarSyncToken findCalendarSyncTokenByUserIdAndAccountType(Long userId, CalendarSyncToken.AccountType accountType) {
		return refreshTokenRepository.findByUserIdAndAccountType(userId, accountType);
	}
	
	public void saveCalendarSyncToken(CalendarSyncToken calendarSyncToken) {
		refreshTokenRepository.save(calendarSyncToken);
	}
	
	public List<Event> findEventsToSendAlerts() {
		return eventRepository.findAllEventsWithTimeDifferenceEqualToOne();
	}
	
	public List<LocationDto> findEventLocationsByUserId(Long userId) {
		return eventServiceDao.findDistinctEventLocations(userId);
	}
	
	public List<GatheringPreviousLocation> findTop15PreviousLocationsByUserId(Long userId) {
		return gatheringPreviousLocationRepository.findTop15ByUserIdOrderByGatheringPreviousLocationIdDesc(userId);
	}
	
	/*public static void main(String[] args) {
		String endDateStr = CenesUtils.yyyyMMddTHHmmss.format(new Date());
		Date outlookDate = null;
		try {
			outlookDate = CenesUtils.yyyyMMddTHHmmss.parse(endDateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TimeZone tzTo = TimeZone.getTimeZone("PST");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(tzTo);
		String endDateStrTemp = sdf.format(outlookDate);
		try {
			System.out.println(sdf.parse(endDateStrTemp));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
}
