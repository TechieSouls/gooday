package com.cg.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import org.primefaces.json.JSONArray;
import org.primefaces.json.JSONException;
import org.primefaces.json.JSONObject;

import com.cg.bo.CalendarSyncToken;
import com.cg.bo.GoogleCountries;
import com.cg.events.bo.GoogleEventAttendees;
import com.cg.events.bo.GoogleEventItem;
import com.cg.events.bo.GoogleEvents;
import com.cg.utils.CenesUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GoogleService {
	private String calendar_list_api = "https://www.googleapis.com/calendar/v3/users/me/calendarList";
	private String clientId = "212716305349-dqqjgf3njkqt9s3ucied3bit42po3m39.apps.googleusercontent.com";
	private String clientSecret = "aaHR0dPqd57brx15We1sr1LE";
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
	
	//anot Used
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
				//String events_list_api_str = "https://www.googleapis.com/calendar/v3/calendars/"+URLEncoder.encode(calendarId)+"/events";
				//String calenderAPI = events_list_api_str+"?access_token="+accessToken+"&future_events=true&singleEvents=true&timeMax="+URLEncoder.encode(sdf.format(minTimeCal.getTime()))+"&timeMax="+URLEncoder.encode(sdf.format(maxTimeCal.getTime()));

				String events_list_api_str = "https://www.googleapis.com/calendar/v3/calendars/"+URLEncoder.encode(calendarId)+"/events";
				String calenderAPI = events_list_api_str+"?future_events=true&singleEvents=true&timeMax="+URLEncoder.encode(sdf.format(minTimeCal.getTime()))+"&timeMax="+URLEncoder.encode(sdf.format(maxTimeCal.getTime()));

				try {
					//JSONObject calResponse = doGoogleCalendarRestRequest(calenderAPI,"GET");
					HttpService httpService = new HttpService();
					JSONObject calResponse = httpService.getRequestWithAuthorization(calenderAPI, "GET", accessToken);
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
	
	
	public List<GoogleEvents> getCalenderEvents(boolean isNextSyncRequest, String accessToken) {
		List<GoogleEvents> googleCalendarEvents = new ArrayList<>();
		/* List<String> calendars = googleCalendarList(accessToken);
		if (calendars != null && calendars.size() > 0) {
			for (String calendarId : calendars) {

				if (calendarId.indexOf("#holiday@") != -1) {
					System.out.println("Skipping :: "+calendarId);
					continue;
				}
		*/		
				Calendar minTimeCal = Calendar.getInstance();
				minTimeCal.add(Calendar.DAY_OF_MONTH, - 1);
				minTimeCal.set(Calendar.HOUR_OF_DAY, 0);
				minTimeCal.set(Calendar.MINUTE, 0);
				minTimeCal.set(Calendar.SECOND, 0);
				
				Calendar maxTimeCal = Calendar.getInstance();
				maxTimeCal.set(Calendar.HOUR_OF_DAY, 0);
				maxTimeCal.set(Calendar.MINUTE, 0);
				maxTimeCal.set(Calendar.SECOND, 0);
				maxTimeCal.add(Calendar.MONTH, 3);
								
				String events_list_api_str = "https://www.googleapis.com/calendar/v3/calendars/primary/events";
				
				String tokenParam = "";//"access_token="+accessToken;
				if (isNextSyncRequest) {
					tokenParam = "&syncToken="+accessToken;
				}
				
				HttpService httpService = null;
				
				//String recurringEventsAPI = events_list_api_str+"?key="+CenesUtils.googleAPIKey+"&future_events=true"+tokenParam+"&singleEvents=true&timeMin="+URLEncoder.encode(sdf.format(minTimeCal.getTime()))+"&timeMax="+URLEncoder.encode(sdf.format(maxTimeCal.getTime()));
				//String recurringEventsAPI = events_list_api_str+"?key="+CenesUtils.googleAPIKey+"&"+tokenParam+"&singleEvents=true";
				String recurringEventsAPI = events_list_api_str+"?maxResults=100&singleEvents=true&timeMin="+URLEncoder.encode(sdf.format(minTimeCal.getTime()))+"&alt=json";
				httpService = new HttpService();
				JSONObject calResponse = httpService.getRequestWithAuthorization(recurringEventsAPI, "GET", accessToken);//doGoogleCalendarRestRequest(recurringEventsAPI,"GET");
				googleCalendarEvents.addAll(parseGoogleEventsResponse(calResponse,true));
				
				httpService = new HttpService();
				//String normalEventsAPI = events_list_api_str+"?key="+CenesUtils.googleAPIKey+"&future_events=true"+tokenParam+"&timeMin="+URLEncoder.encode(sdf.format(minTimeCal.getTime()))+"&timeMax="+URLEncoder.encode(sdf.format(maxTimeCal.getTime()));
				//String normalEventsAPI = events_list_api_str+"?key="+CenesUtils.googleAPIKey+"&"+tokenParam;
				String normalEventsAPI = events_list_api_str+"?maxResults=100&singleEvents=false&timeMin="+URLEncoder.encode(sdf.format(minTimeCal.getTime()))+"&alt=json";

				calResponse = httpService.getRequestWithAuthorization(normalEventsAPI, "GET", accessToken);//doGoogleCalendarRestRequest(normalEventsAPI,"GET");
				googleCalendarEvents.addAll(parseGoogleEventsResponse(calResponse,false));
		/*	}
		}	*/
		return googleCalendarEvents;
	}
	
	
	public List<GoogleEvents> getGoogleEventsOnNotification(String resourceUrl, String accessToken, Date minTime) {
		
		List<GoogleEvents> googleCalendarEvents = new ArrayList<>();
		HttpService httpService = new HttpService();

		/*String resourceUrlTemp = "https://www.googleapis.com/calendar/v3/calendars/primary/events?maxResults=100&singleEvents=true&timeMin="+URLEncoder.encode(sdf.format(minTime))+"&alt=json";
		JSONObject calResponse = httpService.getRequestWithAuthorization(resourceUrlTemp, "GET", accessToken);
		googleCalendarEvents.addAll(parseGoogleEventsResponse(calResponse,true));*/

		
		String recurringEventsAPI = "https://www.googleapis.com/calendar/v3/calendars/primary/events?maxResults=50&singleEvents=true&timeMin="+URLEncoder.encode(sdf.format(minTime))+"&alt=json";
		System.out.println("Url recurringEventsAPI : "+recurringEventsAPI);
		JSONObject calResponse = httpService.getRequestWithAuthorization(recurringEventsAPI, "GET", accessToken);//doGoogleCalendarRestRequest(recurringEventsAPI,"GET");
		googleCalendarEvents.addAll(parseGoogleEventsResponse(calResponse,true));
		
		httpService = new HttpService();
		//String normalEventsAPI = events_list_api_str+"?key="+CenesUtils.googleAPIKey+"&future_events=true"+tokenParam+"&timeMin="+URLEncoder.encode(sdf.format(minTimeCal.getTime()))+"&timeMax="+URLEncoder.encode(sdf.format(maxTimeCal.getTime()));
		String normalEventsAPI = "https://www.googleapis.com/calendar/v3/calendars/primary/events?maxResults=50&timeMin="+URLEncoder.encode(sdf.format(minTime))+"&alt=json";
		System.out.println("Url normalEventsAPI : "+normalEventsAPI);
		calResponse = httpService.getRequestWithAuthorization(normalEventsAPI, "GET", accessToken);//doGoogleCalendarRestRequest(normalEventsAPI,"GET");
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
					if (!calItem.has("summary")) {
						continue;
					}
					
					//System.out.println(calItem.toString());
					
					GoogleEventItem item = new GoogleEventItem();
					item.setId(calItem.getString("id"));
					
					item.setSummary(calItem.getString("summary"));
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
					if (calItem.has("creator")) {
						item.setCreatorEmail(calItem.getJSONObject("creator").getString("email"));
						if (calItem.getJSONObject("creator").has("self")) {
							item.setSelf(calItem.getJSONObject("creator").getBoolean("self"));
						}
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
				if (calResponse.has("nextSyncToken")) {
					events.setNextSyncToken(calResponse.getString("nextSyncToken"));

				}
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
		//String calenderListAPI = calendar_list_api+"?access_token="+accessToken;
		//JSONObject calendarListObj = doGoogleCalendarRestRequest(calenderListAPI, "GET");
		
		HttpService httpService = new HttpService();
		JSONObject calendarListObj = httpService.getRequestWithAuthorization(calendar_list_api, "GET",accessToken );
		if (calendarListObj != null && !calendarListObj.has("ErrorCode")) {
			try {
				JSONArray calendarListArray = (JSONArray)calendarListObj.get("items");
				for (int i = 0; i < calendarListArray.length(); i++) {
					JSONObject calObj = (JSONObject)calendarListArray.get(i);
					System.out.println(calObj.toString());
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
			HttpService httpService = new HttpService();
			JSONObject holidayCalendarObj = httpService.doGoogleCalendarRestRequest(events_list_api_str,"GET");
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
				//System.out.println(itemObj.toString());
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
	
	public String httpPostQueryString(String apiUrl, String postStr) {
		try {
			System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");

			URL obj = new URL(apiUrl);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

			//add request header
			con.setRequestMethod("POST");
			//con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(postStr);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + apiUrl);
			System.out.println("Post parameters : " + postStr);
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
			return response.toString();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String httpPostWithoutData(String apiUrl, String accessToken, String postData) {
		try {
			URL obj = new URL(apiUrl);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

			//add request header
			con.setRequestMethod("POST");
			//con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			con.setRequestProperty("Authorization", "Bearer "+accessToken+"");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Content-Length", "0");

			// Send post request
			con.setDoOutput(true);

			// Send post request
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(postData);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + apiUrl);
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
			return response.toString();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public JSONObject getRefreshTokenFromCode(String serverAuthCode) {
		
		String authUrl = "https://accounts.google.com/o/oauth2/token";
		String postParams = "code="+serverAuthCode;
		postParams += "&client_id="+clientId;
		postParams += "&client_secret="+clientSecret;
		postParams += "&grant_type=authorization_code";
		postParams += "&redirect_uri=";
		
		String authResponse = httpPostQueryString(authUrl, postParams);
		if (authResponse != null) {
			try {
				JSONObject resp = new JSONObject(authResponse);
				return resp;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	public JSONObject getAccessTokenFromRefreshToken(String refreshToken) {
		
		String authUrl = "https://www.googleapis.com/oauth2/v4/token";
		String postParams = "refresh_token="+refreshToken;
		postParams += "&client_id="+clientId;
		postParams += "&client_secret="+clientSecret;
		postParams += "&grant_type=refresh_token";
		

		String authResponse = httpPostQueryString(authUrl, postParams);
		if (authResponse != null) {
			try {
				JSONObject resp = new JSONObject(authResponse);
				return resp;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	
	//To subscribe, Channel Id is necessary and should be same which is used very first time
	public JSONObject subscribeToGoogleEventWatcher(String accessToken, String channelId) {
		
		System.out.println("Subscribing.. Access Token : "+accessToken);
		String apiUrl = "https://www.googleapis.com/calendar/v3/calendars/primary/events/watch";
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DAY_OF_MONTH, 3);
			

			JSONObject postData = new JSONObject();
			postData.put("id", ""+channelId+"");
			postData.put("type", "web_hook");
			postData.put("address", "https://deploy.cenesgroup.com/api/event/google/notifyWebhook");
			postData.put("expiration", calendar.getTimeInMillis());

			JSONObject json = new JSONObject(httpPostWithoutData(apiUrl, accessToken, postData.toString()));
			System.out.println(json.toString());
			return json;

		} catch(Exception e) {
			e.printStackTrace();
		}
		return  null;
	}
	
	
	public void getAccessTokenFromRefreshTokenBkp(String refreshToken) {
		
		String url = "https://www.googleapis.com/oauth2/v4/token";
		//String url = "https://accounts.google.com/o/oauth2/token";
		try {
			URL obj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

			//add request header
			con.setRequestMethod("POST");
			//con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			String urlParameters = "code=4/gQA9i17zvlDMc1n557AolilRVxs32xPt7PLehE1tbZ3SUqx5LU0mqSJ5WiMWTUCiZOBVi8jP0OhDVPwWZ5ux15A&"
					+ "client_id=212716305349-dqqjgf3njkqt9s3ucied3bit42po3m39.apps.googleusercontent.com&"
					+ "client_secret=aaHR0dPqd57brx15We1sr1LE&grant_type=authorization_code";
			
			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Post parameters : " + urlParameters);
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
	}
	
	
	public GoogleCountries getCountries() {
		String placeApi = "http://cenes.test2.redblink.net/assets/countries-info.json";
		GoogleCountries gc = null;//doGoogleCountriesRestRequest(placeApi,HttpMethod.GET,null);
		return gc;
	}
	
	public void unsubscribeGoogleNotification(CalendarSyncToken calendarSyncToken) {
		
		
		JSONObject resp = getAccessTokenFromRefreshToken(calendarSyncToken.getRefreshToken());
		
		String url = "https://www.googleapis.com/calendar/v3/channels/stop";

		
		JSONObject jsonObj = new JSONObject();
		try {
			String accessToken = resp.getString("access_token");

			jsonObj.put("id", calendarSyncToken.getSubscriptionId());
			jsonObj.put("resourceId", calendarSyncToken.getResourceId());

			HttpService httpService = new HttpService();
			httpService.httpPostWithDataAccessToken(url, accessToken, jsonObj.toString());

		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	//Subscription call
	/*public static void main(String[] args) {
		GoogleService gs = new GoogleService();
		try {
			JSONObject json = gs.getAccessTokenFromRefreshToken("1/kXkYzmXkrGJwza7UgJ5nTiQultJPE3KeKRPyGxfv6B4Oh2nQIZv7D-QADMq-Zhde");
			String accessToken = json.getString("access_token");
			//String accessToken = "ya29.GlsYB8zmcdMOBQ77Hr5k5I5Ucs6XQgj3lfM4EK87lJ4zuQBUbXIiGWqk8qP61VJYNItVyURxo_phfjZtRvWN44bfQsmUTXWkP32lNWDhd2LkjIAsuf1D57Ct8Yzb";
			JSONObject json2 = gs.subscribeToGoogleEventWatcher(accessToken);
			System.out.println(json2.toString());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}*/
	/*public static void main(String[] args) {
		//new GoogleService().getCountryHolidayEvents("en.canadian#holiday@group.v.calendar.google.com");
		
		new GoogleService().getCalenderEvents(false, "ya29.Gly3Bh9sw_7rm0Cl8GXeS0eKOp7R_ZfVfKFmAcRduTk1GcCkI_TUTzZdKqMZ8K1Z4_3rn0aOznZPt05O5ygtdkrNpFmjTV6oEOZ5eFABDwpDDGx0G6DOJ-rX-We5Xg");
	}*/
	
	/*public static void main(String[] args) {
		Calendar maxTimeCal = Calendar.getInstance();
		maxTimeCal.set(Calendar.HOUR_OF_DAY, 0);
		maxTimeCal.set(Calendar.MINUTE, 0);
		maxTimeCal.set(Calendar.SECOND, 0);
		maxTimeCal.add(Calendar.MONTH, 3);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");

		System.out.println(URLEncoder.encode(sdf.format(maxTimeCal.getTime())));
	}*/
	/*public static void main(String[] args) {
		GoogleService gs = new GoogleService();
		
		String code = "4/gQAMvMDxd-8HMgUURTzygIbqipG4q8zQU_IYpiDxsWvqJQHKMm66V0Qx4T420PD3aj0j8muMsLRU1rRfL66zcy0";
		
		gs.getRefreshTokenFromCode(code);
		
		String refToken = "1/FJyHUGLkgKxPd7doia2IHtI13txoC6h43_bHzdAV7m8";
		gs.getAccessTokenFromRefreshToken(refToken);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");

		Calendar minTimeCal = Calendar.getInstance();
		minTimeCal.set(Calendar.HOUR_OF_DAY, 0);
		minTimeCal.set(Calendar.MINUTE, 0);
		minTimeCal.set(Calendar.SECOND, 0);
		
		Calendar maxTimeCal = Calendar.getInstance();
		maxTimeCal.set(Calendar.HOUR_OF_DAY, 0);
		maxTimeCal.set(Calendar.MINUTE, 0);
		maxTimeCal.set(Calendar.SECOND, 0);
		maxTimeCal.add(Calendar.MONTH, 3);
		
		System.out.println(URLEncoder.encode(sdf.format(minTimeCal.getTime())));
		
		System.out.println(URLEncoder.encode(sdf.format(maxTimeCal.getTime())));
		
		
		System.out.println(URLEncoder.encode("en.malaysia#holiday@group.v.calendar.google.com"));
	}*/
	
	/*public static void main(String[] args) {
		GoogleService gs = new GoogleService();
		System.out.println(gs.getAccessTokenFromRefreshToken("1/vtmthlD7W_4ufUQk5J48S89EIdLP9KP-xlM7ttcEXuw"));
	}*/
}
