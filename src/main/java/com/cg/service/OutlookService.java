package com.cg.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

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
	private String clientId = "0b228193-26f2-4837-b791-ffd7eab7441e";
	
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
			//con.setRequestProperty("Prefer", "outlook.timezone=\"Pacific Standard Time\"");
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
	
	public String httpPostQueryStr(String apiUrl, String queryStr) {
		StringBuffer response = new StringBuffer();

		try {
			URL obj = new URL(apiUrl);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
			//add reuqest header
			con.setRequestMethod("POST");
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			con.setDoOutput(true);

			OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());

			writer.write(queryStr);
			writer.flush();

			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));


			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			System.out.println(response.toString());

			writer.close();
			reader.close();     
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		return response.toString();
	}
	
	public JSONObject getAccessTokenFromRefreshToken(String refreshToken) {
		
		String apiUrl = "https://login.microsoftonline.com/common/oauth2/v2.0/token";
		String queryStr = "client_id="+clientId+"&grant_type=refresh_token&refresh_token="+refreshToken;
		String response = httpPostQueryStr(apiUrl, queryStr);
		if (response != null) {
			try {
				JSONObject jsonObject = new JSONObject(response);
				return jsonObject;
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		return null;
	}
	
	
	/*public static void main(String[] args) {
		
		String apiUrl = "https://login.windows.net/common/oauth2/token";
		String queryStr = "grant_type=refresh_token&"
				+ "refresh_token=MCTZ!cpBmnvHsZcMTIzELS6DB9qw5RPZWt23M2wpQb3D6kuGGpZ11UpzEzwnKZmo9zZrK4tNyri9kMGsyB5SVfZjLH2uEAmUE*oz1b8g*7fbKI1TOQbh8iExdS1QC50XsJf3VCuDJp6nc9WP4rBdtAKRr8hEQMqCPSqVxE33MsUWKYEHeq8Q*zUDtt4KtTCMPAvcXpKxVKyt9n7PhxaUa2zRoyE90HSDM22P3tAmNKPi02cbw7FGFG!xDzhIC4Fz6R9LQOpr78Mk9LOvFA2b1HYNlCarVH6rLQ!4ot4NZy8ZzUTyxYzN1hODsDUFrQZVOGp76UxYXWbwwMQoFr9!z*ILICTLwLY7XWIAzi1iqny1q&"
				+ "client_id=0b228193-26f2-4837-b791-ffd7eab7441e&"
				+ "client_secret=jnnjJDIN8291)ymgMOC9|}$";
		try {
			URL obj = new URL(apiUrl);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

			//add reuqest header
			con.setRequestMethod("POST");
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(queryStr);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + apiUrl);
			System.out.println("Post parameters : " + queryStr);
			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			//print result
			System.out.println(response.toString());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}*/
	
	/*public static void main(String[] args) {
		OutlookService os = new OutlookService();
		String refreshToken = "MCWzjiOS1Xanp6JdlUnpSWB6fSuOkGj5U29pUNSRfBwwQBHyjEaAnuYWXVGB4QJB!PA1aT632COanl4W2feK2BlSEav72G5*vyMwNiPElXpw7pY9Az7eC4sWmM92ADH0BaHkcurxlHcbeVShmiwRA70Pre75eODrdfYnQkVs6O64!QeUEeuL0hT9KQenUtPcck8gvto0c8IqOksoH1w!9O5yKWMIiCOcRbOnJWTT51VQIxH*YkJPv1!nlOFelZfSz8tKqU0ZVXMZTTNXiY2VCpUtWJJSxPAzTX86rQcNMFtUytUl85hVIMPbPNBlqIpHyaxatKuAwqYYmF7kKfuBjg1uxn4qU!ym3hMlnx5BBs2ic";
		os.getAccessTokenFromRefreshToken(refreshToken);
	}*/
	
	/*public static void main(String[] args) {
		
		String accessToken = "EwAgA61DBAAUcSSzoTJJsy+XrnQXgAKO5cj4yc8AAV2zfWEFS69wDDqjHCypIDm3fX2I/C8KuKT8NFInGbq3qH98TTTqVzmdUTyKhOI9xKFAUSnFYNF+JSE6FGgdj/9L875qaSIOydLKTQmWTm/Ml309Am3QZSVEbt7WevU80KbeW3ppPBFtt5TJ4AQKnb1cEruPvM9mEHYew2hqVXRFXv9/1GejRLSEWVUQ1N2XUtiQrwfI3vJbNWly7E1GXuMC/v71G4RIC3CFN5bp/3oLzFtB2QpReXa8kqNU+iN58QYe76HTyvAwbkP3FrKIjFTqQl4Ad4dH3f9+4CT2MxrmjwOjmlYn1ffhXL5ldFUFtqt7+D33A/Q0h5f6wKIr36oDZgAACFuV/RWfmNOM8AGzTeS8w/+MYvg5S21R/vNZhfzLPvBWVLKSUZkxu9txxLChh1KF4FeJEosgu2UutGLSUAbI1kLurOu3AAk3F2Cj3FzwMD/Hdw/814ony1uqeWYYDS5mur3q7WSR2lIQlzdQdWUfLacGg4dzat7YqhZ/ZBvQl5IPaNa3IRojb9Qpm+/0cl/vBoa2ntJ/PCWgnifcxk6XR4nm7hSnqfv4D3t1ccJ1/Lw7CwrC4ujJwCBYgxlPY+v2YnKQLGD5O4TD4F3Umw136FSXrypwKSacFsxUmK5kDe20nqRnJRnuTj6Ig6v3vEYR5qR/P+gQeMHQx03jIIfSZa9usJJb1o8SkS+2rCIsTNXwwDctK93Gw2WK/sXlRTqrdYE7lV9yXdmJZPvIyosceGo/rUeXhuk72Etx9phxvCIAkdgNswj9tWu6yPwcH0ka6QiTrf8eChc4+NfgwBSyeck819kZhIUpO5g5kBM4IM0/VASIqFYs4YL5E2aZXayB2FVLth80FOEa2h+mnlQszdortTHsyJDFThqpOsIjwG6XmmOf2Yt4DrgcMdN9jokY+xpXdnzktWDcJsaspdyEeABPqrOs2Pp2lXvHM/DogiVYvfV1g4MoFoVsD/Lxwz+f8LveDThR0dMf14yTSl8ouTec9l48cywWSvYOIgI=";
		
		OutlookService os = new OutlookService();
		os.getOutlookCalenderEvents(accessToken);
	}*/
}
