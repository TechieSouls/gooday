package com.cg.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import com.cg.events.bo.GoogleEvents;

public class HttpService {

	public JSONObject getRequestWithAuthorization(String url, String method, String token){
		System.out.println("["+new Date()+" Making Google Calendar List Request,url : "+url);
		System.out.println("["+new Date()+" Access Token : "+token);
		JSONObject jObject = null;
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod(method);
			con.setRequestProperty("Authorization", "Bearer "+token);
			int responseCode = con.getResponseCode();
			System.out.println(con.getContent().toString());
			if (responseCode == 200) {

				System.out.println("Response Code : " + responseCode);

				BufferedReader in = new BufferedReader(new InputStreamReader(
						con.getInputStream(), Charset.forName("UTF-8")));
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
						con.getInputStream(), Charset.forName("UTF-8")));
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
}
