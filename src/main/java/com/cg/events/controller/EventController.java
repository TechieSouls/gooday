package com.cg.events.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import okhttp3.internal.framed.ErrorCode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cg.constant.CgConstants.ErrorCodes;
import com.cg.events.bo.Event;
import com.cg.events.bo.Event.EventSource;
import com.cg.events.bo.EventMember;
import com.cg.events.bo.EventMember.MemberStatus;
import com.cg.events.bo.FacebookEventItem;
import com.cg.events.bo.FacebookEvents;
import com.cg.events.bo.GoogleEventAttendees;
import com.cg.events.bo.GoogleEventItem;
import com.cg.events.bo.GoogleEvents;
import com.cg.events.repository.EventRepository;
import com.cg.repository.UserRepository;
import com.cg.service.FacebookService;
import com.cg.service.GoogleService;
import com.cg.user.bo.User;

@RestController
@Api(value = "Event", description = "Events of user")
public class EventController {

	@Autowired
	public EventRepository eventRepository;

	@Autowired
	public UserRepository userRepository;
	SimpleDateFormat zoneFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssX");
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	SimpleDateFormat searchDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	
	@ApiOperation(value = "Save Event", notes = "Save event for a user", code = 200, httpMethod = "POST", produces = "application/json")
	@ModelAttribute(value = "event")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Event Saved successfuly", response = Event.class) })
	@RequestMapping(value = "/api/event/", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Event> saveEvent(
			@ApiParam(name = "event", value = "dummy event", required = true) @RequestBody Event event) {
		try {
			event = eventRepository.save(event);
			for (EventMember eventMember : event.getEventMembers()) {
				//User user = userRepository.findOne(eventMember.getSourceId());
				//eventMember.setMember(user);
				//eventMember.setEventId(event.getEventId());
			}
			return new ResponseEntity<Event>(event, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<Event>(event, HttpStatus.OK);
	}

	@ApiOperation(value = "Fetch Event By Its Id", notes = "Fecth event by id", code = 200, httpMethod = "GET", produces = "application/json")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Event fetched successfuly", response = Event.class) })
	@RequestMapping(value = "/api/event/{eventId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Event> getEventById(
			@ApiParam(name = "event_id", value = "long", required = true) @PathVariable("eventId") Long eventId) {
		Event event = new Event();
		try {
			event = eventRepository.findOne(eventId);
			return new ResponseEntity<Event>(event, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		}
		event.setErrorCode(ErrorCodes.RowNotFound.getErrorCode());
		event.setErrorDetail(ErrorCodes.RowNotFound.toString());
		return new ResponseEntity<Event>(event, HttpStatus.OK);
	}

	@ApiOperation(value = "Fetch User Events", notes = "Fecth user events by date and timezone", code = 200, httpMethod = "GET", produces = "application/json")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Event fetched successfuly", response = Event.class) })
	@RequestMapping(value = "/api/getEvents", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<Event>> getUserEventsByDateAndTimeZone(@RequestParam("user_id") Long userId,
			@RequestParam(value="date",required=false,defaultValue="") String eventDate,
			@RequestParam(value="timezone",required=false,defaultValue="") String timeZone) {
		List<Event> events = new ArrayList<Event>();
		try {
			if (timeZone.length() != 0) {
				if (eventDate.length() == 0) {
					events = eventRepository.findByCreatedByIdAndTimezone(userId,timeZone);
				} else {
					events = eventRepository.findByCreatedByIdAndStartDateAndTimeZone(userId,searchDateFormat.parse(eventDate),timeZone);
				}
			} else {
				if (eventDate.length() == 0) {
					events = eventRepository.findByCreatedById(userId);
				} else {
					events = eventRepository.findByCreatedByIdAndStartDate(userId,searchDateFormat.parse(eventDate));
				}
			}
			return new ResponseEntity<List<Event>>(events, HttpStatus.OK);
		} catch (Exception ex) {
			ex.printStackTrace();
			Event event = new Event();
			event.setErrorCode(ErrorCode.INTERNAL_ERROR.ordinal());
			event.setErrorDetail(ErrorCode.INTERNAL_ERROR.toString());
			events.add(event);
			return new ResponseEntity<List<Event>>(events, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@ApiOperation(value = "Fetch User Events", notes = "Fecth user events by date and timezone", code = 200, httpMethod = "GET", produces = "application/json")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Event fetched successfuly", response = Event.class) })
	@RequestMapping(value = "/api/getEvents/user", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<Event>> getUserEvents(@RequestParam("user_id") Long userId) {
		List<Event> events = new ArrayList<Event>();
		try {
			events = eventRepository.findByCreatedById(userId);
			return new ResponseEntity<List<Event>>(events, HttpStatus.OK);
		} catch (Exception ex) {
			ex.printStackTrace();
			Event event = new Event();
			event.setErrorCode(ErrorCode.INTERNAL_ERROR.ordinal());
			event.setErrorDetail(ErrorCode.INTERNAL_ERROR.toString());
			events.add(event);
			return new ResponseEntity<List<Event>>(events, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	//Method to get Facebook Events from API.
	@ApiOperation(value = "Get Facebook events", notes = "Get Facebook events and save in db", code = 200, httpMethod = "GET", produces = "application/json")
	@ModelAttribute(value = "events")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Events Fetched Successfuly") })
	@RequestMapping(value = "/api/facebook/events/{facebook_id}/{access_token}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<Event>> getFacebookUserEvents(
			@PathVariable("facebook_id") String facebookId,
			@PathVariable("access_token") String accessToken) {
		List<Event> events = new ArrayList<>();
		try {

			User user = userRepository.findUserByFacebookID(facebookId);
			
			FacebookService fs = new FacebookService();
			FacebookEvents facebookEvents = fs.facebookEvents(facebookId, accessToken);
			for (FacebookEventItem eventItem : facebookEvents.getData()) {
				
				Event event = eventRepository.findBySourceEventId(eventItem.getId());
				if (event == null) {
					event = new Event();
				}
				event.setCreatedById(user.getUserId());
				event.setSourceUserId(facebookId);
				event.setSourceEventId(eventItem.getId());
				event.setTitle(eventItem.getName());
				if (eventItem.getDescription() != null) {
					event.setDecription(eventItem.getDescription());
				}
				event.setSource(EventSource.Facebook.toString());
				event.setTimezone(eventItem.getTimezone());

				if (eventItem.getStart_time() != null) {
					Date startDate = zoneFormat.parse(eventItem.getStart_time());
					String startDateStr = sdf.format(startDate);
					event.setStartTime(sdf.parse(startDateStr));
					
					Date endDate = zoneFormat.parse(eventItem.getEnd_time());
					String endDateStr = sdf.format(endDate);
					event.setEndTime(sdf.parse(endDateStr));
				}
				event.setLocation((String)eventItem.getPlace().get("name"));
				
				List<EventMember> members = new ArrayList<>();
				if (eventItem.getAttending() != null) {
					List<Map<String,String>> attendees = (List)eventItem.getAttending().get("data");
					members.addAll(parseFacebookEventMembers(attendees));
				}
				if (eventItem.getMaybe() != null) {
					List<Map<String,String>> attendees = (List)eventItem.getMaybe().get("data");
					members.addAll(parseFacebookEventMembers(attendees));
				}
				if (eventItem.getDeclined() != null) {
					List<Map<String,String>> attendees = (List)eventItem.getDeclined().get("data");
					members.addAll(parseFacebookEventMembers(attendees));
				}
				event.setEventMembers(members);
				event = eventRepository.save(event);
				events.add(event);
			}
			return new ResponseEntity<List<Event>>(events, HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			Event errorEvent = new Event();
			errorEvent.setErrorCode(ErrorCode.INTERNAL_ERROR.ordinal());
			errorEvent.setErrorDetail(ErrorCode.INTERNAL_ERROR.toString());
			events.add(errorEvent);
			return new ResponseEntity<List<Event>>(events,
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public List<EventMember> parseFacebookEventMembers(List<Map<String,String>> attendees) {
		List<EventMember> members = new ArrayList<>();
		for (Map<String,String> attendee : attendees) {
			EventMember eventMember = new EventMember();
			eventMember.setName(attendee.get("name"));
			eventMember.setPicture("https://graph.facebook.com/v2.10/"+attendee.get("id")+"/picture?height=200&type=normal&width=200");
			if (attendee.get("rsvp_status").equals("attending")) {
				eventMember.setStatus(MemberStatus.Going.toString());
			} else if (attendee.get("rsvp_status").equals("maybe")) {
				eventMember.setStatus(MemberStatus.Maybe.toString());
			} else if (attendee.get("rsvp_status").equals("declined")) {
				eventMember.setStatus(MemberStatus.NotGoing.toString());
			}
			eventMember.setSource(EventSource.Facebook.toString());
			eventMember.setSourceId(attendee.get("id"));
			members.add(eventMember);
		}
		return members;
	}
	
	
	//Method to get Google events from API.
		@ApiOperation(value = "Get Google events", notes = "Get Google events and save in db", code = 200, httpMethod = "GET", produces = "application/json")
		@ModelAttribute(value = "events")
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Events Fetched Successfuly") })
		@RequestMapping(value = "/api/google/events", method = RequestMethod.GET)
		@ResponseBody
	public ResponseEntity<List<Event>> getGoogleEvents(@RequestParam("access_token") String accessToken) {

		List<Event> events = new ArrayList<>();
		try {
			GoogleService gs = new GoogleService();
			GoogleEvents googleEvents = gs.getCalenderEvents(accessToken);
			if (events != null && googleEvents.getItems() != null) {
				for (GoogleEventItem eventItem : googleEvents.getItems()) {

					Event event = this.eventRepository.findBySourceEventId(eventItem.getId());
					if (event == null) {
						event = new Event();
					}
					event.setSourceEventId(eventItem.getId());
					event.setSource(EventSource.Google.toString());
					event.setTitle(eventItem.getSummary());
					event.setDecription(event.getDecription());
					event.setLocation(eventItem.getLocation());
					event.setTimezone(googleEvents.getTimeZone());
					if (eventItem.getStart() != null) {
						Date startDate = null;
						if (eventItem.getStart().containsKey("dateTime")) {
							startDate = zoneFormat.parse((String) eventItem.getStart().get("dateTime"));
						} else if (eventItem.getStart().containsKey("date")) {
							startDate = searchDateFormat.parse((String) eventItem.getStart().get("date"));
						}
						if (startDate != null) {
							String startDateStr = sdf.format(startDate);
							event.setStartTime(sdf.parse(startDateStr));
						}
					}
					if (eventItem.getEnd() != null) {
						Date endDate = null;
						if (eventItem.getEnd().containsKey("dateTime")) {
							endDate = zoneFormat.parse((String) eventItem.getEnd().get("dateTime"));
						} else if (eventItem.getEnd().containsKey("date")) {
							endDate = searchDateFormat.parse((String) eventItem.getEnd().get("date"));
						}
						if (endDate != null) {
							String endDateStr = sdf.format(endDate);
							event.setEndTime(sdf.parse(endDateStr));
						}
					}
					event.setEventMembers(parseGoogleEventMembers(eventItem.getAttendees()));
					event = this.eventRepository.save(event);
					events.add(event);
				}
			} else {
				if (googleEvents.getErrorDetail() != null) {
					Event event = new Event();
					event.setErrorCode(googleEvents.getErrorCode());
					event.setErrorDetail(googleEvents.getErrorDetail());
					events.add(event);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Event errorEvent = new Event();
			errorEvent.setErrorCode(ErrorCode.INTERNAL_ERROR.ordinal());
			errorEvent.setErrorDetail(ErrorCode.INTERNAL_ERROR.toString());
			List<Event> errorEvents = new ArrayList<>();
			errorEvents.add(errorEvent);
			return new ResponseEntity<List<Event>>(errorEvents, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<List<Event>>(events,HttpStatus.OK);
	}
	
	public List<EventMember> parseGoogleEventMembers(List<GoogleEventAttendees> attendees) {
		List<EventMember> members = new ArrayList<>();
		for (GoogleEventAttendees attendee : attendees) {
			EventMember eventMember = new EventMember();
			if (attendee.getDisplayName() != null) {
				eventMember.setName(attendee.getDisplayName());
			}
			if (attendee.getResponseStatus().equals("accepted")) {
				eventMember.setStatus(MemberStatus.Going.toString());
			} else if (attendee.getResponseStatus().equals("needsAction")) {
				eventMember.setStatus(MemberStatus.Maybe.toString());
			} else if (attendee.getResponseStatus().equals("declined")) {
				eventMember.setStatus(MemberStatus.NotGoing.toString());
			}
			eventMember.setSource(EventSource.Google.toString());
			eventMember.setSourceEmail(attendee.getEmail());
			members.add(eventMember);
		}
		return members;
	}
}
