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
import com.cg.utils.CenesUtils;

public class PushNotificationService {

/*	public static void main(String[] args) {
		JSONArray toArr = new JSONArray();
		try {
			toArr.put("e7IIzk-L2P4:APA91bEhN4UMqNyMmabT8R9RCQQLo9CqAnq5y8lrBE78MaTzDxwoXkzMuhcz1c9GXmmkNBcfTSFEhdtaLcklZeIir0vPVeXSBwOK1F4YjSDbM4eWfoDW59byN4ov5mHKs21HRc9JNSp6");
			JSONObject payloadObj = new JSONObject();
			payloadObj.put("notificationTypeTitle","Aajo");
			payloadObj.put("notificationTypeId",145);
			payloadObj.put("notificationType",NotificationType.Gathering.toString());
			
			JSONObject notifyObj = new JSONObject();
			notifyObj.put("title", "mandeep");
			notifyObj.put("body", " invited you to his event Party");
			notifyObj.put("payload", payloadObj);
			sendAndroidPush(toArr,notifyObj);

		} catch(Exception e){
			e.printStackTrace();
		}
	}*/
	
	/*public static void main(String[] args) {
		List devices = new ArrayList<>();
		devices.add("429e2452be7b0694ccaca988180233708abc3d74bfba0361de6a4da402b4ede0");
		
		JSONObject notifyObj = new JSONObject();
		
		try {
			JSONObject payloadObj = new JSONObject();
			payloadObj.put("notificationTypeId","123");
			payloadObj.put("notificationTypeLocation","asadsdfsdsfgfgfghfghfh");
			payloadObj.put("notificationTypeTime","23435654676434");

			payloadObj.put("notificationType",NotificationType.Reminder.toString());
			
			JSONObject alert = new JSONObject();
			alert.put("title","Mandy called you to his event Welcome");
			//alert.put("content-available",1);
			payloadObj.put("alert",alert);
			notifyObj.put("aps", payloadObj);
			
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
            List<PushedNotification> NOTIFICATIONS = Push.payload(payload, "/home/mandeep/svn/cenes/cenes/src/main/resources/cenesGroupProd.p12", "123456", false, devices);
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
