package com.cg.events.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.primefaces.json.JSONArray;
import org.primefaces.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.multipart.MultipartFile;

import com.cg.bo.CalendarSyncToken;
import com.cg.bo.CenesProperty;
import com.cg.bo.CenesProperty.PropertyOwningEntity;
import com.cg.bo.CenesPropertyValue;
import com.cg.bo.GatheringPreviousLocation;
import com.cg.bo.CalendarSyncToken.AccountType;
import com.cg.bo.Notification.NotificationType;
import com.cg.constant.CgConstants.ErrorCodes;
import com.cg.dao.EventServiceDao;
import com.cg.dto.HomeScreenDto;
import com.cg.events.bo.Event;
import com.cg.events.bo.Event.EventSource;
import com.cg.events.bo.Event.EventUpdateFor;
import com.cg.events.bo.EventMember;
import com.cg.events.bo.EventMember.MemberStatus;
import com.cg.events.bo.EventTimeSlot;
import com.cg.events.bo.OutlookEvents;
import com.cg.events.bo.PredictiveCalendar;
import com.cg.events.bo.RecurringPattern;
import com.cg.events.repository.EventRepository;
import com.cg.events.repository.EventTimeSlotRepository;
import com.cg.events.repository.RecurringPatternRepository;
import com.cg.manager.EventManager;
import com.cg.manager.EventTimeSlotManager;
import com.cg.manager.NotificationManager;
import com.cg.repository.ReminderRepository;
import com.cg.repository.UserFriendRepository;
import com.cg.repository.UserRepository;
import com.cg.service.EventService;
import com.cg.service.GoogleService;
import com.cg.service.OutlookService;
import com.cg.service.PushNotificationService;
import com.cg.service.UserService;
import com.cg.user.bo.User;
import com.cg.utils.CenesUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.coobird.thumbnailator.Thumbnails;
import okhttp3.internal.framed.ErrorCode;

@RestController
@Api(value = "Event", description = "Events of user")
public class EventController {

	private static Object HttpClientBuilder;

	@Autowired
	public EventRepository eventRepository;

	@Autowired
	public ReminderRepository reminderRepository;

	@Autowired
	public RecurringPatternRepository recurringPatternRepository;

	@Autowired
	public UserFriendRepository userFriendRepository;

	@Autowired
	public EventTimeSlotRepository eventTimeSlotRepository;

	@Autowired
	public EventServiceDao eventServiceDao;

	@Autowired
	public UserRepository userRepository;

	@Autowired
	public EventService eventService;

	@Autowired
	public UserService userService;

	@Autowired
	EventManager eventManager;

	@Autowired
	EventTimeSlotManager eventTimeSlotManager;

	@Autowired
	public NotificationManager notificationManager;

	@Value("${cenes.eventUploadPath}")
	private String eventUploadPath;

	@Value("${cenes.domain}")
	private String domain;

