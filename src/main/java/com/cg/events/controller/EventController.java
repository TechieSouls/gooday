package com.cg.events.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import okhttp3.internal.framed.ErrorCode;

import org.primefaces.json.JSONArray;
import org.primefaces.json.JSONObject;
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
import com.cg.events.repository.EventRepository;
import com.cg.repository.UserRepository;
import com.cg.user.bo.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

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
	public ResponseEntity<Event> saveEvent(
			@ApiParam(name = "event", value = "dummy event", required = true) @RequestBody Event event) {
		try {
			event = eventRepository.save(event);
			for (EventMember eventMember : event.getEventMembers()) {
				User user = userRepository.findOne(eventMember.getMemberId());
				eventMember.setMember(user);
				eventMember.setEventId(event.getEventId());
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
			@RequestParam("date") String eventDate,
			@RequestParam("timezone") String timeZone) {
		List<Event> events = new ArrayList<Event>();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			events = eventRepository.findByCreatedByIdAndStartDateAndTimeZone(userId,sdf.parse(eventDate),timeZone);

			return new ResponseEntity<List<Event>>(events, HttpStatus.OK);
		} catch (Exception ex) {
			Event event = new Event();
			event.setErrorCode(ErrorCode.INTERNAL_ERROR.ordinal());
			event.setErrorDetail(ErrorCode.INTERNAL_ERROR.toString());
			events.add(event);
			ex.printStackTrace();
		}
		Event event = new Event();
		event.setErrorCode(ErrorCodes.RowNotFound.getErrorCode());
		event.setErrorDetail(ErrorCodes.RowNotFound.toString());
		events.add(event);
		return new ResponseEntity<List<Event>>(events, HttpStatus.OK);
	}

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

			String url = "https://graph.facebook.com/"
					+ facebookId
					+ "/events?fields=owner,name,id,attending_count,description,place,type,timezone,start_time,end_time,attending{id,name,picture}&access_token="
					+ accessToken;
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			if (responseCode == 200) {

				System.out.println("Response Code : " + responseCode);

				BufferedReader in = new BufferedReader(new InputStreamReader(
						con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				Map<String, Object> fbEventsMap = new ObjectMapper().readValue(
						response.toString(), HashMap.class);
				List<Map<String, Object>> fbEvents = (List<Map<String, Object>>) fbEventsMap
						.get("data");
				for (Map<String, Object> fbEvent : fbEvents) {

					Event event = new Event();
					event.setCreatedById(user.getUserId());
					event.setSourceEventId(fbEvent.get("id").toString());
					event.setTitle(fbEvent.get("name").toString());

					if (fbEvent.containsKey("description")) {
						event.setDecription(fbEvent.get("description")
								.toString());
					}

					event.setSource(EventSource.Facebook.toString());
					event.setTimezone(fbEvent.get("timezone").toString());

					SimpleDateFormat sdf = new SimpleDateFormat(
							"yyyy-MM-dd'T'HH:mm:ssZ");

					if (fbEvent.containsKey("start_time")) {
						event.setStartTime(sdf.parse(fbEvent.get("start_time")
								.toString()));
						event.setEndTime(sdf.parse(fbEvent.get("end_time")
								.toString()));
					}

					Map<String, String> placeMap = (Map<String, String>) fbEvent
							.get("place");
					event.setLocation(placeMap.get("name"));

					Map<String, String> ownerMap = (Map<String, String>) fbEvent
							.get("owner");
					event.setSourceUserId(ownerMap.get("id"));

					// Check if Events already exists, if not save events in DB.
					// That are new events to be sync.
					if (!eventRepository.existsBySourceEventId(event
							.getSourceEventId())) {
						event = eventRepository.save(event);
					}
					events.add(event);
				}

				return new ResponseEntity<List<Event>>(events, HttpStatus.OK);
			}

		} catch (Exception e) {
			e.printStackTrace();
			Event event = new Event();
			event.setErrorCode(ErrorCode.INTERNAL_ERROR.ordinal());
			event.setErrorDetail(ErrorCode.INTERNAL_ERROR.toString());
			events.add(event);
			return new ResponseEntity<List<Event>>(events,
					HttpStatus.METHOD_FAILURE);
		}
		Event event = new Event();
		event.setErrorCode(ErrorCodes.RowNotFound.getErrorCode());
		event.setErrorDetail(ErrorCodes.RowNotFound.toString());
		events.add(event);
		return new ResponseEntity<List<Event>>(events, HttpStatus.OK);
	}

}
