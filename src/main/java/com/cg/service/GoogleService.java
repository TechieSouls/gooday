package com.cg.service;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.cg.events.bo.GoogleEvents;

public class GoogleService {
	private String events_list_api = "https://www.googleapis.com/calendar/v3/calendars/primary/events";
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
	
	public GoogleEvents getCalenderEvents(String accessToken) {
		Date currentDate = new Date();
		String calenderAPI = events_list_api+"?aaccess_token="+accessToken+"&future_events=true&timeMin="+sdf.format(currentDate);
		try {
			GoogleEvents events = doGoogleEventsRestRequest(calenderAPI,HttpMethod.GET,null);
			return events;
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
}