	SimpleDateFormat zoneFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	SimpleDateFormat searchDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	public RecurringPattern saveRecurringPattern(
			@ApiParam(name = "event", value = "dummy event", required = true) @RequestBody RecurringPattern rp) {
		try {
			rp = recurringPatternRepository.save(rp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rp;
	}

	public List<User> getAvailableUsers(Date startDate, Date endDate) {
		List<User> availableUsers = new ArrayList<>();

		List<User> users = (List<User>) userRepository.findAll();
		for (User user : users) {
			List<Event> userEvents = new ArrayList<Event>();// eventRepository.findByCreatedByIdAndStartDate(user.getUserId(),
															// startDate);
			if (userEvents == null || userEvents.size() == 0) {
				availableUsers.add(user);
				continue;
			}
			for (Event event : userEvents) {
				long startSearhTimeNumber = startDate.getTime();
				long endSearchTimeNumber = endDate.getTime();

				long eventStartTime = event.getStartTime().getTime();
				long eventEndTime = event.getEndTime().getTime();
				if (eventStartTime < startSearhTimeNumber
						&& (eventEndTime >= startSearhTimeNumber && eventEndTime < startSearhTimeNumber)
						|| (eventStartTime > startSearhTimeNumber && eventStartTime <= endSearchTimeNumber)
								&& eventEndTime >= endSearchTimeNumber) {
					availableUsers.add(user);
					break;
				}
			}
		}
		return availableUsers;
	}

	@RequestMapping(value = "/api/event/create", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Map<String, Object>> createEvent(@RequestBody Event event) {

		boolean isOldEvent = false;
		Map<String, Object> response = new HashMap<>();
		System.out.println("[CreateEvent : " + new Date() + ", STARTS]");
		System.out.println("Event Details : " + event);
		try {
			response.put("success", true);
			response.put("errorCode", 0);
			response.put("errorDetail", null);
			// Check if event exists in Db.
			// Update all old timeslots to free and set Event to unprocessed
			// To update new status to time slots
			if (event.getEventId() != null) {
				isOldEvent = true;
				Event eventFromDatabase = eventManager.findEventByEventId(event.getEventId());

				// Check the update in Event
				int countsForNumberOfChanges = 0;
				if (!event.getTitle().equals(eventFromDatabase.getTitle())) {
					event.setUpdatedFor(EventUpdateFor.Title);
					countsForNumberOfChanges++;
				}
				if (event.getEventPicture() != null
						&& !event.getEventPicture().equals(eventFromDatabase.getEventPicture())) {
					event.setUpdatedFor(EventUpdateFor.Image);
					countsForNumberOfChanges++;
				}
				if (!event.getStartTime().equals(eventFromDatabase.getStartTime())
						|| !event.getEndTime().equals(eventFromDatabase.getEndTime())) {
					event.setUpdatedFor(EventUpdateFor.Time);
					countsForNumberOfChanges++;
				}

				if (event.getDescription() != null
						&& !event.getDescription().equals(eventFromDatabase.getDescription())) {
					event.setUpdatedFor(EventUpdateFor.Description);
					countsForNumberOfChanges++;
				}

				if (event.getLocation() != null && !event.getLocation().equals(eventFromDatabase.getLocation())) {
					event.setUpdatedFor(EventUpdateFor.Location);
					countsForNumberOfChanges++;
				}

				int oldCounts = eventFromDatabase.getEventMembers().size();
				int newCounts = event.getEventMembers().size();
				if (oldCounts != newCounts) {
					event.setUpdatedFor(EventUpdateFor.GuestList);
					countsForNumberOfChanges++;
				}

				if (countsForNumberOfChanges == 0) {
					event.setUpdatedFor(EventUpdateFor.Nothing);
				}

				if (countsForNumberOfChanges > 1) {
					event.setUpdatedFor(EventUpdateFor.MultipleChanges);
				}

				// eventManager.deleteTimeSlotsForEventMember(eventFromDatabase, eventMember);
				eventManager.deleteTimeSlotsForEvent(eventFromDatabase);

			}
			if (event.getEventId() == null || event.getKey() == null) {
				event.setScheduleAs("Gathering");
				event.setSource("Cenes");
				event.setKey(CenesUtils.getAlphaNumeric(32));
			}
			event = eventManager.createEvent(event);

			if (event.getPlaceId() != null && event.getEventPicture() != null
					&& event.getEventPicture().indexOf("google") != -1) {
				GatheringPreviousLocation gatheringPreviousLocation = null;
				gatheringPreviousLocation = eventManager
						.findGatheringPreviousLocationByUserIdAndPlaceId(event.getCreatedById(), event.getPlaceId());
				if (gatheringPreviousLocation == null) {
					gatheringPreviousLocation = new GatheringPreviousLocation();
					gatheringPreviousLocation.setEventId(event.getEventId());
					gatheringPreviousLocation.setPhoto(event.getEventPicture());
					gatheringPreviousLocation.setUserId(event.getCreatedById());
					gatheringPreviousLocation.setLocation(event.getLocation());
					gatheringPreviousLocation.setLongitude(event.getLongitude());
					gatheringPreviousLocation.setLatitude(event.getLatitude());
					gatheringPreviousLocation.setPlaceId(event.getPlaceId());
					eventManager.saveUpdateGatheringPreviousLocation(gatheringPreviousLocation);
				}
			}

			response.put("data", event);
		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("data", new Event());
			response.put("errorCode", ErrorCodes.InternalServerError.toString());
			response.put("errorDetail", ErrorCodes.InternalServerError.toString());
		}
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/api/event/delete", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Map<String, Object>> deleteEvent(@RequestParam("event_id") Long gatehringId) {
		Map<String, Object> deleteResponse = new HashMap<>();
		try {

			Event event = eventService.findEventById(gatehringId);

			notificationManager.sendDeleteNotification(event);

			for (EventMember eventMember : event.getEventMembers()) {
				eventMember.setUser(null);
			}
			try {
				eventService.deleteEventTimeSlotsByEventId(event.getEventId());
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (event.getRecurringEventId() != null) {
				eventService.deleteEventsByRecurringId(event.getRecurringEventId());
			} else {
				eventService.deleteGathering(gatehringId);
			}
			/// notificationManager.deleteNotificationByNotificationTypeId(event.getEventId());
			deleteResponse.put("success", true);
			deleteResponse.put("message", "Gathering deleted Successfully");
			deleteResponse.put("errorCode", 0);
			deleteResponse.put("errorDetail", null);

		} catch (Exception e) {
			e.printStackTrace();
			deleteResponse.put("success", false);
			deleteResponse.put("message", "Error in deleting Gathering");
			deleteResponse.put("errorCode", ErrorCodes.InternalServerError.ordinal());
			deleteResponse.put("errorDetail", ErrorCodes.InternalServerError.toString());
		}
		return new ResponseEntity<Map<String, Object>>(deleteResponse, HttpStatus.OK);
	}

	@RequestMapping(value = "/api/event/{eventId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getEventById(@PathVariable("eventId") Long eventId) {

		Map<String, Object> response = new HashMap<>();
		Event event = new Event();
		try {
			event = eventServiceDao.findGatheringByEventId(eventId);
			if (event == null) {
				response.put("success", false);
			} else {
				response.put("success", true);
			}
			response.put("data", event);
			response.put("errorCode", 0);
			response.put("errorDetail", null);
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("data", "");
			response.put("errorCode", ErrorCodes.InternalServerError.ordinal());
			response.put("errorDetail", ErrorCodes.InternalServerError.toString());
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
		}
	}

	@RequestMapping(value = "/api/event/invitation/{key}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Map<String, Object>> findEventByKey(@PathVariable("key") String key) {

		Map<String, Object> response = new HashMap<>();
		Event event = new Event();
		try {
			event = eventServiceDao.findGatheringByKey(key);
			if (event == null) {
				response.put("success", false);
			} else {
				response.put("success", true);
			}
			response.put("data", event);
			response.put("errorCode", 0);
			response.put("errorDetail", null);
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("data", "");
			response.put("errorCode", ErrorCodes.InternalServerError.ordinal());
			response.put("errorDetail", ErrorCodes.InternalServerError.toString());
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
		}
	}

	@RequestMapping(value = "/api/event/update", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> updateEventMemberStatus(
			@RequestParam("event_member_id") Long eventMemberId, @RequestParam("status") String status) {
		Map<String, Object> response = new HashMap<>();
		try {

			EventMember eventMember = eventManager.findEventMemberByEventMemberId(eventMemberId);
			if (eventMember != null) {
				notificationManager.deleteNotificationByRecepientIdNotificationTypeId(eventMember.getUserId(),
						eventMember.getEventId());
			}

			if ("confirmed".equals(status)) {
				eventMember.setStatus(MemberStatus.Going.toString());
				eventMember.setProcessed(Event.EventProcessedStatus.UnProcessed.ordinal());
			} else if ("declined".equals(status)) {
				eventMember.setStatus(MemberStatus.NotGoing.toString());
			}
			eventService.saveEventMember(eventMember);
			// eventService.updateEventMemberStatus(memberStatus, eventMemberId);

			notificationManager.sendEventAcceptDeclinedPush(eventMember);

			response.put("success", true);
			response.put("message", "Status Updated Successfully");
			response.put("errorCode", 0);
			response.put("errorDetail", null);
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("message", "Error in updating status");
			response.put("errorCode", ErrorCodes.InternalServerError.ordinal());
			response.put("errorDetail", ErrorCodes.InternalServerError.toString());
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
		}

	}

	@RequestMapping(value = "/api/event/memberStatusUpdate", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> updateEventMemberStatus(Long eventId, Long userId, String status) {
		Map<String, Object> response = new HashMap<>();
		try {

			Event event = eventManager.findEventByEventId(eventId);
			EventMember eventMember = eventManager.findEventMemberByEventIdAndUserId(eventId, userId);
			/*
			 * if (eventMember != null) {
			 * notificationManager.deleteNotificationByRecepientIdNotificationTypeId(
			 * eventMember.getUserId(), eventMember.getEventId()); }
			 */

			if ("Going".equals(status)) {
				eventMember.setStatus(MemberStatus.Going.toString());
				eventManager.deleteTimeSlotsForEventMember(event, eventMember);
				eventMember = eventManager.generateTimeSlotsForEventMember(event, eventMember);
			} else if ("NotGoing".equals(status)) {
				// if (eventMember.getStatus() != null) {
				// eventManager.updateEventMemberTimeSlot(event, eventMember);
				eventManager.deleteTimeSlotsForEventMember(event, eventMember);
				// }
				eventMember.setStatus(MemberStatus.NotGoing.toString());
			}
			eventService.saveEventMember(eventMember);
			// eventService.updateEventMemberStatus(memberStatus, eventMemberId);

			notificationManager.sendEventAcceptDeclinedPush(eventMember);

			response.put("success", true);
			response.put("message", "Status Updated Successfully");
			response.put("errorCode", 0);
			response.put("errorDetail", null);
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("message", "Error in updating status");
			response.put("errorCode", ErrorCodes.InternalServerError.ordinal());
			response.put("errorDetail", ErrorCodes.InternalServerError.toString());
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
		}

	}

	@ApiOperation(value = "Fetch User Events", notes = "Fecth user events by date and timezone", code = 200, httpMethod = "GET", produces = "application/json")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Event fetched successfuly", response = Event.class) })
	@RequestMapping(value = "/api/getEvents", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getUserEventsByDate(@RequestParam("user_id") Long userId,
			@RequestParam(value = "date", required = false, defaultValue = "") String eventDate,
			@RequestParam(value = "timezone", required = false, defaultValue = "") String timeZone,
			@RequestParam(value = "pagination_next", required = false, defaultValue = "") String paginationDate) {
		Map<String, Object> response = new HashMap<>();
		try {

			System.out.println("[USER EVENTS -  User Id : " + userId + "]");
			if (eventDate == null) {
				Date eDate = new Date();
				eventDate = CenesUtils.yyyyMMddTHHmmss.format(eDate);
			} else {
				Calendar eventCalDate = Calendar.getInstance();
				try {
					eventCalDate.setTimeInMillis(Long.valueOf(eventDate));
					eventDate = CenesUtils.yyyyMMddTHHmmss.format(eventCalDate.getTime());
				} catch (Exception e) {
					e.printStackTrace();
					eventDate = CenesUtils.yyyyMMddTHHmmss.format(CenesUtils.yyyyMMddTHHmmss.parse(eventDate));
				}
			}

			Calendar c = Calendar.getInstance();
			c.setTime(CenesUtils.yyyyMMddTHHmmss.parse(eventDate)); // Now use today date.
			c.add(Calendar.DATE, 5); // Adding 5 days
			String endDateStr = CenesUtils.yyyyMMddTHHmmss.format(c.getTime());

			List<HomeScreenDto> responseDataToSend = eventManager
					.getEventsAndRemindersMergedDataByUserIdStartDateEndDate(userId,
							CenesUtils.yyyyMMddTHHmmss.parse(eventDate), CenesUtils.yyyyMMddTHHmmss.parse(endDateStr));
			System.out.println("[USER EVENTS -  Events list : " + responseDataToSend.size() + "]");

			response.put("success", true);
			response.put("data", responseDataToSend);
			response.put("errorCode", 0);
			response.put("errorDetail", null);
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
		} catch (Exception ex) {
			ex.printStackTrace();
			response.put("success", false);
			response.put("data", new ArrayList<>());
			response.put("errorCode", ErrorCodes.InternalServerError.ordinal());
			response.put("errorDetail", ErrorCodes.InternalServerError.toString());

			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
		}
	}

	@RequestMapping(value = "/api/getEvents/v2", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> getUserEventsByDate(Long userId, Long timestamp, int pageNumber,
			int offSet) {
		Map<String, Object> response = new HashMap<>();
		try {

			System.out.println("[USER EVENTS V2 -  User Id : " + userId + "]");

			String startDateStr = CenesUtils.yyyyMMddTHHmmss.format(timestamp);

			List<HomeScreenDto> responseDataToSend = eventManager.getPageableHomeScreenData(userId, startDateStr,
					pageNumber, offSet);
			System.out.println("[USER EVENTS V2 -  Events list : " + responseDataToSend.size() + "]");

			int counts = 0;
			try {

				counts = eventServiceDao.findCountByGatheringsByUserIdAndDate(userId, startDateStr);
			} catch (Exception e) {
				e.printStackTrace();
			}

			response.put("totalCounts", counts);

			response.put("success", true);
			response.put("data", responseDataToSend);
			response.put("errorCode", 0);
			response.put("errorDetail", null);
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
		} catch (Exception ex) {
			ex.printStackTrace();
			response.put("success", false);
			response.put("data", new ArrayList<>());
			response.put("errorCode", ErrorCodes.InternalServerError.ordinal());
			response.put("errorDetail", ErrorCodes.InternalServerError.toString());

			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
		}
	}

	@RequestMapping(value = "/api/homeCalendarEvents/v2", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> getUserEventsByDate(Long userId, Long startTime, Long endTime) {
		Map<String, Object> response = new HashMap<>();
		try {

			System.out.println("[homeCalendarEvents V2 -  User Id : " + userId + "]");

			String startDateStr = CenesUtils.yyyyMMddTHHmmss.format(startTime);
			String endDateStr = CenesUtils.yyyyMMddTHHmmss.format(endTime);

			List<Map<String, Object>> events = eventServiceDao.findHomeCalendarEventsString(userId, startDateStr,
					endDateStr);

			response.put("success", true);
			response.put("data", events);
			response.put("errorCode", 0);
			response.put("errorDetail", null);
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
		} catch (Exception ex) {
			ex.printStackTrace();
			response.put("success", false);
			response.put("data", new ArrayList<>());
			response.put("errorCode", ErrorCodes.InternalServerError.ordinal());
			response.put("errorDetail", ErrorCodes.InternalServerError.toString());

			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
		}
	}

	@RequestMapping(value = "/api/events", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getEventsByPage(Long userId, Long startTime, int offSet) {
		Map<String, Object> response = new HashMap<>();
		try {

			System.out.println("[USER EVENTS -  User Id : " + userId + "]");
			Date eDate = new Date(startTime);

			/*
			 * List<HomeScreenDto> responseDataToSend =
			 * eventManager.getHomeScreenData(userId,eDate, offSet);
			 * System.out.println("[USER EVENTS -  Events list : " +
			 * responseDataToSend.size()+ "]");
			 */

			List<HomeScreenDto> responseDataToSend = eventManager.getHomeScreenData(userId, eDate, offSet);
			response.put("success", true);
			response.put("data", responseDataToSend);
			response.put("errorCode", 0);
			response.put("errorDetail", null);
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
		} catch (Exception ex) {
			ex.printStackTrace();
			response.put("success", false);
			response.put("data", new ArrayList<>());
			response.put("errorCode", ErrorCodes.InternalServerError.ordinal());
			response.put("errorDetail", ErrorCodes.InternalServerError.toString());

			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
		}
	}

	@ApiOperation(value = "Fetch User Events", notes = "Fecth user events by date and timezone", code = 200, httpMethod = "GET", produces = "application/json")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Event fetched successfuly", response = Event.class) })
	@RequestMapping(value = "/api/getEvents/user", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<Event>> getUserEvents(@RequestParam("user_id") Long userId) {
		List<Event> events = new ArrayList<Event>();
		try {

			Calendar c = Calendar.getInstance();
			c.add(Calendar.DATE, 5); // Adding 5 days
			String endDateStr = sdf.format(c.getTime());
			events = eventRepository.findByCreatedById(userId, searchDateFormat.parse(endDateStr));
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

	// Method to get Facebook Events from API.
	@RequestMapping(value = "/api/facebook/events/{facebook_id}/{access_token}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<Event>> getFacebookUserEvents(@PathVariable("facebook_id") String facebookId,
			@PathVariable("access_token") String accessToken) {
		List<Event> events = null;
		try {
			System.out.println("[Facebook Sync Events -> FacebookId : " + facebookId + ", Access Token : " + accessToken
					+ " STARTS]");

			User user = userRepository.findUserByFacebookId(facebookId);

			eventManager.deleteEventsByCreatedByIdSource(user.getUserId(), Event.EventSource.Facebook.toString());
			eventTimeSlotManager.deleteEventTimeSlotsByUserIdSource(user.getUserId(),
					Event.EventSource.Facebook.toString());

			System.out.println("[Facebook Sync Events -> FacebookId : " + facebookId + ", Access Token : " + accessToken
					+ " Old Events Deleted]");

			events = eventManager.syncFacebookEvents(facebookId, accessToken, user);

			System.out.println("[Facebook Sync Events -> FacebookId : " + facebookId + ", Access Token : " + accessToken
					+ " ENDS]");

			return new ResponseEntity<List<Event>>(events, HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			Event errorEvent = new Event();
			errorEvent.setErrorCode(ErrorCode.INTERNAL_ERROR.ordinal());
			errorEvent.setErrorDetail(ErrorCode.INTERNAL_ERROR.toString());
			events.add(errorEvent);
			return new ResponseEntity<List<Event>>(events, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Method to get Google events from API.
	@RequestMapping(value = "/api/google/events", method = RequestMethod.GET)
	public ResponseEntity<List<Event>> getGoogleEvents(@RequestParam("access_token") String accessToken,
			@RequestParam("user_id") Long userId, String serverAuthCode, String refreshToken) {

		// if (calendarSyncToken == null) {
		System.out.println("[Google Sync] Date : " + new Date() + " Getting Refresh Token Response from AuthCode");
		GoogleService googleService = new GoogleService();

		if (serverAuthCode != null) {
			JSONObject authCodeResponse = googleService.getRefreshTokenFromCode(serverAuthCode);
			System.out.println(
					"[Google Sync] Date : " + new Date() + " Response from AuthCode : " + authCodeResponse.toString());
			if (authCodeResponse != null) {
				try {
					String refToken = authCodeResponse.getString("refresh_token");
					System.out.println("[Google Sync] Date : " + new Date() + " Refresh Token : " + refToken);

					CalendarSyncToken calendarSyncToken = eventManager
							.findCalendarSyncTokenByUserIdAndAccountType(userId, CalendarSyncToken.AccountType.Google);
					if (calendarSyncToken == null) {
						System.out.println("[Google Sync] Date : " + new Date() + " New Entry");

						calendarSyncToken = new CalendarSyncToken(userId, CalendarSyncToken.AccountType.Google,
								refToken);
					} else {
						System.out.println("[Google Sync] Date : " + new Date() + " Updating Existing Entry");

						calendarSyncToken.setRefreshToken(refToken);
					}
					System.out.println("[Google Sync] Date : " + new Date() + " Saving Refresh Token : "
							+ authCodeResponse.toString());

					eventManager.saveCalendarSyncToken(calendarSyncToken);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		// }

		User user = userService.findUserById(userId);

		// eventManager.deleteEventsByCreatedByIdSourceScheduleAs(userId,
		// Event.EventSource.Google.toString(),Event.ScheduleEventAs.Event.toString());

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, Calendar.DAY_OF_MONTH - 1);
		eventManager.deleteEventsByStartTimeGreatherThanCreatedByIdAndSourceAndScheduleAs(cal.getTime(), userId,
				Event.EventSource.Google.toString(), Event.ScheduleEventAs.Event.toString());
		// eventTimeSlotManager.deleteEventTimeSlotsByUserIdSourceScheduleAs(userId,
		// Event.EventSource.Google.toString(),Event.ScheduleEventAs.Event.toString());
		eventTimeSlotManager.deleteEventTimeSlotsByUserIdSourceScheduleAs(userId, Event.EventSource.Google.toString(),
				Event.ScheduleEventAs.Event.toString());
		System.out.println("[ Syncing Google Events - User Id : " + userId + ", Access Token : " + accessToken + "]");
		List<Event> events = new ArrayList<>();
		try {
			events = eventManager.syncGoogleEvents(false, accessToken, user);
			if (events == null) {
				Event errorEvent = new Event();
				errorEvent.setErrorCode(ErrorCode.INTERNAL_ERROR.ordinal());
				errorEvent.setErrorDetail(ErrorCode.INTERNAL_ERROR.toString());
				events = new ArrayList<>();
				events.add(errorEvent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("[ Syncing Google Events - User Id : " + userId + ", Access Token : " + accessToken + "]");
		return new ResponseEntity<List<Event>>(events, HttpStatus.OK);
	}

	@RequestMapping(value = "/api/google/events/v2", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> syncGoogleEventsV2(@RequestBody Map<String, Object> syncParams) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("success", true);

		System.out.println("[Google Sync V2] Date : " + new Date() + " Getting Refresh Token Response from AuthCode");
		Long userId = Long.valueOf(syncParams.get("userId").toString());

		GoogleService googleService = new GoogleService();

		if (syncParams.containsKey("serverAuthCode") && syncParams.get("serverAuthCode") != null) {
			JSONObject authCodeResponse = googleService
					.getRefreshTokenFromCode(syncParams.get("serverAuthCode").toString());
			System.out.println("[Google Sync V2] Date : " + new Date() + " Response from AuthCode : "
					+ authCodeResponse.toString());
			if (authCodeResponse != null) {
				try {
					String refToken = authCodeResponse.getString("refresh_token");
					System.out.println("[Google Sync V2] Date : " + new Date() + " Refresh Token : " + refToken);

					CalendarSyncToken calendarSyncToken = eventManager
							.findCalendarSyncTokenByUserIdAndAccountType(userId, CalendarSyncToken.AccountType.Google);
					if (calendarSyncToken == null) {
						System.out.println("[Google Sync V2] Date : " + new Date() + " New Entry");

						calendarSyncToken = new CalendarSyncToken(userId, CalendarSyncToken.AccountType.Google,
								refToken);

						String email = syncParams.get("email").toString();
						calendarSyncToken.setEmailId(email);

					} else {
						System.out.println("[Google Sync V2] Date : " + new Date() + " Updating Existing Entry");

						calendarSyncToken.setRefreshToken(refToken);
					}
					System.out.println("[Google Sync V2] Date : " + new Date() + " Saving Refresh Token : "
							+ authCodeResponse.toString());

					eventManager.saveCalendarSyncToken(calendarSyncToken);
					response.put("data", calendarSyncToken);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		User user = userService.findUserById(userId);
		String accessToken = syncParams.get("accessToken").toString();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, Calendar.DAY_OF_MONTH - 1);
		eventManager.deleteEventsByStartTimeGreatherThanCreatedByIdAndSourceAndScheduleAs(cal.getTime(), userId,
				Event.EventSource.Google.toString(), Event.ScheduleEventAs.Event.toString());
		eventTimeSlotManager.deleteEventTimeSlotsByUserIdSourceScheduleAs(userId, Event.EventSource.Google.toString(),
				Event.ScheduleEventAs.Event.toString());
		System.out
				.println("[ Syncing Google Events V2 - User Id : " + userId + ", Access Token : " + accessToken + "]");
		List<Event> events = new ArrayList<>();
		try {
			events = eventManager.syncGoogleEvents(false, accessToken, user);
			if (events == null) {
				Event errorEvent = new Event();
				errorEvent.setErrorDetail(ErrorCode.INTERNAL_ERROR.toString());
				events = new ArrayList<>();
				events.add(errorEvent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("[ Syncing Google Events - User Id : " + userId + ", Access Token : " + accessToken + "]");
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/api/google/refreshEvents", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<Event>> getGoogleEvents(Long userId) {

		System.out.println("[Google Refresh : User ID : " + userId + "]");
		CalendarSyncToken calendarSyncToken = eventManager.findCalendarSyncTokenByUserIdAndAccountType(userId,
				CalendarSyncToken.AccountType.Google);

		if (calendarSyncToken != null) {
			System.out
					.println("[Google Sync] Date : " + new Date() + " Getting Access Token Response from RefreshToken");
			GoogleService googleService = new GoogleService();
			JSONObject refreshTokenResponse = googleService
					.getAccessTokenFromRefreshToken(calendarSyncToken.getRefreshToken());
			System.out.println("[Google Sync] Date : " + new Date() + " Response from Refresh Token : "
					+ refreshTokenResponse.toString());
			try {
				if (refreshTokenResponse != null) {

					String accessToken = refreshTokenResponse.getString("access_token");

					User user = userService.findUserById(userId);

					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.DAY_OF_MONTH, Calendar.DAY_OF_MONTH - 1);
					eventManager.deleteEventsByStartTimeGreatherThanCreatedByIdAndSourceAndScheduleAs(cal.getTime(),
							userId, Event.EventSource.Google.toString(), Event.ScheduleEventAs.Event.toString());
					eventTimeSlotManager.deleteEventTimeSlotsByUserIdSourceScheduleAs(userId,
							Event.EventSource.Google.toString(), Event.ScheduleEventAs.Event.toString());

					System.out.println("[ Syncing Google Refreshing Events - User Id : " + userId + ", Access Token : "
							+ accessToken + "]");
					List<Event> events = eventManager.syncGoogleEvents(false, accessToken, user);
					if (events == null) {
						Event errorEvent = new Event();
						errorEvent.setErrorCode(ErrorCode.INTERNAL_ERROR.ordinal());
						errorEvent.setErrorDetail(ErrorCode.INTERNAL_ERROR.toString());
						events = new ArrayList<>();
						events.add(errorEvent);
					}
					System.out.println("[ Refreshing Google Events - User Id : " + userId + ", Access Token : "
							+ accessToken + "]");
					return new ResponseEntity<List<Event>>(events, HttpStatus.OK);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return new ResponseEntity<List<Event>>(new ArrayList<>(), HttpStatus.OK);
			}
		}

		System.out.println("[ Syncing Google Events ENDS]");
		return new ResponseEntity<List<Event>>(new ArrayList<>(), HttpStatus.OK);
	}

	// Method to get Google events from API.
	@RequestMapping(value = "/api/holiday/calendar/events", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<Event>> syncGoogleHolidayCalendar(@RequestParam("calendar_id") String calendarId,
			@RequestParam("user_id") Long userId) {

		System.out.println(
				"[ Holiday Calendar Events Sync - User Id : " + userId + ", Calendar Id : " + calendarId + " STARTS]");

		User user = userRepository.findOne(userId);

		List<Event> events = eventManager.syncHolidays(calendarId, user);
		return new ResponseEntity<List<Event>>(events, HttpStatus.OK);
	}

	@RequestMapping(value = "/api/holiday/calendar/events/v2", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> syncGoogleHolidayCalendar(@RequestBody Map<String, Object> postData) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("success", true);

		Long userId = Long.valueOf(postData.get("userId").toString());
		String calendarId = postData.get("calendarId").toString();
		System.out.println("[ Holiday Calendar Events Sync V2  - User Id : " + userId + ", Calendar Id : " + calendarId
				+ " STARTS]");

		CalendarSyncToken calendarSyncToken = eventManager.findCalendarSyncTokenByUserIdAndAccountType(userId,
				CalendarSyncToken.AccountType.Holiday);
		if (calendarSyncToken == null) {
			System.out.println("[Holiday Sync V2] Date : " + new Date() + " New Entry");
			calendarSyncToken = new CalendarSyncToken(userId, CalendarSyncToken.AccountType.Holiday,
					postData.get("calendarId").toString());
		} else {
			calendarSyncToken.setRefreshToken(postData.get("calendarId").toString());
		}
		String email = postData.get("name").toString();
		calendarSyncToken.setEmailId(email);
		eventManager.saveCalendarSyncToken(calendarSyncToken);

		User user = userRepository.findOne(userId);

		List<Event> events = eventManager.syncHolidays(calendarId, user);
		response.put("data", events);

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/api/predictive/calendar", method = RequestMethod.GET)
	public ResponseEntity<List<PredictiveCalendar>> predictiveCalendarData(Long userId,
			@RequestParam("start_time") Long eventStartTime, @RequestParam("end_time") Long eventEndTime,
			@RequestParam(value = "friends", required = false, defaultValue = "") String selectedFriendIds) {
		System.out.println("[Predictive Calendar URL : /api/predictive/calendar?userId=" + userId + "&start_time="
				+ eventStartTime + "&end_time=" + eventEndTime + "&friends=" + selectedFriendIds);
		System.out.println("[Predictive Calendar -> Date : " + new Date() + ", User ID : " + userId + ", Start Time : "
				+ eventStartTime + ", End Time : " + eventEndTime + " STARTS]");
		int slotsInMinutes = 5;
		List<PredictiveCalendar> predictiveCalendarDateWise = new ArrayList<>();

		// Find all Friends Of User by its id which is friend id
		// List<UserFriend> friends =
		// userFriendRepository.findByFriendId(friendId, null);

		int totalFriends = 0;
		boolean friendsExists = false;
		String userIds = "";
		if (selectedFriendIds.length() > 0) {
			selectedFriendIds += "," + userId;
		} else {
			selectedFriendIds = userId + "";
		}
		if (selectedFriendIds != "" && selectedFriendIds.length() > 0) {
			userIds = selectedFriendIds;
			totalFriends = selectedFriendIds.split(",").length;
			friendsExists = true;
		} else {

			List<User> friends = (List) userRepository.findAll();
			if (friends != null && friends.size() > 0) {
				List<Long> friendUserIds = new ArrayList<>();
				/*
				 * for (UserFriend userFriend : friends) { userIds +=
				 * userFriend.getUserId()+","; friendUserIds.add(userFriend.getUserId()); }
				 */
				totalFriends = friends.size() - 1;
				String users = "";
				for (User user : friends) {
					if (user.getUserId().equals(userId)) {
						continue;
					}
					users += user.getUserId() + ",";
					friendUserIds.add(user.getUserId());
				}
				userIds = users.substring(0, users.length() - 1);
				friendsExists = true;
			}
		}

		// select * from event_time_slots where DATE(event_date) >= DATE('2018-01-01')
		// and DATE(event_date) <= '2018-01-05' and HOUR(event_date) >= HOUR('2018-01-01
		// 02:00:00') and HOUR(event_date) <= HOUR('2018-01-01 03:00:00') and user_id in
		// (149,150);

		if (friendsExists) {

			Calendar searchCalDate = Calendar.getInstance();
			searchCalDate.setTimeInMillis(eventStartTime);

			// Now we have to divide the time of event to be created by user
			// into slots of milliseconds. we will then check other users slots
			// to match these event slots.
			List<Long> createEventTimeSlotList = CenesUtils.divideTimeIntoMinuteSlots(new Date(eventStartTime),
					new Date(eventEndTime), slotsInMinutes);

			// Lets Calculate the hours list from time slots.
			List<String> hoursList = new ArrayList<>();
			for (Long timeSlot : createEventTimeSlotList) {
				hoursList.add(CenesUtils.hhmm.format(new Date(timeSlot)));
			}

			// We will create the month's start date
			// and month's end date
			// and find all the event slots within this date
			Calendar monthStartTimeCalendar = Calendar.getInstance();
			monthStartTimeCalendar.setTime(searchCalDate.getTime());
			monthStartTimeCalendar.add(Calendar.MONTH, -1);
			monthStartTimeCalendar.set(Calendar.DAY_OF_MONTH,
					monthStartTimeCalendar.getActualMaximum(Calendar.DAY_OF_MONTH) - 1);

			Calendar monthEndTimeCalendar = Calendar.getInstance();
			monthEndTimeCalendar.setTime(searchCalDate.getTime());
			monthEndTimeCalendar.add(Calendar.MONTH, 1);
			monthEndTimeCalendar.set(Calendar.DAY_OF_MONTH,
					monthEndTimeCalendar.getActualMinimum(Calendar.DAY_OF_MONTH));

			Calendar currentMonthCal = Calendar.getInstance();
			currentMonthCal.setTime(searchCalDate.getTime());

			// Lets create list of all the dates in month
			// We will then use this to check which is returned by DB and which
			// is not.
			List<String> monthlyDates = new ArrayList<>();
			for (int i = 0; i < currentMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH) + 2; i++) {
				monthlyDates.add(CenesUtils.yyyyMMdd
						.format(CenesUtils.getDateAfterAddingDays(monthStartTimeCalendar.getTime(), i)));
			}
			try {

				monthStartTimeCalendar.set(Calendar.HOUR, 0);
				monthStartTimeCalendar.set(Calendar.MINUTE, 0);
				monthStartTimeCalendar.set(Calendar.SECOND, 0);
				monthStartTimeCalendar.set(Calendar.MILLISECOND, 0);
				String sDateStr = CenesUtils.yyyyMMddTHHmmss.format(monthStartTimeCalendar.getTime());

				monthStartTimeCalendar.set(Calendar.HOUR, 23);
				monthStartTimeCalendar.set(Calendar.MINUTE, 59);
				monthStartTimeCalendar.set(Calendar.SECOND, 59);
				monthStartTimeCalendar.set(Calendar.MILLISECOND, 999);
				String eDateStr = CenesUtils.yyyyMMddTHHmmss.format(monthEndTimeCalendar.getTime());

				Long dbFetchStartTime = new Date().getTime();

				// List<Long> friendAttending = new ArrayList<>();
				Map<String, String> dateFriendsAvailabilityMap = new HashMap<String, String>();
				Map<String, Long> dateHoursMap = new HashMap<String, Long>();

				for (String friendId : userIds.split(",")) {
					List<EventTimeSlot> eventTimeSlots = eventServiceDao
							.getFreeTimeSlotsByUserIdAndStartDateAndEndDate(Long.valueOf(friendId), sDateStr, eDateStr);
					Map<String, List<EventTimeSlot>> etsDateWiseMap = new HashMap<>();

					for (EventTimeSlot etsFromList : eventTimeSlots) {

						List<EventTimeSlot> etsMapList = null;
						if (etsDateWiseMap.containsKey(CenesUtils.yyyyMMdd.format(etsFromList.getEventDate()))) {
							etsMapList = etsDateWiseMap.get(CenesUtils.yyyyMMdd.format(etsFromList.getEventDate()));
							etsMapList.add(etsFromList);
						} else {
							etsMapList = new ArrayList<>();
							etsMapList.add(etsFromList);
						}
						etsDateWiseMap.put(CenesUtils.yyyyMMdd.format(etsFromList.getEventDate()), etsMapList);
					}

					for (String mDate : monthlyDates) {
						System.out.println(mDate);
						Calendar cal = Calendar.getInstance();
						cal.setTime(CenesUtils.yyyyMMdd.parse(mDate));
						cal.set(Calendar.HOUR_OF_DAY, searchCalDate.get(Calendar.HOUR_OF_DAY));
						cal.set(Calendar.MINUTE, searchCalDate.get(Calendar.MINUTE));
						cal.set(Calendar.SECOND, 0);

						if (etsDateWiseMap.containsKey(mDate)) {
							List<EventTimeSlot> userTimeSlots = etsDateWiseMap.get(mDate);

							Boolean slotExistsBetweenTimeRange = false;
							Long freeFriendIdForSlotsNotExistsInRange = null;
							Set<Long> freeFriends = new HashSet<>();
							List<String> checkForNumberOfSlotsUserIsBusy = new ArrayList<>();
							for (EventTimeSlot userEts : userTimeSlots) {

								freeFriendIdForSlotsNotExistsInRange = userEts.getUserId();
								// System.out.println("User Id : "+userEts.getUserId()+", Status :
								// "+userEts.getStatus());
								// if (hoursList.contains(CenesUtils.hhmm.format(userEts.getStartTime())) &&
								// userEts.getStatus().equals(TimeSlotStatus.Free.toString())) {
								// System.out.println(CenesUtils.hhmm.format(userEts.getStartTime()));
								System.out.println(
										"Time In Hour Format : " + CenesUtils.hhmm.format(userEts.getStartTime()));
								// System.out.println("userEts.getStatus() : "+userEts.getStatus());
								if (hoursList.contains(CenesUtils.hhmm.format(userEts.getStartTime()))) {
									slotExistsBetweenTimeRange = true;
									System.out.println("Friend Found.." + userEts.getStatus());
									checkForNumberOfSlotsUserIsBusy.add(userEts.getStatus());
									if (userEts.getStatus().equals(EventTimeSlot.TimeSlotStatus.Free.toString())) {
										// This is to check if the events are of date yyyy-MM-dd
										if (CenesUtils.yyyyMMdd.format(eventStartTime)
												.equals(CenesUtils.yyyyMMdd.format(eventEndTime))) {
											// freeFriends.add(userEts.getUserId());
											slotExistsBetweenTimeRange = false;
											break;
										} else if (CenesUtils.yyyyMMdd.format(userEts.getEventStartTime())
												.equals(mDate)) {
											// freeFriends.add(userEts.getUserId());
											slotExistsBetweenTimeRange = false;
											break;
										}
									}

								}
								// setOfUsersWithTimeSlots.add(userEts.getUserId());
							}

							if (slotExistsBetweenTimeRange && checkForNumberOfSlotsUserIsBusy.size() < 2) {
								slotExistsBetweenTimeRange = false;
							}

							if (!slotExistsBetweenTimeRange) {
								// System.out.println("slotExistsBetweenTimeRange NOooooooo");
								// freeFriends.add(freeFriendIdForSlotsNotExistsInRange);
								String userAvailabilityIds = "";
								if (dateFriendsAvailabilityMap.containsKey(mDate)) {
									userAvailabilityIds = dateFriendsAvailabilityMap.get(mDate);
									userAvailabilityIds += "," + userId;
								} else {
									userAvailabilityIds += userId;
								}
								dateFriendsAvailabilityMap.put(mDate, userAvailabilityIds);
							}

							// System.out.println("totalFriends : "+totalFriends+", Busy Friends :
							// "+setOfUsersWithTimeSlots.size());
							// int totalFriendsComing = freeFriends.size() + (totalFriends -
							// freeFriends.size() - setOfUsersWithTimeSlots.size());

							// If User is single and looking for his time slots then we will make total
							// coming to total friends

							/*
							 * System.out.println("Total Friends: "+totalFriends+" && Friend Size : "
							 * +freeFriends.size()); int totalFriendsComing = freeFriends.size();
							 * 
							 * 
							 * float predictivePercentage = Math.abs((Float.valueOf(totalFriendsComing) /
							 * Float.valueOf(totalFriends)) * 100);
							 * 
							 * PredictiveCalendar pc = new PredictiveCalendar(); pc.setReadableDate(new
							 * SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTimeInMillis()));
							 * pc.setDate(cal.getTimeInMillis()); pc.setTotalFriends(totalFriends);
							 * pc.setAttendingFriends(totalFriendsComing);
							 * pc.setPredictivePercentage((int)predictivePercentage);
							 * predictiveCalendarDateWise.add(pc);
							 */

						} else {
							String userAvailabilityIds = "";
							if (dateFriendsAvailabilityMap.containsKey(mDate)) {
								userAvailabilityIds = dateFriendsAvailabilityMap.get(mDate);
								userAvailabilityIds += "," + userId;
							} else {
								userAvailabilityIds += userId;
							}
							dateFriendsAvailabilityMap.put(mDate, userAvailabilityIds);
						}

					}

				}
				for (String mDate : monthlyDates) {

					String availableUserIds = dateFriendsAvailabilityMap.get(mDate);
					int friendsAttending = 0;
					if (availableUserIds != null) {
						friendsAttending = availableUserIds.split(",").length;
					}

					float predictivePercentage = Math
							.abs((Float.valueOf(friendsAttending) / Float.valueOf(totalFriends)) * 100);

					Date dd = new SimpleDateFormat("yyyy-MM-dd").parse(mDate);
					Calendar montlyDateCal = Calendar.getInstance();
					montlyDateCal.setTime(dd);

					Calendar dateCal = Calendar.getInstance();
					dateCal.setTimeInMillis(eventStartTime);
					dateCal.set(Calendar.DAY_OF_MONTH, montlyDateCal.get(Calendar.DAY_OF_MONTH));
					dateCal.set(Calendar.MONTH, montlyDateCal.get(Calendar.MONTH));
					// System.out.println("Event Date After adding mDate : "+dateCal.getTime());

					System.out.println(dateCal.getTimeInMillis() + " - MIllis, Day : "
							+ dateCal.get(Calendar.DAY_OF_MONTH) + ", MONTH : " + dateCal.get(Calendar.MONTH)
							+ " Predictive Percentage : " + predictivePercentage);
					PredictiveCalendar pc = new PredictiveCalendar();
					pc.setReadableDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(montlyDateCal.getTime()));
					pc.setDate(dateCal.getTimeInMillis());
					pc.setTotalFriends(totalFriends);
					pc.setAttendingFriends(friendsAttending);
					pc.setPredictivePercentage((int) predictivePercentage);
					predictiveCalendarDateWise.add(pc);

				}

				System.out.println("[Predictive Calendar : Date : " + new Date() + ", Logic Ends");
				// dbFetchEndTime = new Date().getTime();
				// System.out.println("[Predictive Calendar : Date : "+new Date()+", Total Time
				// Taken By Logic "+(dbFetchEndTime - dbFetchStartTime)/1000+" seconds");

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("[Predictive Calendar -> Date : " + new Date() + ", User ID : " + userId + ", Start Time : "
				+ eventStartTime + ", End Time : " + eventEndTime + " ENDS]");
		return new ResponseEntity<List<PredictiveCalendar>>(predictiveCalendarDateWise, HttpStatus.OK);
	}

	@RequestMapping(value = "/api/predictive/calendar/v2", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> predictiveCalendarDataV2(Long userId, Long startTime, Long endTime,
			String friends) {
		System.out.println("[Predictive Calendar URL : /api/predictive/calendar/v2?userId=" + userId + "&startTime="
				+ startTime + "&endTime=" + endTime + "&friends=" + friends);
		System.out.println("[Predictive Calendar -> Date : " + new Date() + ", User ID : " + userId + ", Start Time : "
				+ startTime + ", End Time : " + endTime + " STARTS]");

		Map<String, Object> response = new HashMap();
		response.put("success", true);

		int slotsInMinutes = 5;
		List<PredictiveCalendar> predictiveCalendarDateWise = new ArrayList<>();

		// Find all Friends Of User by its id which is friend id
		// List<UserFriend> friends =
		// userFriendRepository.findByFriendId(friendId, null);

		int totalFriends = 0;
		boolean friendsExists = false;
		String userIds = "";
		if (friends != null && friends.length() > 0) {
			friends += "," + userId;
		} else {
			friends = userId + "";
		}
		if (friends != null && friends != "" && friends.length() > 0) {
			userIds = friends;
			totalFriends = friends.split(",").length;
			friendsExists = true;
		} /*
			 * else {
			 * 
			 * List<User> friends = (List) userRepository.findAll(); if (friends != null &&
			 * friends.size() > 0) { List<Long> friendUserIds = new ArrayList<>(); /* for
			 * (UserFriend userFriend : friends) { userIds += userFriend.getUserId()+",";
			 * friendUserIds.add(userFriend.getUserId()); }
			 */
		/*
		 * totalFriends = friends.size() - 1; String users = ""; for (User user :
		 * friends) { if (user.getUserId().equals(userId)) { continue; } users +=
		 * user.getUserId() + ","; friendUserIds.add(user.getUserId()); } userIds =
		 * users.substring(0,users.length()-1); friendsExists = true; } }
		 */

		// select * from event_time_slots where DATE(event_date) >= DATE('2018-01-01')
		// and DATE(event_date) <= '2018-01-05' and HOUR(event_date) >= HOUR('2018-01-01
		// 02:00:00') and HOUR(event_date) <= HOUR('2018-01-01 03:00:00') and user_id in
		// (149,150);

		if (friendsExists) {

			Calendar searchCalDate = Calendar.getInstance();
			searchCalDate.setTimeInMillis(startTime);

			// Now we have to divide the time of event to be created by user
			// into slots of milliseconds. we will then check other users slots
			// to match these event slots.
			List<Long> createEventTimeSlotList = CenesUtils.divideTimeIntoMinuteSlots(new Date(startTime),
					new Date(endTime), slotsInMinutes);

			// Lets Calculate the hours list from time slots.
			List<String> hoursList = new ArrayList<>();
			for (Long timeSlot : createEventTimeSlotList) {
				hoursList.add(CenesUtils.hhmm.format(new Date(timeSlot)));
			}

			// We will create the month's start date
			// and month's end date
			// and find all the event slots within this date
			Calendar monthStartTimeCalendar = Calendar.getInstance();
			monthStartTimeCalendar.setTime(searchCalDate.getTime());
			monthStartTimeCalendar.add(Calendar.MONTH, -1);
			monthStartTimeCalendar.set(Calendar.DAY_OF_MONTH,
					monthStartTimeCalendar.getActualMaximum(Calendar.DAY_OF_MONTH) - 1);

			Calendar monthEndTimeCalendar = Calendar.getInstance();
			monthEndTimeCalendar.setTime(searchCalDate.getTime());
			monthEndTimeCalendar.add(Calendar.MONTH, 1);
			monthEndTimeCalendar.set(Calendar.DAY_OF_MONTH,
					monthEndTimeCalendar.getActualMinimum(Calendar.DAY_OF_MONTH));

			Calendar currentMonthCal = Calendar.getInstance();
			currentMonthCal.setTime(searchCalDate.getTime());

			// Lets create list of all the dates in month
			// We will then use this to check which is returned by DB and which
			// is not.
			List<String> monthlyDates = new ArrayList<>();
			for (int i = 0; i < currentMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH) + 2; i++) {
				monthlyDates.add(CenesUtils.yyyyMMdd
						.format(CenesUtils.getDateAfterAddingDays(monthStartTimeCalendar.getTime(), i)));
			}
			try {

				monthStartTimeCalendar.set(Calendar.HOUR, 0);
				monthStartTimeCalendar.set(Calendar.MINUTE, 0);
				monthStartTimeCalendar.set(Calendar.SECOND, 0);
				monthStartTimeCalendar.set(Calendar.MILLISECOND, 0);
				String sDateStr = CenesUtils.yyyyMMddTHHmmss.format(monthStartTimeCalendar.getTime());

				monthStartTimeCalendar.set(Calendar.HOUR, 23);
				monthStartTimeCalendar.set(Calendar.MINUTE, 59);
				monthStartTimeCalendar.set(Calendar.SECOND, 59);
				monthStartTimeCalendar.set(Calendar.MILLISECOND, 999);
				String eDateStr = CenesUtils.yyyyMMddTHHmmss.format(monthEndTimeCalendar.getTime());

				Long dbFetchStartTime = new Date().getTime();

				// List<Long> friendAttending = new ArrayList<>();
				Map<String, String> dateFriendsAvailabilityMap = new HashMap<String, String>();
				Map<String, Long> dateHoursMap = new HashMap<String, Long>();

				for (String friendId : userIds.split(",")) {
					List<EventTimeSlot> eventTimeSlots = eventServiceDao
							.getFreeTimeSlotsByUserIdAndStartDateAndEndDate(Long.valueOf(friendId), sDateStr, eDateStr);
					Map<String, List<EventTimeSlot>> etsDateWiseMap = new HashMap<>();

					for (EventTimeSlot etsFromList : eventTimeSlots) {

						List<EventTimeSlot> etsMapList = null;
						if (etsDateWiseMap.containsKey(CenesUtils.yyyyMMdd.format(etsFromList.getEventDate()))) {
							etsMapList = etsDateWiseMap.get(CenesUtils.yyyyMMdd.format(etsFromList.getEventDate()));
							etsMapList.add(etsFromList);
						} else {
							etsMapList = new ArrayList<>();
							etsMapList.add(etsFromList);
						}
						etsDateWiseMap.put(CenesUtils.yyyyMMdd.format(etsFromList.getEventDate()), etsMapList);
					}

					for (String mDate : monthlyDates) {
						System.out.println(mDate);
						Calendar cal = Calendar.getInstance();
						cal.setTime(CenesUtils.yyyyMMdd.parse(mDate));
						cal.set(Calendar.HOUR_OF_DAY, searchCalDate.get(Calendar.HOUR_OF_DAY));
						cal.set(Calendar.MINUTE, searchCalDate.get(Calendar.MINUTE));
						cal.set(Calendar.SECOND, 0);

						if (etsDateWiseMap.containsKey(mDate)) {
							List<EventTimeSlot> userTimeSlots = etsDateWiseMap.get(mDate);

							Boolean slotExistsBetweenTimeRange = false;
							Long freeFriendIdForSlotsNotExistsInRange = null;
							Set<Long> freeFriends = new HashSet<>();
							List<String> checkForNumberOfSlotsUserIsBusy = new ArrayList<>();
							for (EventTimeSlot userEts : userTimeSlots) {

								freeFriendIdForSlotsNotExistsInRange = userEts.getUserId();
								// System.out.println("User Id : "+userEts.getUserId()+", Status :
								// "+userEts.getStatus());
								// if (hoursList.contains(CenesUtils.hhmm.format(userEts.getStartTime())) &&
								// userEts.getStatus().equals(TimeSlotStatus.Free.toString())) {
								// System.out.println(CenesUtils.hhmm.format(userEts.getStartTime()));
								System.out.println(
										"Time In Hour Format : " + CenesUtils.hhmm.format(userEts.getStartTime()));
								// System.out.println("userEts.getStatus() : "+userEts.getStatus());
								if (hoursList.contains(CenesUtils.hhmm.format(userEts.getStartTime()))) {
									slotExistsBetweenTimeRange = true;
									System.out.println("Friend Found.." + userEts.getStatus());
									checkForNumberOfSlotsUserIsBusy.add(userEts.getStatus());
									if (userEts.getStatus().equals(EventTimeSlot.TimeSlotStatus.Free.toString())) {
										// This is to check if the events are of date yyyy-MM-dd
										if (CenesUtils.yyyyMMdd.format(startTime)
												.equals(CenesUtils.yyyyMMdd.format(endTime))) {
											// freeFriends.add(userEts.getUserId());
											slotExistsBetweenTimeRange = false;
											break;
										} else if (CenesUtils.yyyyMMdd.format(userEts.getEventStartTime())
												.equals(mDate)) {
											// freeFriends.add(userEts.getUserId());
											slotExistsBetweenTimeRange = false;
											break;
										}
									}

								}
								// setOfUsersWithTimeSlots.add(userEts.getUserId());
							}

							if (slotExistsBetweenTimeRange && checkForNumberOfSlotsUserIsBusy.size() < 2) {
								slotExistsBetweenTimeRange = false;
							}

							if (!slotExistsBetweenTimeRange) {
								// System.out.println("slotExistsBetweenTimeRange NOooooooo");
								// freeFriends.add(freeFriendIdForSlotsNotExistsInRange);
								String userAvailabilityIds = "";
								if (dateFriendsAvailabilityMap.containsKey(mDate)) {
									userAvailabilityIds = dateFriendsAvailabilityMap.get(mDate);
									userAvailabilityIds += "," + userId;
								} else {
									userAvailabilityIds += userId;
								}
								dateFriendsAvailabilityMap.put(mDate, userAvailabilityIds);
							}

							// System.out.println("totalFriends : "+totalFriends+", Busy Friends :
							// "+setOfUsersWithTimeSlots.size());
							// int totalFriendsComing = freeFriends.size() + (totalFriends -
							// freeFriends.size() - setOfUsersWithTimeSlots.size());

							// If User is single and looking for his time slots then we will make total
							// coming to total friends

							/*
							 * System.out.println("Total Friends: "+totalFriends+" && Friend Size : "
							 * +freeFriends.size()); int totalFriendsComing = freeFriends.size();
							 * 
							 * 
							 * float predictivePercentage = Math.abs((Float.valueOf(totalFriendsComing) /
							 * Float.valueOf(totalFriends)) * 100);
							 * 
							 * PredictiveCalendar pc = new PredictiveCalendar(); pc.setReadableDate(new
							 * SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTimeInMillis()));
							 * pc.setDate(cal.getTimeInMillis()); pc.setTotalFriends(totalFriends);
							 * pc.setAttendingFriends(totalFriendsComing);
							 * pc.setPredictivePercentage((int)predictivePercentage);
							 * predictiveCalendarDateWise.add(pc);
							 */

						} else {
							String userAvailabilityIds = "";
							if (dateFriendsAvailabilityMap.containsKey(mDate)) {
								userAvailabilityIds = dateFriendsAvailabilityMap.get(mDate);
								userAvailabilityIds += "," + userId;
							} else {
								userAvailabilityIds += userId;
							}
							dateFriendsAvailabilityMap.put(mDate, userAvailabilityIds);
						}

					}

				}
				for (String mDate : monthlyDates) {

					String availableUserIds = dateFriendsAvailabilityMap.get(mDate);
					int friendsAttending = 0;
					if (availableUserIds != null) {
						friendsAttending = availableUserIds.split(",").length;
					}

					float predictivePercentage = Math
							.abs((Float.valueOf(friendsAttending) / Float.valueOf(totalFriends)) * 100);

					Date dd = new SimpleDateFormat("yyyy-MM-dd").parse(mDate);
					Calendar montlyDateCal = Calendar.getInstance();
					montlyDateCal.setTime(dd);

					Calendar dateCal = Calendar.getInstance();
					dateCal.setTimeInMillis(startTime);
					dateCal.set(Calendar.DAY_OF_MONTH, montlyDateCal.get(Calendar.DAY_OF_MONTH));
					dateCal.set(Calendar.MONTH, montlyDateCal.get(Calendar.MONTH));
					// System.out.println("Event Date After adding mDate : "+dateCal.getTime());

					System.out.println(dateCal.getTimeInMillis() + " - MIllis, Day : "
							+ dateCal.get(Calendar.DAY_OF_MONTH) + ", MONTH : " + dateCal.get(Calendar.MONTH)
							+ " Predictive Percentage : " + predictivePercentage);
					PredictiveCalendar pc = new PredictiveCalendar();
					pc.setReadableDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(montlyDateCal.getTime()));
					pc.setDate(dateCal.getTimeInMillis());
					pc.setTotalFriends(totalFriends);
					pc.setAttendingFriends(friendsAttending);
					pc.setPredictivePercentage((int) predictivePercentage);
					predictiveCalendarDateWise.add(pc);

				}

				System.out.println("[Predictive Calendar : Date : " + new Date() + ", Logic Ends");
				// dbFetchEndTime = new Date().getTime();
				// System.out.println("[Predictive Calendar : Date : "+new Date()+", Total Time
				// Taken By Logic "+(dbFetchEndTime - dbFetchStartTime)/1000+" seconds");

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		response.put("data", predictiveCalendarDateWise);

		System.out.println("[Predictive Calendar -> Date : " + new Date() + ", User ID : " + userId + ", Start Time : "
				+ startTime + ", End Time : " + endTime + " ENDS]");
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/api/event/upload", method = RequestMethod.POST)
	public ResponseEntity<String> uploadImages(MultipartFile uploadfile, Long eventId) {

		Event event = eventManager.findEventByEventId(eventId);

		InputStream inputStream = null;
		OutputStream outputStream = null;
		String extension = uploadfile.getOriginalFilename().substring(
				uploadfile.getOriginalFilename().trim().lastIndexOf("."), uploadfile.getOriginalFilename().length());

		String fileName = UUID.randomUUID().toString() + extension;

		String dirPath = eventUploadPath + "large/";
		File f = new File(dirPath);
		if (!f.exists()) {
			try {
				f.mkdirs();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		File newFile = new File(dirPath + fileName);
		try {
			inputStream = uploadfile.getInputStream();

			if (!newFile.exists()) {
				newFile.createNewFile();
			}
			outputStream = new FileOutputStream(newFile);
			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}

			String eventImageUrl = domain + "/assets/uploads/events/large/" + fileName;
			event.setEventPicture(eventImageUrl);

			try {
				File originalFile = new File(dirPath + fileName);

				String thumnailDirPath = eventUploadPath + "thumbnail/";
				File thumbnailFile = new File(thumnailDirPath);
				if (!thumbnailFile.exists()) {
					try {
						thumbnailFile.mkdirs();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				String thumbnailPath = eventUploadPath + "thumbnail/" + fileName;

				Thumbnails.of(originalFile).size(250, 250).outputFormat("jpg").toFile(thumbnailPath);

				String thumbnailImageUrl = domain + "/assets/uploads/events/thumbnail/" + fileName;
				event.setThumbnail(thumbnailImageUrl);
			} catch (Exception e) {
				e.printStackTrace();
			}

			event = eventManager.saveUpdateEvent(event);

			JSONObject jobj = new JSONObject();
			jobj.put("success", true);
			jobj.put("eventPicture", eventImageUrl);
			return new ResponseEntity<String>(jobj.toString(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			JSONObject jobj = new JSONObject();
			try {
				jobj.put("errorCode", HttpStatus.NOT_ACCEPTABLE.ordinal());
				jobj.put("errorDetail", HttpStatus.NOT_ACCEPTABLE.toString());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return new ResponseEntity<String>(jobj.toString(), HttpStatus.OK);
		}
	}

	// Method to get Outlook events from API.
	@RequestMapping(value = "/api/outlook/events", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<Event>> getOutlookEvents(@RequestParam("access_token") String accessToken,
			@RequestParam("user_id") Long userId, String refreshToken) {
		System.out.println("[ Syncing Outlook Events - User Id : " + userId + ", Access Token : " + accessToken + "]");
		User user = userService.findUserById(userId);
		List<Event> events = null;
		try {

			CalendarSyncToken calendarSyncToken = eventManager.findCalendarSyncTokenByUserIdAndAccountType(userId,
					CalendarSyncToken.AccountType.Outlook);
			if (calendarSyncToken == null) {
				System.out.println("[ Syncing Outlook Events - User Id : " + userId + ", New Token]");

				calendarSyncToken = new CalendarSyncToken(userId, CalendarSyncToken.AccountType.Outlook, refreshToken);
			} else {
				System.out.println("[ Syncing Outlook Events - User Id : " + userId + ", Existing Token]");
				calendarSyncToken.setRefreshToken(refreshToken);
			}
			System.out.println("[ Syncing Outlook Events - User Id : " + userId + ", Saving Token]");
			eventManager.saveCalendarSyncToken(calendarSyncToken);

			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.DAY_OF_MONTH, Calendar.DAY_OF_MONTH - 1);
			eventManager.deleteEventsByStartTimeGreatherThanCreatedByIdAndSourceAndScheduleAs(cal.getTime(), userId,
					Event.EventSource.Outlook.toString(), Event.ScheduleEventAs.Event.toString());
			eventTimeSlotManager.deleteEventTimeSlotsByUserIdSource(userId, Event.EventSource.Outlook.toString());

			OutlookService os = new OutlookService();
			List<OutlookEvents> outlookEventList = os.getOutlookCalenderEvents(accessToken);
			if (outlookEventList != null && outlookEventList.size() > 0) {
				System.out.println("Outlook Calendar events size : " + outlookEventList.size());
				events = eventManager.populateOutlookEventsInCenes(outlookEventList, user);
				System.out.println("Events to Sync : " + events.size());

			}
			CenesProperty cenesProperty = eventService.findCenesPropertyByNameAndOwner("outlook_calendar",
					PropertyOwningEntity.User);
			if (cenesProperty != null) {
				CenesPropertyValue cenesPropertyValue = new CenesPropertyValue();
				cenesPropertyValue.setCenesProperty(cenesProperty);
				cenesPropertyValue.setDateValue(new Date());
				cenesPropertyValue.setEntityId(userId);
				cenesPropertyValue.setOwningEntity(PropertyOwningEntity.User);
				cenesPropertyValue.setValue("true");
				eventService.saveCenesPropertyValue(cenesPropertyValue);
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
		return new ResponseEntity<List<Event>>(events, HttpStatus.OK);
	}

	// Method to get Outlook events from API.
	@RequestMapping(value = "/api/outlook/events/v2", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Map<String, Object>> syncOutlookEventsV2(@RequestBody Map<String, Object> postData) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("success", true);

		Long userId = Long.valueOf(postData.get("userId").toString());
		System.out.println("[ Syncing Outlook Events V2 - User Id : " + userId + ", Access Token : "
				+ postData.get("accessToken").toString() + "]");
		User user = userService.findUserById(userId);
		List<Event> events = null;
		try {

			CalendarSyncToken calendarSyncToken = eventManager.findCalendarSyncTokenByUserIdAndAccountType(userId,
					CalendarSyncToken.AccountType.Outlook);
			if (calendarSyncToken == null) {
				System.out.println("[ Syncing Outlook Events - User Id : " + userId + ", New Token]");

				calendarSyncToken = new CalendarSyncToken(userId, CalendarSyncToken.AccountType.Outlook,
						postData.get("refreshToken").toString());
			} else {
				System.out.println("[ Syncing Outlook Events - User Id : " + userId + ", Existing Token]");
				calendarSyncToken.setRefreshToken(postData.get("refreshToken").toString());
			}
			System.out.println("[ Syncing Outlook Events - User Id : " + userId + ", Saving Token]");
			calendarSyncToken.setEmailId(postData.get("email").toString());
			
			
			OutlookService oservice = new OutlookService();
			JSONObject subScribeJson = oservice.subscribeToCalendarNotifications(postData.get("accessToken").toString());
			if (subScribeJson != null) {
				
				//This is for IOS
				if (subScribeJson.has("id")) {
					calendarSyncToken.setSubscriptionId(subScribeJson.getString("id"));
				} else if (subScribeJson.has("Id")) {
					//This is for Android
					calendarSyncToken.setSubscriptionId(subScribeJson.getString("Id"));
				}
				
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
				
				//This is for IOS
				if (subScribeJson.has("expirationDateTime")) {
					Date oneWeekDate = sdf.parse(subScribeJson.getString("expirationDateTime"));

					calendarSyncToken.setSubExpiryDate(oneWeekDate);
				} else if (subScribeJson.has("SubscriptionExpirationDateTime")) {
					//This is for Android
					
					Date oneWeekDate = sdf.parse(subScribeJson.getString("SubscriptionExpirationDateTime"));
					calendarSyncToken.setSubExpiryDate(oneWeekDate);
				}
				
			}
			eventManager.saveCalendarSyncToken(calendarSyncToken);

			response.put("data", calendarSyncToken);

			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.DAY_OF_MONTH, Calendar.DAY_OF_MONTH - 1);
			eventManager.deleteEventsByStartTimeGreatherThanCreatedByIdAndSourceAndScheduleAs(cal.getTime(), userId,
					Event.EventSource.Outlook.toString(), Event.ScheduleEventAs.Event.toString());
			eventTimeSlotManager.deleteEventTimeSlotsByUserIdSource(userId, Event.EventSource.Outlook.toString());

			OutlookService os = new OutlookService();
			List<OutlookEvents> outlookEventList = os.getOutlookCalenderEvents(postData.get("accessToken").toString());
			if (outlookEventList != null && outlookEventList.size() > 0) {
				System.out.println("Outlook Calendar events size v2: " + outlookEventList.size());
				events = eventManager.populateOutlookEventsInCenes(outlookEventList, user);
				System.out.println("Events to Sync V2 : " + events.size());

			} else {
				outlookEventList = os.getIosOutlookEvents(postData.get("accessToken").toString());
				if (outlookEventList != null && outlookEventList.size() > 0) {
					System.out.println("Outlook Calendar events size : " + outlookEventList.size());
					events = eventManager.populateOutlookEventsInCenes(outlookEventList, user);
					System.out.println("Events to Sync : " + events.size());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}

	// Method to get Outlook events from API.
	@RequestMapping(value = "/api/outlook/refreshevents", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<Event>> refreshOutlookEvents(Long userId) {
		System.out.println("[ Refreshing Outlook Events - User Id : " + userId + "]");

		CalendarSyncToken calendarSyncToken = eventManager.findCalendarSyncTokenByUserIdAndAccountType(userId,
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
						Calendar cal = Calendar.getInstance();
						cal.set(Calendar.DAY_OF_MONTH, Calendar.DAY_OF_MONTH - 1);
						eventManager.deleteEventsByStartTimeGreatherThanCreatedByIdAndSourceAndScheduleAs(cal.getTime(),
								userId, Event.EventSource.Outlook.toString(), Event.ScheduleEventAs.Event.toString());
						eventTimeSlotManager.deleteEventTimeSlotsByUserIdSource(userId,
								Event.EventSource.Outlook.toString());

						OutlookService os = new OutlookService();
						List<OutlookEvents> outlookEventList = os.getOutlookCalenderEvents(accessToken);
						if (outlookEventList != null && outlookEventList.size() > 0) {
							System.out.println("Outlook Calendar events size : " + outlookEventList.size());
							events = eventManager.populateOutlookEventsInCenes(outlookEventList, user);
							System.out.println("Events to Sync : " + events.size());

						} else {
							List<OutlookEvents> iosOutlookEvents = os.getIosOutlookEvents(accessToken);
							if (iosOutlookEvents != null && iosOutlookEvents.size() > 0) {
								System.out.println("Outlook IOS Calendar events size : " + iosOutlookEvents.size());
								events = eventManager.populateOutlookEventsInCenes(iosOutlookEvents, user);
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
						return new ResponseEntity<List<Event>>(errorEvents, HttpStatus.INTERNAL_SERVER_ERROR);
					}
					return new ResponseEntity<List<Event>>(events, HttpStatus.OK);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	@RequestMapping(value = "/api/iosoutlook/events", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<Event>> syncIosOutlookEvents(@RequestParam("access_token") String accessToken,
			@RequestParam("user_id") Long userId, String refreshToken) {
		System.out.println("[ Syncing Outlook Events - User Id : " + userId + ", Access Token : " + accessToken + "]");
		User user = userService.findUserById(userId);
		List<Event> events = null;
		try {

			CalendarSyncToken calendarSyncToken = eventManager.findCalendarSyncTokenByUserIdAndAccountType(userId,
					CalendarSyncToken.AccountType.Outlook);
			if (calendarSyncToken == null) {
				calendarSyncToken = new CalendarSyncToken(userId, CalendarSyncToken.AccountType.Outlook, refreshToken);
				eventManager.saveCalendarSyncToken(calendarSyncToken);
			}

			eventManager.deleteEventsByCreatedByIdSource(userId, Event.EventSource.Outlook.toString());
			eventTimeSlotManager.deleteEventTimeSlotsByUserIdSource(userId, Event.EventSource.Outlook.toString());

			OutlookService os = new OutlookService();
			List<OutlookEvents> outlookEventList = os.getIosOutlookEvents(accessToken);
			if (outlookEventList != null && outlookEventList.size() > 0) {
				System.out.println("Outlook Calendar events size : " + outlookEventList.size());
				events = eventManager.populateOutlookEventsInCenes(outlookEventList, user);
				System.out.println("Events to Sync : " + events.size());
			}
			/*
			 * CenesProperty cenesProperty =
			 * eventService.findCenesPropertyByNameAndOwner("outlook_calendar",
			 * PropertyOwningEntity.User); if (cenesProperty != null) { CenesPropertyValue
			 * cenesPropertyValue = new CenesPropertyValue();
			 * cenesPropertyValue.setCenesProperty(cenesProperty);
			 * cenesPropertyValue.setDateValue(new Date());
			 * cenesPropertyValue.setEntityId(userId);
			 * cenesPropertyValue.setOwningEntity(PropertyOwningEntity.User);
			 * cenesPropertyValue.setValue("true");
			 * eventService.saveCenesPropertyValue(cenesPropertyValue); }
			 */
		} catch (Exception e) {
			e.printStackTrace();
			Event errorEvent = new Event();
			errorEvent.setErrorCode(ErrorCode.INTERNAL_ERROR.ordinal());
			errorEvent.setErrorDetail(ErrorCode.INTERNAL_ERROR.toString());
			List<Event> errorEvents = new ArrayList<>();
			errorEvents.add(errorEvent);
			return new ResponseEntity<List<Event>>(errorEvents, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<List<Event>>(events, HttpStatus.OK);
	}

	@RequestMapping(value = "/api/event/outlook/iosNotifyWebhook", method = RequestMethod.POST)
	public String iosOutlookWebHookUrl(String validationToken, HttpServletRequest request) {
		
		if (validationToken == null) {
			System.out.println("Notification From Outlook");
			try {

				System.out.println("Checking String buffer");
				BufferedReader reader = request.getReader();
				StringBuffer sb = new StringBuffer();
				String line = "";
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				reader.close();
				
				
				System.out.println("String Buffer : "+sb.toString());
				//String resp = "{\"value\":[{\"subscriptionId\":\"3384308e-ac0d-41a1-9476-c299a4f25180\",\"subscriptionExpirationDateTime\":\"2019-05-29T23:19:06.006+00:00\",\"tenantId\":\"\",\"changeType\":\"created\",\"resource\":\"Users/3f674dde22c48491/Events/AQMkADAwATM0MDAAMS1kOWNmLWFiZWUALTAwAi0wMAoARgAABKpLzewHd0aqXPJwbt36YAcABKuHDM08AUSh-3RPLq3nQAAAAgENAAAABKuHDM08AUSh-3RPLq3nQAABXyRemAAAAA==\",\"resourceData\":{\"@odata.type\":\"#Microsoft.Graph.Event\",\"@odata.id\":\"Users/3f674dde22c48491/Events/AQMkADAwATM0MDAAMS1kOWNmLWFiZWUALTAwAi0wMAoARgAABKpLzewHd0aqXPJwbt36YAcABKuHDM08AUSh-3RPLq3nQAAAAgENAAAABKuHDM08AUSh-3RPLq3nQAABXyRemAAAAA==\",\"@odata.etag\":\"W/\\\"DwAAABYAAAAEq4cMzTwBRKH/dE8uredAAAFfI0sD\\\"\",\"id\":\"AQMkADAwATM0MDAAMS1kOWNmLWFiZWUALTAwAi0wMAoARgAABKpLzewHd0aqXPJwbt36YAcABKuHDM08AUSh-3RPLq3nQAAAAgENAAAABKuHDM08AUSh-3RPLq3nQAABXyRemAAAAA==\"},\"clientState\":null}]}";
				JSONObject notificationData = new JSONObject(sb.toString());
				JSONArray value = notificationData.getJSONArray("value");
				if (value.length() > 0) {
					JSONObject payload = value.getJSONObject(0);
					
					String subScriptionId = payload.getString("subscriptionId");
					CalendarSyncToken calSyncToken = eventManager.findCalendarSyncTokenByAccountTypeAndSubscriptionId(AccountType.Outlook, subScriptionId);
					if (calSyncToken != null) {
						
						String resourceUrl = "https://graph.microsoft.com/v1.0/"+payload.getString("resource");
						OutlookService oservice = new OutlookService();
						List<OutlookEvents> olevents = oservice.getEventDetailsByResourceUrl(resourceUrl, calSyncToken);
						if (olevents != null && olevents.size() > 0) {
							
							User user = userService.findUserById(calSyncToken.getUserId());
							eventManager.populateOutlookEventsInCenes(olevents, user);
						}
					}

				}
				

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return validationToken;
	}
	
	@RequestMapping(value = "/api/event/outlook/notifyWebhook", method = RequestMethod.POST)
	public String outlookWebHookUrl(HttpServletRequest request) {

		String validationtoken = "";
		if (request.getParameterMap().containsKey("validationtoken")) {
			validationtoken = request.getParameterMap().get("validationtoken").toString();
		} if (request.getParameterMap().containsKey("validationToken")) {
			validationtoken = request.getParameterMap().get("validationToken").toString();
		} else {
			System.out.println("Notification From Outlook");
			try {

				System.out.println("Checking String buffer");
				BufferedReader reader = request.getReader();
				StringBuffer sb = new StringBuffer();
				String line = "";
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				reader.close();
				
				JSONObject notificationData = new JSONObject(sb.toString());
				JSONArray value = notificationData.getJSONArray("value");
				if (value.length() > 0) {
					JSONObject payload = value.getJSONObject(0);
					
					String subScriptionId = payload.getString("SubscriptionId");
					CalendarSyncToken calSyncToken = eventManager.findCalendarSyncTokenByAccountTypeAndSubscriptionId(AccountType.Outlook, subScriptionId);
					if (calSyncToken != null) {
						
						String resourceUrl = payload.getString("Resource");
						OutlookService oservice = new OutlookService();
						List<OutlookEvents> olevents = oservice.getEventDetailsByResourceUrl(resourceUrl, calSyncToken);
						if (olevents != null && olevents.size() > 0) {
							
							User user = userService.findUserById(calSyncToken.getUserId());
							eventManager.populateOutlookEventsInCenes(olevents, user);
						}
					}

				}
				

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return validationtoken;
	}
	
	@RequestMapping(value = "/api/event/google/notifyWebhook", method = RequestMethod.POST)
	public String googleWebHookUrl(HttpServletRequest request) {

		System.out.println("Google Push Notification");
		try {
			BufferedReader reader = request.getReader();
			StringBuffer sb = new StringBuffer();
			String line = "";
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();
			
			System.out.println("POST BODY" + sb.toString());
			//return sb.toString();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		
		try {
			
			Enumeration<String> headrs = request.getHeaderNames();
			while(headrs.hasMoreElements()) {
				System.out.println("Header Name : " + headrs.nextElement()+" , Value : "+request.getHeader(headrs.nextElement()));

			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	

	// Method to get Outlook events from API.
	@RequestMapping(value = "/api/user/gatherings", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getUserGatherings(@RequestParam("user_id") Long userId,
			@RequestParam("status") String status) {
		Map<String, Object> responseMap = new HashMap<>();
		try {
			responseMap.put("data", new ArrayList<>());

			List<Event> events = null;
			if ("pending".equals(status)) {
				events = eventService.findPendingInvitations(userId);
			} else if ("NotGoing".equals(status)) {
				events = eventService.findUserFutureGatherings(userId, "NotGoing");
			} else {
				events = eventService.findUserFutureGatherings(userId, status);
			}
			if (events == null || events.size() == 0) {
				events = new ArrayList<>();
			} else {

				for (Event iteratableEvent : events) {

					List<EventMember> members = new ArrayList<>();
					if (iteratableEvent.getEventMembers() != null && iteratableEvent.getEventMembers().size() > 0) {
						for (EventMember eventMember : iteratableEvent.getEventMembers()) {
							if (eventMember.getUserId() != null
									&& eventMember.getUserId().equals(iteratableEvent.getCreatedById())) {
								eventMember.setOwner(true);
								members.add(eventMember);
								break;
							}
						}
						for (EventMember eventMember : iteratableEvent.getEventMembers()) {
							if (eventMember.getUserId() != null
									&& !eventMember.getUserId().equals(iteratableEvent.getCreatedById())) {
								members.add(eventMember);
							}
						}
					}

					iteratableEvent.setEventMembers(members);
				}
			}
			responseMap.put("data", events);
			responseMap.put("errorCode", 0);
			responseMap.put("errorDetail", null);
			responseMap.put("success", true);
			responseMap.put("status", "ok");
			return new ResponseEntity<Map<String, Object>>(responseMap, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			responseMap.put("success", false);
			responseMap.put("status", "fail");
			responseMap.put("data", new ArrayList<>());
			responseMap.put("errorCode", ErrorCodes.InternalServerError.ordinal());
			responseMap.put("errorDetail", ErrorCodes.InternalServerError.toString());
		}

		return new ResponseEntity<Map<String, Object>>(responseMap, HttpStatus.OK);
	}

	@RequestMapping(value = "/api/user/gatherings/v2", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> getUserGatheringsV2(Long userId, String status, Long timestamp,
			int pageNumber, int offSet) {
		Map<String, Object> responseMap = new HashMap<>();
		try {
			responseMap.put("data", new ArrayList<>());

			String startDateStr = CenesUtils.yyyyMMddTHHmmss.format(timestamp);

			List<Event> events = null;
			if ("Pending".equalsIgnoreCase(status)) {
				events = eventServiceDao.findPageableGatheringsByUserIdAndStatus(userId, null, startDateStr, pageNumber,
						offSet);
			} else if ("NotGoing".equalsIgnoreCase(status)) {
				events = eventServiceDao.findPageableGatheringsByUserIdAndStatus(userId, status, startDateStr,
						pageNumber, offSet);
			} else {
				events = eventServiceDao.findPageableGatheringsByUserIdAndStatus(userId, status, startDateStr,
						pageNumber, offSet);
			}
			if (events == null || events.size() == 0) {
				events = new ArrayList<>();
			} /*
				 * else {
				 * 
				 * for (Event iteratableEvent : events) { List<EventMember> members = new
				 * ArrayList<>(); if (iteratableEvent.getEventMembers() != null &&
				 * iteratableEvent.getEventMembers().size() > 0) { for (EventMember eventMember
				 * : iteratableEvent.getEventMembers()) { if (eventMember.getUserId() != null &&
				 * eventMember.getUserId().equals(iteratableEvent.getCreatedById())) {
				 * eventMember.setOwner(true); members.add(eventMember); break; } } for
				 * (EventMember eventMember : iteratableEvent.getEventMembers()) { if
				 * (eventMember.getUserId() != null &&
				 * !eventMember.getUserId().equals(iteratableEvent.getCreatedById())) {
				 * members.add(eventMember); } } }
				 * 
				 * iteratableEvent.setEventMembers(members); } }
				 */

			int totalCounts = 0;

			try {
				totalCounts = eventServiceDao.findCountByGatheringsByUserIdAndStatus(userId, status, startDateStr);
			} catch (Exception e) {
				e.printStackTrace();
			}
			responseMap.put("totalCounts", totalCounts);
			responseMap.put("data", events);
			responseMap.put("errorCode", 0);
			responseMap.put("errorDetail", null);
			responseMap.put("success", true);
			responseMap.put("status", "ok");
			return new ResponseEntity<Map<String, Object>>(responseMap, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			responseMap.put("success", false);
			responseMap.put("status", "fail");
			responseMap.put("data", new ArrayList<>());
			responseMap.put("errorCode", ErrorCodes.InternalServerError.ordinal());
			responseMap.put("errorDetail", ErrorCodes.InternalServerError.toString());
		}

		return new ResponseEntity<Map<String, Object>>(responseMap, HttpStatus.OK);
	}

	@RequestMapping(value = "/api/user/syncdevicecalendar", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> syncDeviceCalendar(@RequestBody Map<String, Object> eventMap) {
		System.out.println("[Syncing Device Calendar : Date : " + new Date() + " STARTS]");
		System.out.println("syncdevicecalendar : " + eventMap);
		Map<String, Object> response = new HashMap<>();
		try {
			if (eventMap.containsKey("data")) {
				// Event event = ((ArrayList<Event>)eventMap.get("data")).get(0);
				User user = userService.findUserById(Long.valueOf(eventMap.get("userId").toString()));

				eventManager.deleteEventsByCreatedByIdSource(user.getUserId(), Event.EventSource.Apple.toString());
				eventTimeSlotManager.deleteEventTimeSlotsByUserIdSource(user.getUserId(),
						Event.EventSource.Apple.toString());

				List<Event> deviceEvents = new ObjectMapper().convertValue(eventMap.get("data"),
						new TypeReference<List<Event>>() {
						});
				for (Event deviceEvent : deviceEvents) {
					List<EventMember> members = new ArrayList<>();

					EventMember eventMember = new EventMember();
					eventMember.setName(user.getName());
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
				eventService.saveEventsBatch(deviceEvents);

				/*
				 * CenesProperty cenesProperty =
				 * eventService.findCenesPropertyByNameAndOwner("device_calendar",
				 * PropertyOwningEntity.User); if (cenesProperty != null) { CenesPropertyValue
				 * cenesPropertyValue = new CenesPropertyValue();
				 * cenesPropertyValue.setCenesProperty(cenesProperty);
				 * cenesPropertyValue.setDateValue(new Date());
				 * cenesPropertyValue.setEntityId(event.getCreatedById());
				 * cenesPropertyValue.setOwningEntity(PropertyOwningEntity.User);
				 * cenesPropertyValue.setValue("true");
				 * eventService.saveCenesPropertyValue(cenesPropertyValue); }
				 */
				CalendarSyncToken calendarSyncToken = eventManager.findCalendarSyncTokenByUserIdAndAccountType(
						user.getUserId(), CalendarSyncToken.AccountType.Apple);
				if (calendarSyncToken == null) {
					calendarSyncToken = new CalendarSyncToken(user.getUserId(), CalendarSyncToken.AccountType.Apple,
							"");
				}
				calendarSyncToken.setEmailId(eventMap.get("name").toString());
				eventManager.saveCalendarSyncToken(calendarSyncToken);
				response.put("data", calendarSyncToken);

			}
			response.put("success", true);
		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("errorCode", ErrorCodes.InternalServerError.ordinal());
			response.put("errorDetail", ErrorCodes.InternalServerError.toString());
		}
		System.out.println("[Syncing Device Calendar : Date : " + new Date() + " ENDS]");
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/api/events/holidays", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> findUserHolidayEvents(@RequestParam("user_id") Long userId) {
		System.out.println("[Holidays Calendar : Date : " + new Date() + ", User ID : " + userId + " STARTS]");
		Map<String, Object> response = new HashMap<>();
		try {
			/*
			 * List<String> calendarIndicators = new ArrayList<>();
			 * calendarIndicators.add("MeTime"); calendarIndicators.add("Holiday");
			 * List<Event> holidays =
			 * eventService.findUserHolidayEvents(userId,calendarIndicators);
			 */
			List<Event> allEvents = eventManager.findEventsByEventMemberId(userId);
			System.out.println("Calendar indicator Events Size : " + allEvents.size());
			if (allEvents != null && allEvents.size() > 0) {
				response.put("data", allEvents);
			} else {
				response.put("data", new ArrayList<>());
			}
			response.put("success", true);
			response.put("errorCode", 0);
			response.put("errorDetail", null);
		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("errorCode", ErrorCodes.InternalServerError.ordinal());
			response.put("errorDetail", ErrorCodes.InternalServerError.toString());
			response.put("data", new ArrayList<>());
		}
		System.out.println("[Holidays Calendar : Date : " + new Date() + ", User ID : " + userId + " ENDS]");
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/api/send/push", method = RequestMethod.GET)
	public void sendPush(@RequestParam("deviceToken") String deviceToken) {
		System.out.println("Sending Push");
		JSONArray toAndroidArray = new JSONArray();
		try {
			toAndroidArray.put(deviceToken);

			JSONObject payloadObj = new JSONObject();
			payloadObj.put("notificationTypeTitle", "Fun");
			payloadObj.put("notificationTypeId", 123);
			payloadObj.put("notificationType", NotificationType.Gathering.toString());

			JSONObject notifyObj = new JSONObject();
			notifyObj.put("title", "mandeep");
			notifyObj.put("body", " invited you to his event Fun");
			notifyObj.put("payload", payloadObj);
			PushNotificationService.sendAndroidPush(toAndroidArray, notifyObj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Push Sent");
	}

	@RequestMapping(value = "/api/event/syncData", method = RequestMethod.POST)
	public void syncUserData(Map<String, Object> syncRequestData) {

		Long userId = Long.valueOf(syncRequestData.get("userId").toString());
		System.out.println("User Id : " + userId);

		Map<String, List<Event>> deviceEvents = (Map<String, List<Event>>) syncRequestData.get("eventMap");
		System.out.println("Device Events : " + deviceEvents.toString());

		Map<String, Object> contacts = (Map<String, Object>) syncRequestData.get("contacts");
		System.out.println("Contacts : " + contacts.toString());

		eventManager.runSyncThread(userId, deviceEvents, contacts);
	}

	@RequestMapping(value = "/api/event/locations", method = RequestMethod.GET)
	public List<GatheringPreviousLocation> findEventLocationsByUserId(Long userId) {
		return eventManager.findTop15PreviousLocationsByUserId(userId);
	}
	/*
	 * public static void main(String[] args) { String testJSO =
	 * "[{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1506841200000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1506927600000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1507014000000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1507100400000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1507186800000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1507273200000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1507359600000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1507446000000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1507532400000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1507618800000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1507705200000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1507791600000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1507878000000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1507964400000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1508050800000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1508137200000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1508223600000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1508310000000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1508396400000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1508482800000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1508569200000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1508655600000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1508742000000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1508828400000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1508914800000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1509001200000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1509087600000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1509174000000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1509260400000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1509346800000},{\"predictivePercentage\":100,\"totalFriends\":1,\"attendingFriends\":1,\"date\":1509433200000}]";
	 * System.out.println(testJSO.replaceAll("\\\"","\"")); }
	 */
	// public static void main(String[] args) {

	// try{
	// String fileName = "/Users/cenes_dev/digital_image_processing.jpg";
	// String website =
	// "https://beta.cenesgroup.com/assets/uploads/events/large/78e348a0-546c-42c9-9359-0b94b4b6d241.jpg";

	// System.out.println("Downloading File From: " + website);

	/*
	 * URL url = new URL(website); InputStream inputStream = url.openStream();
	 * OutputStream outputStream = new FileOutputStream(fileName); byte[] buffer =
	 * new byte[100];
	 * 
	 * int length = 0;
	 * 
	 * while ((length = inputStream.read(buffer)) != -1) {
	 * System.out.println("Buffer Read of length: " + length);
	 * outputStream.write(buffer, 0, length); }
	 * 
	 * inputStream.close(); outputStream.close();
	 */

	// Thumbnails.of(new URL(website))
	// .size(200, 200)
	// .outputFormat("jpg").toFile(fileName);

	// } catch(Exception e) {
	// System.out.println("Exception: " + e.getMessage());
	// }
	// }

}
