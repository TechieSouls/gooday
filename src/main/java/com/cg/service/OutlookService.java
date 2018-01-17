package com.cg.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.primefaces.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.cg.events.bo.OutlookEventAttendees;
import com.cg.events.bo.OutlookEventItem;
import com.cg.events.bo.OutlookEvents;

public class OutlookService {
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
	public List<OutlookEvents> getOutlookCalenderEvents (String accessToken) {
		List<OutlookEvents> outlookCalenderEvents = new ArrayList<>();
		Date date = new Date();
		String startDateTime = sdf.format(date);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
	    calendar.add(Calendar.MONTH, 3);
	    date = calendar.getTime();
	    String endDateTime = sdf.format(date);
	    String outlookApi = "https://outlook.office.com/api/v2.0/me/calendarview?startDateTime="+ URLEncoder.encode(startDateTime) +"&endDateTime=" + URLEncoder.encode(endDateTime)+"&$top=1000";
	    //OutlookEvents outlookEvents = //doOutlookEventsRestRequest(outlookApi, HttpMethod.GET, null, accessToken);
	    JSONObject outlookJSON = doOutlookCalendarRestRequest(outlookApi, "GET", accessToken);
	    OutlookEvents outlookEvents = parseOutlookResponse(outlookJSON);
	    outlookCalenderEvents.add(outlookEvents);
		return outlookCalenderEvents;
	}
	
	public List<OutlookEvents> getIosOutlookEvents(String accessToken) {
		List<OutlookEvents> outlookCalenderEvents = new ArrayList<>();
		String outlookApi = "https://graph.microsoft.com/v1.0/me/events";
		JSONObject outlookJSON = doOutlookCalendarRestRequest(outlookApi, "GET", accessToken);
	    OutlookEvents outlookEvents = parseIosOutlookEvents(outlookJSON);
	    outlookCalenderEvents.add(outlookEvents);
		return outlookCalenderEvents;
	}
	
	
	public <T> OutlookEvents doOutlookEventsRestRequest(String url, HttpMethod method, T body, String accessToken){
		System.out.println("["+new Date()+" Making Request,url : "+url);
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Bearer " + accessToken);
		//headers.setAccept(Arrays.asList(new MediaType[]{MediaType.APPLICATION_JSON}));
		HttpEntity<T> entity = new HttpEntity<T>(body, headers);
		OutlookEvents orderEntity = null;
		try{
			orderEntity = restTemplate.exchange(url,method, entity, OutlookEvents.class).getBody();
		}catch (HttpClientErrorException e) {
		      System.out.println("OutlookService ErrorCode: "+e.getStatusCode());
		      System.out.println("OutlookService ErrorMessage: "+e.getResponseBodyAsString());
		      //throw e;
		      orderEntity = new OutlookEvents();
		      orderEntity.setErrorCode(e.getStatusCode().ordinal());
		      orderEntity.setErrorDetail(e.getMessage());
		      return orderEntity;
		}catch (HttpServerErrorException e) {
		      System.out.println("OutlookService ErrorCode: "+e.getStatusCode());
		      System.out.println("OutlookService ErrorMessage: "+e.getResponseBodyAsString());
		      //throw e;
		      orderEntity = new OutlookEvents();
		      orderEntity.setErrorCode(e.getStatusCode().ordinal());
		      orderEntity.setErrorDetail(e.getMessage());
		      return orderEntity;
		}
		System.out.println("["+new Date()+" Request Complete");
		return orderEntity;
	}
	
	public JSONObject doOutlookCalendarRestRequest(String url, String method,String accessToken){
		System.out.println("["+new Date()+" Making Google Calendar List Request,url : "+url);
		JSONObject jObject = null;
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod(method);
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Authorization", "Bearer " + accessToken);
			con.setRequestProperty("Prefer", "outlook.timezone=\"Pacific Standard Time\"");
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
	
	
	public OutlookEvents parseOutlookResponse(JSONObject responseObj) {
		OutlookEvents events = new OutlookEvents();
		try {
			if (responseObj.has("value") && responseObj.getJSONArray("value").length() > 0) {
				List<OutlookEventItem> items = new ArrayList<>();
				for (int i=0; i < responseObj.getJSONArray("value").length(); i++) {
					
					JSONObject outlookJson = responseObj.getJSONArray("value").getJSONObject(i);
					
					
					OutlookEventItem item = new OutlookEventItem();
					item.setSubject(outlookJson.getString("Subject"));
					
					if (outlookJson.has("Start")) {
						Map<String,String>  startDateMap = new HashMap<>();
						startDateMap.put("DateTime", outlookJson.getJSONObject("Start").getString("DateTime"));
						item.setStart(startDateMap);
					}
					if (outlookJson.has("End")) {
						Map<String,String>  endDateMap = new HashMap<>();
						endDateMap.put("DateTime", outlookJson.getJSONObject("End").getString("DateTime"));
						item.setEnd(endDateMap);
					}
					item.setId(outlookJson.getString("Id"));
					if (outlookJson.has("Location")) {
						Map<String,String>  addressMap = new HashMap<>();
						JSONObject location = outlookJson.getJSONObject("Location");
						addressMap.put("DisplayName",location.getString("DisplayName"));
						item.setLocation(addressMap);
					}
					
					if (outlookJson.has("Attendees") && outlookJson.getJSONArray("Attendees").length() > 0) {
						//item.setAttendees(attendees);
					} else {
						item.setAttendees(new ArrayList<OutlookEventAttendees>());
					}
					
					items.add(item);
				}
				events.setValue(items);
			} else {
				events.setErrorCode(101);
				events.setErrorDetail("Token Expired");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return events;
	}
	
	public OutlookEvents parseIosOutlookEvents(JSONObject responseObj) {

		OutlookEvents events = new OutlookEvents();
		try {
			if (responseObj.has("value") && responseObj.getJSONArray("value").length() > 0) {
				List<OutlookEventItem> items = new ArrayList<>();
				for (int i=0; i < responseObj.getJSONArray("value").length(); i++) {
					
					JSONObject outlookJson = responseObj.getJSONArray("value").getJSONObject(i);
					
					
					OutlookEventItem item = new OutlookEventItem();
					item.setSubject(outlookJson.getString("subject"));
					
					if (outlookJson.has("start")) {
						Map<String,String>  startDateMap = new HashMap<>();
						startDateMap.put("DateTime", outlookJson.getJSONObject("start").getString("dateTime"));
						item.setStart(startDateMap);
					}
					if (outlookJson.has("End")) {
						Map<String,String>  endDateMap = new HashMap<>();
						endDateMap.put("DateTime", outlookJson.getJSONObject("start").getString("dateTime"));
						item.setEnd(endDateMap);
					}
					item.setId(outlookJson.getString("id"));
					if (outlookJson.has("location")) {
						Map<String,String>  addressMap = new HashMap<>();
						JSONObject location = outlookJson.getJSONObject("location");
						addressMap.put("DisplayName",location.getString("displayName"));
						item.setLocation(addressMap);
					}
					
					if (outlookJson.has("attendees") && outlookJson.getJSONArray("attendees").length() > 0) {
						//item.setAttendees(attendees);
					} else {
						item.setAttendees(new ArrayList<OutlookEventAttendees>());
					}
					
					items.add(item);
				}
				events.setValue(items);
			} else {
				events.setErrorCode(101);
				events.setErrorDetail("Token Expired");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return events;
	
	}
	
}
