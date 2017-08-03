package com.cg.service;

import java.util.Arrays;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.cg.bo.FacebookProfile;
import com.cg.events.bo.FacebookEvents;

public class FacebookService {
	
	String facebook_profile_api = "https://graph.facebook.com/me";
	
	public FacebookProfile facebookProfile(String accessToken) {
		String api = facebook_profile_api+"?access_token="+accessToken;
		FacebookProfile fp = doFacebookProfileRestRequest(api,HttpMethod.GET,null);
	    return fp;
	}
	
	
	public FacebookEvents facebookEvents(String facebookId,String accessToken) {
			
		String fields = "id,description,end_time,name,place,start_time,rsvp_status,timezone,attending,maybe,declined";
		String eventsApi = "https://graph.facebook.com/"
				+ facebookId
				+ "/events?fields="+fields+"&access_token="
				+ accessToken;
		FacebookEvents events = doFacebookEventsRestRequest(eventsApi,HttpMethod.GET,null);
		return events;
	}
	
	public <T> FacebookEvents doFacebookEventsRestRequest(String url, HttpMethod method, T body){
		System.out.println("["+new Date()+" Making Request,url : "+url);
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(new MediaType[]{MediaType.APPLICATION_JSON}));
		HttpEntity<T> entity = new HttpEntity<T>(body, headers);
		FacebookEvents orderEntity = null;
		try{
			orderEntity = restTemplate.exchange(url,method, entity, FacebookEvents.class).getBody();
		}catch (HttpClientErrorException e) {
		      System.out.println("FacebookService ErrorCode: "+e.getStatusCode());
		      System.out.println("FacebookService ErrorMessage: "+e.getResponseBodyAsString());
		      throw e;
		}catch (HttpServerErrorException e) {
		      System.out.println("FacebookService ErrorCode: "+e.getStatusCode());
		      System.out.println("FacebookService ErrorMessage: "+e.getResponseBodyAsString());
		      throw e;
		}
		System.out.println("["+new Date()+" Request Complete");
		return orderEntity;
	}
	
	public <T> FacebookProfile doFacebookProfileRestRequest(String url, HttpMethod method, T body){
		System.out.println("["+new Date()+" Making Request,url : "+url);
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(new MediaType[]{MediaType.APPLICATION_JSON}));
		HttpEntity<T> entity = new HttpEntity<T>(body, headers);
		FacebookProfile orderEntity = null;
		try{
			orderEntity = restTemplate.exchange(url,method, entity, FacebookProfile.class).getBody();
		}catch (HttpClientErrorException e) {
		      System.out.println("FacebookService ErrorCode: "+e.getStatusCode());
		      System.out.println("FacebookService ErrorMessage: "+e.getResponseBodyAsString());
		      throw e;
		}catch (HttpServerErrorException e) {
		      System.out.println("FacebookService ErrorCode: "+e.getStatusCode());
		      System.out.println("FacebookService ErrorMessage: "+e.getResponseBodyAsString());
		      throw e;
		}
		System.out.println("["+new Date()+" Request Complete");
		return orderEntity;
	}
}
