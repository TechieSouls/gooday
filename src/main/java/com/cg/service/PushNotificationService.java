package com.cg.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javapns.Push;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotification;
import javapns.notification.ResponsePacket;

import org.apache.log4j.BasicConfigurator;
import org.primefaces.json.JSONArray;
import org.primefaces.json.JSONObject;

import com.cg.bo.Notification.NotificationType;
import com.cg.constant.CgConstants;
import com.cg.utils.CenesUtils;

public class PushNotificationService {

	/*public static void main(String[] args) {
		JSONArray toArr = new JSONArray();
		try {
			toArr.put("c-hMWm6CENs:APA91bGIxCV-MJScpouTcUYf3WxWWmpzkW2tsmv5t07V2OeEt6F6uP2i2qA7jJij_V0d_8wMbpXphCea76eyee7MMt0xT9uz9Iey_fQ8nsmveuxNFLN7r6OUO0oB4XCPbbKt5ypPNrH4");
			JSONObject payloadObj = new JSONObject();
			payloadObj.put(CgConstants.notificationTypeTitle,"Aajo");
			payloadObj.put(CgConstants.notificationTypeId,145);
			payloadObj.put(CgConstants.notificationType,NotificationType.Gathering.toString());
			
			JSONObject notifyObj = new JSONObject();
			notifyObj.put("title", "mandeep");
			notifyObj.put("body", " invited you to his event Party");
			notifyObj.put("payload", payloadObj);
			sendAndroidPush(toArr,notifyObj);

		} catch(Exception e) {
			e.printStackTrace();
		}
	}*/
	
	/*public static void main(String[] args) {
		List devices = new ArrayList<>();
		devices.add("bef89acbe4e53690c9988452a9e972d9cb0c7688498eaca5e4ba429a91067df4");
		
		JSONObject notifyObj = new JSONObject();
		
		try {
			JSONObject payloadObj = new JSONObject();
			//payloadObj.put("notificationTypeId","123");
			//payloadObj.put("notificationTypeLocation","asadsdfsdsfgfgfghfghfh");
			//payloadObj.put("notificationTypeTime","23435654676434");
//
			//payloadObj.put("notificationType",NotificationType.Reminder.toString());
			
			payloadObj.put(CgConstants.notificationTypeTitle,"Title of pUsh");
			payloadObj.put(CgConstants.notificationTypeId,2453453);
			payloadObj.put(CgConstants.notificationType,NotificationType.Gathering.toString());
			
			JSONObject alert = new JSONObject();
			alert.put("title","Title of piusj");
			alert.put("body","Heyh hey");

			payloadObj.put("alert",alert);
			payloadObj.put("badge",1);
			payloadObj.put("sound","cenes-notification-ringtone.aiff");

			notifyObj.put("aps", payloadObj);

			//alert.put("content-available",1);
			//payloadObj.put("alert",alert);
			//payloadObj.put("content-available",1);
			//payloadObj.put("type","HomeRefresh");
			//notifyObj.put("aps", payloadObj);
			
			//{"aps":{"alert":{"title":"Hello from APNs Tester."},"notificationTypeId":"123","nType":"Reminder"}}
		} catch(Exception e) {
			e.printStackTrace();
		}
		sendIosPushNotification(devices,notifyObj);
	}*/
	
	public static void sendAndroidPush(JSONArray to, JSONObject notifyObj) {
		
		try {
			JSONObject json = new JSONObject();
		    json.put("data",notifyObj);
		    json.put("registration_ids", to);
		    doPostRequest(json.toString());
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	public static JSONObject doPostRequest(String postData){
		String androidPushUrl = "https://fcm.googleapis.com/fcm/send";
 
		System.out.println("["+new Date()+" Making Push Notification Request,url : "+androidPushUrl);
		JSONObject jObject = null;
		try {
			URL obj = new URL(androidPushUrl);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setRequestProperty("Authorization", "key="+CenesUtils.googleAPIKey);
			con.setRequestProperty("Content-Type", "application/json");
			
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            try {
                wr.writeBytes(postData);
                wr.flush();
                wr.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            con.connect();

            int responseCode = con.getResponseCode();
			System.out.println(con.getContent().toString());
			if (responseCode == 200) {

				BufferedReader in = new BufferedReader(new InputStreamReader(
						con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				jObject = new JSONObject(response.toString());
				System.out.println(response.toString());
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
		System.out.println("["+new Date()+" Push Notification Request Complete");
		return jObject;
	}
	
	
	public static void sendIosPushNotification(List devices,JSONObject notifyObj) {
		BasicConfigurator.configure();
        try {
            PushNotificationPayload payload = PushNotificationPayload.fromJSON(notifyObj.toString());
            //payload.addAlert(notifyObj.getString("senderName")+" "+notifyObj.getString("message"));
            //payload.addBadge(1);
            //payload.addSound("default");
            System.out.println(payload.toString());
            //List<PushedNotification> NOTIFICATIONS = Push.payload(payload, "/Users/cenes_dev/Desktop/Mandeep/projects/java/gooday-beta/gooday/src/main/resources/beta_development_push.p12", "123456", true, devices);
            //List<PushedNotification> NOTIFICATIONS = Push.payload(payload, "/Users/cenes_dev/Desktop/Mandeep/projects/java/gooday-beta/gooday/src/main/resources/beta_push_cert.p12", "123456", true, devices);

            //This is when build is in test flight or diawi link
            //List<PushedNotification> NOTIFICATIONS = Push.payload(payload, "/home/ubuntu/garage/beta/gooday/src/main/resources/beta_development_push.p12", "123456", true, devices);
            
            //This is when build is on App Store.. 
            List<PushedNotification> NOTIFICATIONS = Push.payload(payload, "/home/ubuntu/garage/beta/gooday/src/main/resources/beta_push_cert.p12", "123456", true, devices);
            for (PushedNotification NOTIFICATION: NOTIFICATIONS) {
                if (NOTIFICATION.isSuccessful()) {
                     //APPLE ACCEPTED THE NOTIFICATION AND SHOULD DELIVER IT 
                    System.out.println("PUSH NOTIFICATION SENT SUCCESSFULLY TO: " +NOTIFICATION.getDevice().getToken());
                     //STILL NEED TO QUERY THE FEEDBACK SERVICE REGULARLY 
                } else {
                    String INVALIDTOKEN = NOTIFICATION.getDevice().getToken();
                     //ADD CODE HERE TO REMOVE INVALIDTOKEN FROM YOUR DATABASE 
                     //FIND OUT MORE ABOUT WHAT THE PROBLEM WAS 
                    Exception THEPROBLEM = NOTIFICATION.getException();
                    THEPROBLEM.printStackTrace();
                     //IF THE PROBLEM WAS AN ERROR-RESPONSE PACKET RETURNED BY APPLE, GET IT 
                    ResponsePacket THEERRORRESPONSE = NOTIFICATION.getResponse();
                    if (THEERRORRESPONSE != null) {
                        System.out.println(THEERRORRESPONSE.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}
}
