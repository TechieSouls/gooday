package com.cg.events.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cg.constant.CgConstants.ErrorCodes;
import com.cg.events.bo.Event;
import com.cg.events.bo.EventMember;
import com.cg.events.repository.EventRepository;
import com.cg.repository.UserRepository;
import com.cg.user.bo.User;

@RestController
@Api(value = "Event", description = "Events of user")
public class EventController {

	@Autowired
	public EventRepository eventRepository;
	
	@Autowired
	public UserRepository userRepository;

	@ApiOperation(value = "Save Event", notes = "Save event for a user", code = 200, httpMethod = "POST", produces = "application/json")
	@ModelAttribute(value = "event")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Event Saved successfuly", response = Event.class) })
	@RequestMapping(value = "/api/event/", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Event> saveEvent(@ApiParam(name="event", value="dummy event",required=true) @RequestBody Event event) {
		try {
			event = eventRepository.save(event);
			for(EventMember eventMember : event.getEventMembers()){
				User user = userRepository.findOne(eventMember.getMemberId());
				eventMember.setMember(user);
				eventMember.setEventId(event.getEventId());
			}
			return new ResponseEntity<Event>(event,HttpStatus.OK);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<Event>(event,HttpStatus.OK);
	}
	
	@ApiOperation(value = "Fetch Event By Its Id", notes = "Fecth event by id", code = 200, httpMethod = "GET", produces = "application/json")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Event fetched successfuly", response = Event.class) })
	@RequestMapping(value = "/api/event/{eventId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Event> getEventById(@ApiParam(name="event_id", value="long",required=true) @PathVariable("eventId")Long eventId) {
		Event event = new Event();
		try {
			event = eventRepository.findOne(eventId);
			return new ResponseEntity<Event>(event,HttpStatus.OK);
		} catch(Exception e) {
			e.printStackTrace();
		}
		event.setErrorCode(ErrorCodes.RowNotFound.getErrorCode());
		event.setErrorDetail(ErrorCodes.RowNotFound.toString());
		return new ResponseEntity<Event>(event,HttpStatus.OK);
	}
	
	@ApiOperation(value = "Fetch User Events", notes = "Fecth user events", code = 200, httpMethod = "GET", produces = "application/json")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Event fetched successfuly", response = Event.class) })
	@RequestMapping(value = "/api/user-events/{userId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<Event>> getAllUserEvents(
			@ApiParam(name = "user_id", value = "User id", required = true) @PathVariable("userId") Long userId) {
		List<Event> events = new ArrayList<Event>();
		try {
			events = eventRepository.findByCreatedById(userId);

			return new ResponseEntity<List<Event>>(events, HttpStatus.OK);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Event event = new Event();
		event.setErrorCode(ErrorCodes.RowNotFound.getErrorCode());
		event.setErrorDetail(ErrorCodes.RowNotFound.toString());
		events.add(event);
		return new ResponseEntity<List<Event>>(events, HttpStatus.OK);
	}
	
}
