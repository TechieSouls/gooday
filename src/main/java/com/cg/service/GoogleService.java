package com.cg.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.primefaces.json.JSONArray;
import org.primefaces.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.cg.bo.GoogleCountries;
import com.cg.bo.GoogleCountryItem;
import com.cg.events.bo.GoogleEventAttendees;
import com.cg.events.bo.GoogleEventItem;
import com.cg.events.bo.GoogleEvents;
import com.cg.utils.CenesUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GoogleService {
	private String calendar_list_api = "https://www.googleapis.com/calendar/v3/users/me/calendarList";
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
	
	public List<GoogleEvents> getCalenderEventsOld(String accessToken) {
		List<GoogleEvents> googleCalendarEvents = new ArrayList<>();
		List<String> calendars = googleCalendarList(accessToken);
		if (calendars != null && calendars.size() > 0) {
			for (String calendarId : calendars) {
				
				Calendar minTimeCal = Calendar.getInstance();
				minTimeCal.set(Calendar.HOUR_OF_DAY, 0);
				minTimeCal.set(Calendar.MINUTE, 0);
				minTimeCal.set(Calendar.SECOND, 0);
				
				Calendar maxTimeCal = Calendar.getInstance();
				maxTimeCal.set(Calendar.HOUR_OF_DAY, 0);
				maxTimeCal.set(Calendar.MINUTE, 0);
				maxTimeCal.set(Calendar.SECOND, 0);
				maxTimeCal.add(Calendar.MONTH, 3);
				String events_list_api_str = "https://www.googleapis.com/calendar/v3/calendars/"+URLEncoder.encode(calendarId)+"/events";
				String calenderAPI = events_list_api_str+"?access_token="+accessToken+"&future_events=true&singleEvents=true&timeMax="+URLEncoder.encode(sdf.format(minTimeCal.getTime()))+"&timeMax="+URLEncoder.encode(sdf.format(maxTimeCal.getTime()));
				try {
					JSONObject calResponse = doGoogleCalendarRestRequest(calenderAPI,"GET");
					if (calResponse != null && calResponse.has("items")) {
						List<GoogleEventItem> items = new ArrayList<>();
						JSONArray itemsArray = calResponse.getJSONArray("items");
						for (int i=0; i < itemsArray.length(); i++) {
							JSONObject calItem = itemsArray.getJSONObject(i);
							GoogleEventItem item = new GoogleEventItem();
							item.setId(calItem.getString("id"));
							if (calItem.has("summary")) {
								item.setSummary(calItem.getString("summary"));
							}
							if (calItem.has("description")) {
								item.setDescription(calItem.getString("description"));
							}
							if (calItem.has("location")) {
								item.setLocation(calItem.getString("location"));
							}
							item.setStatus(calItem.getString("status"));
							if (calItem.has("start")) {
								item.setStart(new ObjectMapper().readValue(calItem.getJSONObject("start").toString(), HashMap.class));
							}
							if (calItem.has("end")) {
								item.setEnd(new ObjectMapper().readValue(calItem.getJSONObject("end").toString(), HashMap.class));
							}
							
							if (calItem.has("attendees")) {
								JSONArray attendeesArray = calItem.getJSONArray("attendees");
								List<GoogleEventAttendees> attendees = new ArrayList<>();
								for (int j=0; j<attendeesArray.length(); j++) {
									JSONObject attendeeObj = attendeesArray.getJSONObject(j);
									GoogleEventAttendees attendee = new GoogleEventAttendees();
									if (attendeeObj.has("displayName")) {
										attendee.setDisplayName(attendeeObj.getString("displayName"));
									}
									
									if (attendeeObj.has("self")) {
										attendee.setSelf(attendeeObj.getBoolean("self"));
									} else {
										attendee.setSelf(false);
									}
									
									if (attendeeObj.has("organizer")) {
										attendee.setOrganizer(attendeeObj.getBoolean("organizer"));
									} else {
										attendee.setOrganizer(false);
									}
									
									if (attendeeObj.has("responseStatus")) {
										attendee.setResponseStatus(attendeeObj.getString("responseStatus"));
									}
									
									if (attendeeObj.has("email")) {
										attendee.setEmail(attendeeObj.getString("email"));
									}
									
									attendees.add(attendee);
								}
								item.setAttendees(attendees);
							}
							items.add(item);
						}
						//GoogleEvents events = doGoogleEventsRestRequest(calenderAPI,HttpMethod.GET,null);
						GoogleEvents events = new GoogleEvents();
						events.setItems(items);
						events.setTimeZone(calResponse.getString("timeZone"));
						events.setErrorCode(0);
						events.setErrorDetail(null);
						if (events != null && events.getErrorDetail() == null) {
							googleCalendarEvents.add(events);
						}
					} else {
						GoogleEvents events = new GoogleEvents();
						events.setItems(new ArrayList<GoogleEventItem>());
						events.setTimeZone(null);
						events.setErrorCode(0);
						events.setErrorDetail(null);
					}
					
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			return googleCalendarEvents;
		}
		return null;
	}
	
	
	public List<GoogleEvents> getCalenderEvents(String accessToken) {
		List<GoogleEvents> googleCalendarEvents = new ArrayList<>();
				
				Calendar minTimeCal = Calendar.getInstance();
				minTimeCal.set(Calendar.HOUR_OF_DAY, 0);
				minTimeCal.set(Calendar.MINUTE, 0);
				minTimeCal.set(Calendar.SECOND, 0);
				
				Calendar maxTimeCal = Calendar.getInstance();
				maxTimeCal.set(Calendar.HOUR_OF_DAY, 0);
				maxTimeCal.set(Calendar.MINUTE, 0);
				maxTimeCal.set(Calendar.SECOND, 0);
				maxTimeCal.add(Calendar.MONTH, 3);
				
				String events_list_api_str = "https://www.googleapis.com/calendar/v3/calendars/primary/events";
				String recurringEventsAPI = events_list_api_str+"?access_token="+accessToken+"&future_events=true&singleEvents=true&timeMin="+URLEncoder.encode(sdf.format(minTimeCal.getTime()))+"&timeMax="+URLEncoder.encode(sdf.format(maxTimeCal.getTime()));
				JSONObject calResponse = doGoogleCalendarRestRequest(recurringEventsAPI,"GET");
				googleCalendarEvents.addAll(parseGoogleEventsResponse(calResponse,true));
				
				String normalEventsAPI = events_list_api_str+"?access_token="+accessToken+"&future_events=true&timeMin="+URLEncoder.encode(sdf.format(minTimeCal.getTime()))+"&timeMax="+URLEncoder.encode(sdf.format(maxTimeCal.getTime()));
				calResponse = doGoogleCalendarRestRequest(normalEventsAPI,"GET");
				googleCalendarEvents.addAll(parseGoogleEventsResponse(calResponse,false));
				
			return googleCalendarEvents;
	}
	
	public List<GoogleEvents> parseGoogleEventsResponse(JSONObject calResponse,Boolean isRecurringRequest) {
		List<GoogleEvents> googleCalendarEvents = new ArrayList<>();
		try {
			
			if (calResponse != null && calResponse.has("items")) {
				List<GoogleEventItem> items = new ArrayList<>();
				JSONArray itemsArray = calResponse.getJSONArray("items");
				for (int i=0; i < itemsArray.length(); i++) {
					
					JSONObject calItem = itemsArray.getJSONObject(i);
					if (!isRecurringRequest && calItem.has("recurrence")) {
						continue;
					}
					
					GoogleEventItem item = new GoogleEventItem();
					item.setId(calItem.getString("id"));
					if (calItem.has("summary")) {
						item.setSummary(calItem.getString("summary"));
					}
					if (calItem.has("description")) {
						item.setDescription(calItem.getString("description"));
					}
					if (calItem.has("location")) {
						item.setLocation(calItem.getString("location"));
					}
					item.setStatus(calItem.getString("status"));
					if (calItem.has("start")) {
						item.setStart(new ObjectMapper().readValue(calItem.getJSONObject("start").toString(), HashMap.class));
					}
					if (calItem.has("end")) {
						item.setEnd(new ObjectMapper().readValue(calItem.getJSONObject("end").toString(), HashMap.class));
					}
					if (calItem.has("recurringEventId")) {
						item.setRecurringEventId(calItem.getString("recurringEventId"));
					}
					
					if (calItem.has("attendees")) {
						JSONArray attendeesArray = calItem.getJSONArray("attendees");
						List<GoogleEventAttendees> attendees = new ArrayList<>();
						for (int j=0; j<attendeesArray.length(); j++) {
							JSONObject attendeeObj = attendeesArray.getJSONObject(j);
							GoogleEventAttendees attendee = new GoogleEventAttendees();
							if (attendeeObj.has("displayName")) {
								attendee.setDisplayName(attendeeObj.getString("displayName"));
							}
							
							if (attendeeObj.has("self")) {
								attendee.setSelf(attendeeObj.getBoolean("self"));
							} else {
								attendee.setSelf(false);
							}
							
							if (attendeeObj.has("organizer")) {
								attendee.setOrganizer(attendeeObj.getBoolean("organizer"));
							} else {
								attendee.setOrganizer(false);
							}
							
							if (attendeeObj.has("responseStatus")) {
								attendee.setResponseStatus(attendeeObj.getString("responseStatus"));
							}
							
							if (attendeeObj.has("email")) {
								attendee.setEmail(attendeeObj.getString("email"));
							}
							
							attendees.add(attendee);
						}
						item.setAttendees(attendees);
					}
					items.add(item);
				}
				//GoogleEvents events = doGoogleEventsRestRequest(calenderAPI,HttpMethod.GET,null);
				GoogleEvents events = new GoogleEvents();
				events.setItems(items);
				events.setTimeZone(calResponse.getString("timeZone"));
				events.setErrorCode(0);
				events.setErrorDetail(null);
				if (events != null && events.getErrorDetail() == null) {
					googleCalendarEvents.add(events);
				}
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return googleCalendarEvents;
	}
	
	
	public List<String> googleCalendarList(String accessToken) {
		List<String> calendarListIds = new ArrayList<>();
		String calenderListAPI = calendar_list_api+"?access_token="+accessToken;
		JSONObject calendarListObj = doGoogleCalendarRestRequest(calenderListAPI, "GET");
		if (calendarListObj != null && !calendarListObj.has("ErrorCode")) {
			try {
				JSONArray calendarListArray = (JSONArray)calendarListObj.get("items");
				for (int i = 0; i < calendarListArray.length(); i++) {
					JSONObject calObj = (JSONObject)calendarListArray.get(i);
					calendarListIds.add(calObj.getString("id"));
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return calendarListIds;
	}
	
	//Method to get Holiday Calendar events
	public GoogleEvents getCountryHolidayEvents(String calendarId) {
		String events_list_api_str = "https://www.googleapis.com/calendar/v3/calendars/"+URLEncoder.encode(calendarId)+"/events?key="+CenesUtils.googleAPIKey;
		try {
			JSONObject holidayCalendarObj = doGoogleCalendarRestRequest(events_list_api_str,"GET");
			if (holidayCalendarObj == null) {
				return null;
			}
			
			JSONArray items = holidayCalendarObj.getJSONArray("items");
			if (items.length() == 0) {
				return null;
			}
			GoogleEvents events = new GoogleEvents();
			List<GoogleEventItem> eventItems = new ArrayList<>();
			for (int i=0; i<items.length(); i++) {
				JSONObject itemObj = items.getJSONObject(i);
				GoogleEventItem googleEventItem = new GoogleEventItem();
				googleEventItem.setSummary(itemObj.getString("summary"));
				googleEventItem.setId(itemObj.getString("id"));
				googleEventItem.setStart(new ObjectMapper().readValue(itemObj.getJSONObject("start").toString(), HashMap.class));
				googleEventItem.setEnd(new ObjectMapper().readValue(itemObj.getJSONObject("end").toString(), HashMap.class));
				eventItems.add(googleEventItem);
			}
			events.setItems(eventItems);
			return events;
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public GoogleCountries getCountries() {
		String placeApi = "http://cenes.test2.redblink.net/assets/countries-info.json";
		GoogleCountries gc = null;//doGoogleCountriesRestRequest(placeApi,HttpMethod.GET,null);
		return gc;
	}
	
	public <T> GoogleCountries doGoogleCountriesRestRequest(String url, HttpMethod method, T body){
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			System.out.println(con.getContent().toString());
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
				org.primefaces.json.JSONObject jObject = new org.primefaces.json.JSONObject(
						response.toString());
				JSONArray jsonArray =  (JSONArray) jObject.get("Response");
				GoogleCountries googleCountries = new GoogleCountries();
				List<GoogleCountryItem> items = new ArrayList<>();
				for (int i=0; i < jsonArray.length(); i++) {
					JSONObject countryObj = (JSONObject)jsonArray.get(i);
					GoogleCountryItem gci = new GoogleCountryItem();
					gci.setName(countryObj.getString("Name"));
					gci.setFlag(countryObj.getString("Flag"));
					gci.setLatitude(countryObj.getString("Latitude"));
					gci.setLongitude(countryObj.getString("Longitude"));
					items.add(gci);
				}
				googleCountries.setResponse(items);
				googleCountries.setIsSuccess(true);
				return googleCountries;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public <T> GoogleEvents doGoogleEventsRestRequest(String url, HttpMethod method, T body){
		System.out.println("["+new Date()+" Making Request,url : "+url);
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(new MediaType[]{MediaType.APPLICATION_JSON}));
		HttpEntity<T> entity = new HttpEntity<T>(body, headers);
		GoogleEvents orderEntity = null;
		try{
			orderEntity = restTemplate.exchange(url,method, entity, GoogleEvents.class).getBody();
		}catch (HttpClientErrorException e) {
		      System.out.println("GoogleService ErrorCode: "+e.getStatusCode());
		      System.out.println("GoogleService ErrorMessage: "+e.getResponseBodyAsString());
		      //throw e;
		      orderEntity = new GoogleEvents();
		      orderEntity.setErrorCode(e.getStatusCode().ordinal());
		      orderEntity.setErrorDetail(e.getMessage());
		      return orderEntity;
		}catch (HttpServerErrorException e) {
		      System.out.println("GoogleService ErrorCode: "+e.getStatusCode());
		      System.out.println("GoogleService ErrorMessage: "+e.getResponseBodyAsString());
		      //throw e;
		      orderEntity = new GoogleEvents();
		      orderEntity.setErrorCode(e.getStatusCode().ordinal());
		      orderEntity.setErrorDetail(e.getMessage());
		      return orderEntity;
		}
		System.out.println("["+new Date()+" Request Complete");
		return orderEntity;
	}
	
	public JSONObject doGoogleCalendarRestRequest(String url, String method){
		System.out.println("["+new Date()+" Making Google Calendar List Request,url : "+url);
		JSONObject jObject = null;
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod(method);
			int responseCode = con.getResponseCode();
			System.out.println(con.getContent().toString());
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
				jObject = new JSONObject(response.toString());
				
				return jObject;
			}
		} catch(Exception e) {
			e.printStackTrace();
			jObject = new JSONObject();
			try {
				jObject.put("ErrorCode",102);
				jObject.put("ErrorDetail",e.getMessage());
			} catch(Exception e1){
				e1.printStackTrace();
			}
		}
		System.out.println("["+new Date()+" Google Calendar List Request Complete");
		return jObject;
	}
}
