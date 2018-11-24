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
	    if (outlookEvents != null) {
		    outlookCalenderEvents.add(outlookEvents);
	    }
		return outlookCalenderEvents;
	}
	
	public List<OutlookEvents> getIosOutlookEvents(String accessToken) {
		List<OutlookEvents> outlookCalenderEvents = new ArrayList<>();
		String outlookApi = "https://graph.microsoft.com/v1.0/me/events";
		JSONObject outlookJSON = doOutlookCalendarRestRequest(outlookApi, "GET", accessToken);
	    OutlookEvents outlookEvents = parseIosOutlookEvents(outlookJSON);
	    if (outlookEvents != null) {
		    outlookCalenderEvents.add(outlookEvents);
	    }
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
		OutlookEvents events = null;
		try {
			if (responseObj.has("value") && responseObj.getJSONArray("value").length() > 0) {
				events = new OutlookEvents();
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
					
					if (outlookJson.has("IsAllDay")) {
						item.setIsAllDay(outlookJson.getBoolean("IsAllDay"));
					}
					
					items.add(item);
				}
				events.setValue(items);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return events;
	}
	
	public OutlookEvents parseIosOutlookEvents(JSONObject responseObj) {

		OutlookEvents events = null;
		try {
			if (responseObj.has("value") && responseObj.getJSONArray("value").length() > 0) {
				
				events = new OutlookEvents();
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
					
					if (outlookJson.has("isAllDay")) {
						item.setIsAllDay(outlookJson.getBoolean("isAllDay"));
					}
					
					items.add(item);
				}
				events.setValue(items);
			} else {
				//events.setErrorCode(101);
				//events.setErrorDetail("Token Expired");
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
		String refreshToken = "OAQABAAAAAAC5una0EUFgTIF8ElaxtWjTNHUJdhthIYK3kNq4mLCMO4gjcj3EfiycUnM1MS4rMAhOrKgPDjottBZ9GDQ8Uha7Ya9ePO952E7p0eFQAC_reEEbFEerYNFkJS7yXYaSj1Yl-A8trRE96j-dr3arZWncC9W6fuqeHOkn6qcB7sltT4BMIWhrCJn8wI-6ZXyNxoP6N52SwaYS2xCCl3L7jVITaOsq_591wJ_Ad68o-az49ZpjXH7TJj-653yLY1kBzH5bLKXsriV3fgNNx44FAg3iKOMfqi1LfhLbsV25eIoRuzRAumC21jCPADha_e2JHBJG0S9B4JX4K7W25sR8a0mKm8s4D2vpA_kCGMwzW7x4oGOYpv_W_S4nI92kpp1MxOcnisGJbDl1_aoQtLSrGAvQ3kCdlix-KgS7wJ2h0j8t1i1Zb5e2QBZya6qj5QNY28Mk2lQGOECRtEKe9vRY-tS9ryQgG0sykUihURBM307v_-UrIUqH84rFeHYVO90O03wtKhZTwUepD-vmWepL-JIOTtuVrnwm7nXId1ObYUJXInmXcrPcE8UKH4O9dv5jyL14w_tWfpq5SncBKDkngt_7xpKpaTLORiLRkg5g3jrtAGEjL83HJE2ufCzdEfDdwQdP7QnuKrxp7-GM5g-I0kcIYsF7--2QMBONOO0cOLlPYzUv9SeYEbVnbs3KZ1aaZZD4X9OopNuon7BZZQ_Xy5uAS9LxtpukhPdoaNP3MzZtJg5Mh2BODB_l0cLk_lNqxGx5xJb8TogPddmsOz5sv8lbtSK07cjQAM8IDCO3uTSk6iAA";
		//os.getAccessTokenFromRefreshToken(refreshToken);
		
		String accessToken = "eyJ0eXAiOiJKV1QiLCJub25jZSI6IkFRQUJBQUFBQUFDNXVuYTBFVUZnVElGOEVsYXh0V2pUTjRRN25NQXk4bkdjV01JSVJZU0VjM3RKbVBadWVVOVJQUGNNTnl0OXhadGI3N2kyY2N5eWxnQTN3WkhhUUMwOHBCbk82eHdDSExZTXM1eWwtb2NDUnlBQSIsImFsZyI6IlJTMjU2IiwieDV0Ijoid1VMbVlmc3FkUXVXdFZfLWh4VnRESkpaTTRRIiwia2lkIjoid1VMbVlmc3FkUXVXdFZfLWh4VnRESkpaTTRRIn0.eyJhdWQiOiJodHRwczovL2dyYXBoLm1pY3Jvc29mdC5jb20iLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC9mNWIyYWJlYy1mZWIxLTQ0M2ItYmYwNy1lZWUzNThkZmU1NWMvIiwiaWF0IjoxNTQyMTg4NDI0LCJuYmYiOjE1NDIxODg0MjQsImV4cCI6MTU0MjE5MjMyNCwiYWNjdCI6MCwiYWNyIjoiMSIsImFpbyI6IkFTUUEyLzhKQUFBQWNyYnVhejdscGZCUUlKd2tCZFdzMTQrNEVkL09yQUFpc1lsRG5nMVV3TWc9IiwiYW1yIjpbInB3ZCJdLCJhcHBfZGlzcGxheW5hbWUiOiJDZW5lcyIsImFwcGlkIjoiMGIyMjgxOTMtMjZmMi00ODM3LWI3OTEtZmZkN2VhYjc0NDFlIiwiYXBwaWRhY3IiOiIwIiwiZmFtaWx5X25hbWUiOiJaaGFuZyIsImdpdmVuX25hbWUiOiJIYXJyeSIsImlwYWRkciI6IjExMi4xOTYuNTUuMTg3IiwibmFtZSI6IkhhcnJ5IFpoYW5nIiwib2lkIjoiNWEwM2I2MTYtMGM2NC00N2MyLWEwOGItMDY2OWJlZmRmMTY4Iiwib25wcmVtX3NpZCI6IlMtMS01LTIxLTI1ODIyOTYyOTMtMzU4MzI5OTIzNC00MjkxODE2MDE3LTM2MDgiLCJwbGF0ZiI6IjIiLCJwdWlkIjoiMTAwMzAwMDBBRTM5MTRFOCIsInNjcCI6IkNhbGVuZGFycy5SZWFkIENvbnRhY3RzLlJlYWQgTWFpbC5SZWFkIG9wZW5pZCBwcm9maWxlIFVzZXIuUmVhZCBlbWFpbCIsInN1YiI6InNGbjBGclhocDlZSnpPazctUDRwTjBlM1dZU2RzVzZpZ09nazJ3aC1oNmciLCJ0aWQiOiJmNWIyYWJlYy1mZWIxLTQ0M2ItYmYwNy1lZWUzNThkZmU1NWMiLCJ1bmlxdWVfbmFtZSI6IkhaaGFuZ0BicnVuZWxsb2N1Y2luZWxsaXVzYS5jb20iLCJ1cG4iOiJIWmhhbmdAYnJ1bmVsbG9jdWNpbmVsbGl1c2EuY29tIiwidXRpIjoiQmRYak9oLTRhMDZybkJ4dml3UU9BQSIsInZlciI6IjEuMCIsInhtc19zdCI6eyJzdWIiOiJhdlRJNTI1aVpKZldKa0djMjBLdmVTX1Y2bkNlMGZFeGdmMkNwTnJ4QldjIn0sInhtc190Y2R0IjoxNTEyMzk3NzM5fQ.NSWr_rID0UPnKYEbrdFxmH-dYOicagx19KlOb__JdKxUTqqyv5RiaQ3viC_fWOriX0fd_qRr-P4RL2uhr-k-tP7omyheHhgmjE7y6ggFd-EYVlOGMyAfHgaPKktZz-EoX--A-gRopbjuOyOgGV-H9pI6G5lWidRwhBpHbVJUon4wULy-etTXMKcOGZv0YOyRuJEu8b616HKsjHpgBBk76xKM__TuLG-FaO1GK9LVv8aX8qOHiOi_iC7FUJgR1H2lvwlrvh2Bpx--xWtxDHDkcVV-4wpN1XMr5y_SAV1JyJiRT9x8n3N60Qkj53k2UbdIvrR9IDyrxGtXgSsD_ZtELw";
		os.getOutlookCalenderEvents(accessToken);
	}*/
	
	/*public static void main(String[] args) {
		
		String accessToken = "EwAgA61DBAAUcSSzoTJJsy+XrnQXgAKO5cj4yc8AAXwt2T9D4X6nVdVUWfDtCO/ILXB0iTn3VjSddBu/0XNLrsF6rAAMChv2bkZoAdubRYZMQq9m9RlcRL6Aqm2cW/BsKz8YRcLYPh23WXnH2LAXXfTplhXzfmDepdopA+4g/iALsSHDEiAg/Nqg8KnYGIvGDFi3ARFwBPBa+yi4GIp0J7TMCHBSCDxqCh+hqBko3Ga/IWLDf9V2ktoqZCks09PN2PY+W8CHxrgtj/EUqyLc0a+/eU4Ct5zFs3TexWY1HIM+TK/Wk0xKq8/mT71fAKlPANVv+hiauanHkr2rUtUFAqiqo+Veh7g5UB224rXpndD1nAtf/LOsPOp9Gf6wlQQDZgAACBd5Do73Dlvw8AFPFZoySwtGCARJlJmlnN0GRdwSJ7e+1Pl1FcNynsbWx+JMIQFOsHY2OH57XKaASpFJDfa3ulBv+AHMoxuBuh60S4xz1vH4GntFry5caAhHFke1gv1Dte8MfKz7rjMPhKvF9ArYJrSwUXEA3aip7sGoMTc49RCKSAm/p1gM0LrWbV84xl7tVXA4c9TM24zc4tnhU7n+TGclWq+I0zCoUEJ3jgE/yYEOlIMs0pIllFzrdiobPinhq3VfpzmUbiQL5olCP5bQhJPfoc+bIJjC1PMhu7bsKYa2SMJ6UySD4I9sPOVgXaQ/bGm4hfnwjG/NIzNCL1Yd9Luz4UyAb+7qmaWQjI4JCwx9HjKPuZIimxYxDng9RVMXTV8ngF47rDmtDHTtRpd7g+UoTNCSpcvI7QibxVYgafmqgCx9JiYgkJSFrgU+3+hmOrhKJpla1LbBwmr7i3l5I6G6dCvvP4BJCkEAb9Kr+WQq7qVFNqwBi58xLAxKhEouIN5c2jyF7MKi4ipeTkatzcUFM4MlTA32j+EOsm4cKDS0Aatlld0zBIrvJl23hDGWeRpsxgBvubma6iIHUHbXdCTNffDkeB4fq+O9pjh4ZENPaTYP5jP/AEuX/z7LQ2kZ10yg1a080rBXMzfpY3w8FMx3+VLLHgiiOTwQIgI=";
		
		OutlookService os = new OutlookService();
		os.getOutlookCalenderEvents(accessToken);
	}*/
}
