package com.cg.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.primefaces.json.JSONArray;
import org.primefaces.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cg.bo.Notification;
import com.cg.bo.Notification.NotificationType;
import com.cg.bo.Notification.NotificationTypeStatus;
import com.cg.bo.NotificationCountData;
import com.cg.constant.CgConstants;
import com.cg.events.bo.Event;
import com.cg.events.bo.EventMember;
import com.cg.events.bo.EventMember.MemberStatus;
import com.cg.reminders.bo.Reminder;
import com.cg.reminders.bo.ReminderMember;
import com.cg.repository.NotificationCountDataRepository;
import com.cg.repository.NotificationRepository;
import com.cg.service.PushNotificationService;
import com.cg.service.UserService;
import com.cg.user.bo.User;
import com.cg.user.bo.UserDevice;

@Service
public class NotificationManager {
	
	@Autowired
	NotificationRepository notificationRepository;
	
	@Autowired
	UserService userService;
	
	@Autowired
	EventManager eventManager;
	
	@Autowired
	ReminderManager reminderManager;
	
	@Autowired
	NotificationCountDataRepository notificationCountDataRepository;
	
	public Notification saveNotification(Notification notification) {
		return notificationRepository.save(notification);
	}
	
	public Notification findNotificationByNotificationTypeIdRecepientId(Long reminderId,Long reminderMemberId) {
		return notificationRepository.findByNotificationTypeIdAndRecepientId(reminderId, reminderId);
	}
	
	public void deleteNotificationByRecepientIdNotificationTypeId(Long recepientId,Long notificationTypeId) {
		notificationRepository.deleteByRecepientIdAndNotificationTypeId(recepientId, notificationTypeId);
	}
	
	public void deleteNotificationByNotificationTypeId(Long notificationTypeId) {
		notificationRepository.deleteByNotificationTypeId(notificationTypeId);
	}
	
	public Long findNotificationCountsByReceipientIdAndReadStatus(Long receipientId,Notification.NotificationReadStatus readStatus) {
		List<Object> notificationCounts = notificationRepository.findCountsByRecepientIdAndReadStatus(receipientId, readStatus);
		for (Object object : notificationCounts) {
			if(object!=null){
				return (Long)object;
			} else {
				return 0l;
			}
		}
		return 0l;
	}
	
	public void sendGatheringNotification(Event event) {
		User fromUser = userService.findUserById(event.getCreatedById());
		if (event.getEventMembers() != null && event.getEventMembers().size() > 0) {
			System.out.println("[CreateEvent : "+new Date()+", Event Member Size : "+event.getEventMembers().size()+"]");
		}
		
		Map<String,JSONArray> androidMap = new HashMap<>();
		Map<String,List> iOSMap = new HashMap<>();
		
		
		List<EventMember> eventMembers = event.getEventMembers();
		for (EventMember eventMember : eventMembers) {
			if (!eventMember.getUserId().equals(event.getCreatedById())) {
				boolean notificationAlreadySent = false;
				String eventMessage = "sent you an invitation";
				
				Notification notification = notificationRepository.findByNotificationTypeIdAndRecepientId(event.getEventId(),eventMember.getUserId());
				if (notification == null) {
					notification = new Notification();
				}
				
				notification.setSenderId(fromUser.getUserId());
				notification.setSender(fromUser.getName());
				if (fromUser.getPhoto() != null) {
					notification.setSenderPicture(fromUser.getPhoto());
				}
				
				if (eventMember.getStatus() != null) {
					eventMessage = " updated an invitation ";
					notificationAlreadySent = true;
					notification.setNotificationTypeStatus(NotificationTypeStatus.Old);
				}
				notification.setMessage(eventMessage);
				notification.setTitle(event.getTitle());
				notification.setRecepientId(eventMember.getUserId());
				notification.setNotificationTypeId(event.getEventId());
				notification.setType(NotificationType.Gathering);
				notification.setCreatedAt(new Date());
				notification.setUpdateAt(new Date());
				if (!notificationAlreadySent) {
					notificationRepository.save(notification);
				}
				
				List<UserDevice> toUserDeviceInfo = userService.findUserDeviceInfoByUserId(eventMember.getUserId());
				if (toUserDeviceInfo != null && toUserDeviceInfo.size() > 0) {
					for (UserDevice userDevice : toUserDeviceInfo) {
						if ("android".equals(userDevice.getDeviceType())) {
							JSONArray toAndroidArray = new JSONArray();
							if (androidMap.containsKey("old")) {
								toAndroidArray = androidMap.get("old");
							}
							
							String mapKey = "new";
							if (notificationAlreadySent) {
								mapKey = "old";
							}
 							toAndroidArray.put(userDevice.getDeviceToken());
 							androidMap.put(mapKey, toAndroidArray);
						} else if ("ios".equals(userDevice.getDeviceType())) {
							
							List toIosArray = new ArrayList();
							if (iOSMap.containsKey("old")) {
								toIosArray = iOSMap.get("old");
							}
							
							String mapKey = "new";
							if (notificationAlreadySent) {
								mapKey = "old";
							}
							toIosArray.add(userDevice);
							iOSMap.put(mapKey, toIosArray);
						}
					}
				}
			}
		}
		
		try {
			for (Entry<String,JSONArray> androidSet : androidMap.entrySet()) {

				String pushMessage = " sent you an invitation ";
				
				JSONObject payloadObj = new JSONObject();
				payloadObj.put(CgConstants.notificationTypeTitle,event.getTitle());
				payloadObj.put(CgConstants.notificationTypeId,event.getEventId());
				payloadObj.put(CgConstants.notificationType,NotificationType.Gathering.toString());
				if (androidSet.getKey().equals("old")) {
					payloadObj.put(CgConstants.notificationTypeStatus,"Old");
					pushMessage = " updated an invitation ";
				} else {
					payloadObj.put(CgConstants.notificationTypeStatus,"New");
				}
				
				JSONObject notifyObj = new JSONObject();
				notifyObj.put("title", fromUser.getName());
				notifyObj.put("body", pushMessage+event.getTitle());
				notifyObj.put("payload", payloadObj);
				
				PushNotificationService.sendAndroidPush(androidSet.getValue(),notifyObj);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		try {
			for (Entry<String,List> iosSet : iOSMap.entrySet()) {
				
				for (UserDevice userDevice : (List<UserDevice>)iosSet.getValue()) {
					String pushMessage = " sent you an invitation ";
					JSONObject notifyObj = new JSONObject();
					
					JSONObject payloadObj = new JSONObject();
					payloadObj.put(CgConstants.notificationTypeTitle,event.getTitle());
					payloadObj.put(CgConstants.notificationTypeId,event.getEventId());
					payloadObj.put(CgConstants.notificationType,NotificationType.Gathering.toString());
					if (iosSet.getKey().equals("old")) {
						payloadObj.put(CgConstants.notificationTypeStatus,"Old");
						pushMessage = " updated an invitation ";
					} else {
						payloadObj.put(CgConstants.notificationTypeStatus,"New");
					}
					
					JSONObject alert = new JSONObject();
					alert.put("title",fromUser.getName()+pushMessage+event.getTitle());
					payloadObj.put("alert",alert);
					payloadObj.put("badge",getBadgeCountsByUserId(userDevice.getUserId()));
					payloadObj.put("sound","cenes-notification-ringtone.aiff");

					notifyObj.put("aps", payloadObj);
					
					List tokenList = new ArrayList();
					tokenList.add(userDevice.getDeviceToken());
					PushNotificationService.sendIosPushNotification(tokenList,notifyObj);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("[CreateEvent : "+new Date()+", Notification Sent]");
	}
	
	public void sendEventAcceptDeclinedPush(EventMember eventMember) {
		String pushMessage = "";
		if (eventMember.getStatus().equals(MemberStatus.Going.toString())) {
			pushMessage = "[username] accepts your invitation [title]";
		} else if (eventMember.getStatus().equals(MemberStatus.NotGoing.toString())) {
			pushMessage = "[username] declines your invitation [title]";
		}

		Event event = eventManager.findEventByEventId(eventMember.getEventId());
		pushMessage = pushMessage.replace("[username]",eventMember.getName()).replace("[title]", event.getTitle());
		sendPushForAcceptAndDeclineRequest(pushMessage,event.getCreatedById(),eventMember.getName(),Notification.NotificationType.Gathering);
	}

	public void sendReminderAcceptDeclinedPush(ReminderMember reminderMember) {
		String pushMessage = "";
		if (reminderMember.getStatus().equals("Accept")) {
			pushMessage = "[username] accepted your reminder request [title]";
		} else if (reminderMember.getStatus().equals("Declined")) {
			pushMessage = "[username] declined your reminder request [title]";
		}

		Reminder reminder = reminderManager.findReminderByReminderId(reminderMember.getReminderId());
		pushMessage = pushMessage.replace("[username]",reminderMember.getName()).replace("[title]", reminder.getTitle());
		sendPushForAcceptAndDeclineRequest(pushMessage,reminder.getCreatedById(),reminderMember.getName(),Notification.NotificationType.Reminder);
	}
	
	public void sendReminderCompletedPush(Reminder reminder,User user) {
		String pushMessage = "[username] completed the reminder [title]";

		pushMessage = pushMessage.replace("[username]",user.getName()).replace("[title]", reminder.getTitle());
		sendCompletedReminderAlertPush(pushMessage,reminder,user);
	}
	
	public void sendCompletedReminderAlertPush(String pushMessage,Reminder reminder,User user) {
			JSONArray toAndroidArray = new JSONArray();
			List toIosArray = new ArrayList<>();
			
			List<ReminderMember> reminderMembers = reminder.getReminderMembers();
			for (ReminderMember reminderMember : reminderMembers) {
				if (reminderMember.getStatus() != null && reminderMember.getStatus().equals("Accept") && !reminderMember.getMemberId().equals(user.getUserId())) {
					
					List<UserDevice> userDevices = userService.findUserDeviceInfoByUserId(reminderMember.getMemberId());
					if (userDevices != null && userDevices.size() > 0) {
						for (UserDevice userDevice : userDevices) {
							if ("android".equals(userDevice.getDeviceType())){
								toAndroidArray.put(userDevice.getDeviceToken());
							} else if ("ios".equals(userDevice.getDeviceType())){
								toIosArray.add(userDevice.getDeviceToken());
							}
						}
					}
					
				}
			}
			
			try {
				if (toAndroidArray.length() > 0) {
					JSONObject notifyObj = new JSONObject();
					notifyObj.put("title", "Reminder");
					notifyObj.put("body", pushMessage);
					
					PushNotificationService.sendAndroidPush(toAndroidArray,notifyObj);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}

			try {
				if (toIosArray.size() > 0) {
					
					List<UserDevice> iosUserDevices = toIosArray;
					for (UserDevice userDevice : iosUserDevices) {
						JSONObject notifyObj = new JSONObject();
						JSONObject payloadObj = new JSONObject();
						JSONObject alert = new JSONObject();
						alert.put("title","Reminder : "+pushMessage);
						payloadObj.put("alert",alert);
						payloadObj.put("badge",getBadgeCountsByUserId(userDevice.getUserId()));
						payloadObj.put("sound","cenes-notification-ringtone.aiff");
						notifyObj.put("aps", payloadObj);
						
						List iosDeviceTokenList = new ArrayList();
						iosDeviceTokenList.add(userDevice.getDeviceToken());
						PushNotificationService.sendIosPushNotification(iosDeviceTokenList,notifyObj);
					}
					
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
	}
	
	public void sendReminderAlertPush(List<Reminder> reminders) {
		for (Reminder reminder : reminders) {
			JSONArray toAndroidArray = new JSONArray();
			List toIosArray = new ArrayList<>();
			
			String pushMessage = reminder.getTitle();
			
			List<ReminderMember> reminderMembers = reminder.getReminderMembers();
			for (ReminderMember reminderMember : reminderMembers) {
				if (reminderMember.getStatus() != null && reminderMember.getStatus().equals("Accept")) {
					
					List<UserDevice> userDevices = userService.findUserDeviceInfoByUserId(reminderMember.getMemberId());
					if (userDevices != null && userDevices.size() > 0) {
						for (UserDevice userDevice : userDevices) {
							if ("android".equals(userDevice.getDeviceType())){
								toAndroidArray.put(userDevice.getDeviceToken());
							} else if ("ios".equals(userDevice.getDeviceType())){
								toIosArray.add(userDevice);
							}
						}
					}
					
				}
			}
			
			try {
				if (toAndroidArray.length() > 0) {
					JSONObject notifyObj = new JSONObject();
					notifyObj.put("title", "Reminder");
					notifyObj.put("body", pushMessage);
					
					PushNotificationService.sendAndroidPush(toAndroidArray,notifyObj);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}

			try {
				if (toIosArray.size() > 0) {
					List<UserDevice> userDevices = toIosArray;
					for (UserDevice userDevice : userDevices) {
						JSONObject notifyObj = new JSONObject();
						JSONObject payloadObj = new JSONObject();
						JSONObject alert = new JSONObject();
						alert.put("title","Reminder : "+pushMessage);
						payloadObj.put("alert",alert);
						payloadObj.put("badge",getBadgeCountsByUserId(userDevice.getUserId()));
						payloadObj.put("sound","cenes-notification-ringtone.aiff");

						notifyObj.put("aps", payloadObj);
						
						List userDeviceTokenList = new ArrayList();
						userDeviceTokenList.add(userDevice.getDeviceToken());
						PushNotificationService.sendIosPushNotification(userDeviceTokenList,notifyObj);
					}
					
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void sendEventAlertPush(List<Event> events) {
		for (Event event : events) {
			JSONArray toAndroidArray = new JSONArray();
			List toIosArray = new ArrayList<>();
			
			String pushMessage = event.getTitle();
			
			List<EventMember> eventMembers = event.getEventMembers();
			for (EventMember eventMember : eventMembers) {
				System.out.println("Sending Event Alert to : "+eventMember.getEventMemberId()+", Name : "+eventMember.getName());
				if (eventMember.getStatus() != null && eventMember.getStatus().equals("Accept")) {
					
					List<UserDevice> userDevices = userService.findUserDeviceInfoByUserId(eventMember.getEventMemberId());
					if (userDevices != null && userDevices.size() > 0) {
						for (UserDevice userDevice : userDevices) {
							if ("android".equals(userDevice.getDeviceType())){
								toAndroidArray.put(userDevice.getDeviceToken());
							} else if ("ios".equals(userDevice.getDeviceType())){
								toIosArray.add(userDevice);
							}
						}
					}
					
				}
			}
			
			try {
				if (toAndroidArray.length() > 0) {
					JSONObject notifyObj = new JSONObject();
					notifyObj.put("title", "Gathering");
					notifyObj.put("body", pushMessage);
					
					PushNotificationService.sendAndroidPush(toAndroidArray,notifyObj);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}

			try {
				if (toIosArray.size() > 0) {
					List<UserDevice> userDevices = toIosArray;
					for (UserDevice userDevice : userDevices) {
						JSONObject notifyObj = new JSONObject();
						JSONObject payloadObj = new JSONObject();
						JSONObject alert = new JSONObject();
						alert.put("title","Gathering : "+pushMessage);
						payloadObj.put("alert",alert);
						payloadObj.put("badge",getBadgeCountsByUserId(userDevice.getUserId()));
						payloadObj.put("sound","cenes-notification-ringtone.aiff");

						notifyObj.put("aps", payloadObj);
						
						List userDeviceTokenList = new ArrayList();
						userDeviceTokenList.add(userDevice.getDeviceToken());
						PushNotificationService.sendIosPushNotification(userDeviceTokenList,notifyObj);
					}
					
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void sendPushForAcceptAndDeclineRequest(String pushMessage,Long organizerId,String receipentName,Notification.NotificationType type) {
		JSONArray toAndroidArray = new JSONArray();
		List toIosArray = new ArrayList<>();
		
		List<UserDevice> userDevices = userService.findUserDeviceInfoByUserId(organizerId);
		if (userDevices != null && userDevices.size() > 0) {
			for (UserDevice userDevice : userDevices) {
				if ("android".equals(userDevice.getDeviceType())){
					toAndroidArray.put(userDevice.getDeviceToken());
				} else if ("ios".equals(userDevice.getDeviceType())){
					toIosArray.add(userDevice);
				}
			}
		}
		
		try {
			if (toAndroidArray.length() > 0) {
				
				JSONObject payloadObj = new JSONObject();
				payloadObj.put(CgConstants.notificationTypeTitle,type.toString());
				payloadObj.put(CgConstants.notificationTypeStatus,"AcceptAndDecline");
				
				JSONObject notifyObj = new JSONObject();
				notifyObj.put("title", receipentName);
				notifyObj.put("body", pushMessage);
				notifyObj.put("payload", payloadObj);
				
				PushNotificationService.sendAndroidPush(toAndroidArray,notifyObj);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		try {
			if (toIosArray.size() > 0) {
				
				List<UserDevice> iosUserDevices = toIosArray;
				for (UserDevice userDevice : iosUserDevices) {
					JSONObject notifyObj = new JSONObject();
					JSONObject payloadObj = new JSONObject();
					JSONObject alert = new JSONObject();
					alert.put("title",pushMessage);
					payloadObj.put("alert",alert);
					payloadObj.put("badge",getBadgeCountsByUserId(userDevice.getUserId()));
					payloadObj.put("sound","cenes-notification-ringtone.aiff");

					notifyObj.put("aps", payloadObj);
					
					List deviceTokenList = new ArrayList();
					deviceTokenList.add(userDevice.getDeviceToken());
					
					PushNotificationService.sendIosPushNotification(deviceTokenList,notifyObj);
				}
				
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public int getBadgeCountsByUserId(Long userId) {
		int badgeCount = 0;
		NotificationCountData ncd = notificationCountDataRepository.findByUserId(userId);
		if (ncd == null) {
			ncd = new NotificationCountData();
			badgeCount = 1;
		} else {
			badgeCount = ncd.getBadgeCount() + 1;
		}
		ncd.setBadgeCount(badgeCount);
		ncd.setUserId(userId);
		notificationCountDataRepository.save(ncd);
		return badgeCount;
	}
	
	public void sendTestingNotificationToAndroid() {
		//ePRpH6WjCM0:APA91bF1hiSwM45o3-Zm2zfpCofWpBs3o8t7wKpeDBspNHOeFjwBn816W65phskOk4fsIvBgdRacuEPa9jPS7_SHSUKUBXrxKyxrmn7qt2tqgX8OyjVLfUmFdLxOIhsxp7rTvCIj4hTD
		JSONArray toArr = new JSONArray();
		try {
			toArr.put("d7riGwA6wjs:APA91bFubzo32JQxOj2xpmS7-PtW0Psfdh6D3adxtSwHYvA_rphI4AIyMjErY5SGHWpnQemihpt9XmG9WCD9nGJoiSfCqqAxe3iHAozStxjesCjIKtyH_mGp9gBRzdQa1AoWv5qkoJPs");
			//JSONObject payloadObj = new JSONObject();
			//payloadObj.put("notificationType","Gathering Update");
			
			JSONObject notifyObj = new JSONObject();
			notifyObj.put("title", "mandeep");
			notifyObj.put("body", " invited you to his event Party");
			//notifyObj.put("payload", payloadObj);
			PushNotificationService.sendAndroidPush(toArr,notifyObj);

		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void sendTestingNotificationToIos() {
	
		List devices = new ArrayList<>();
		devices.add("22bb6ecea9bd3f02ec7e371f72a17978c5ac995af98cb0a41f0e91f72470a397");
		
		JSONObject notifyObj = new JSONObject();
		
		try {
			JSONObject payloadObj = new JSONObject();
			JSONObject alert = new JSONObject();
			alert.put("title","Mandy called you to his event Welcome");
			//alert.put("content-available",1);
			//payloadObj.put("badge",1);
			payloadObj.put("alert",alert);
			notifyObj.put("aps", payloadObj);
			
			//{"aps":{"alert":{"title":"Hello from APNs Tester."},"notificationTypeId":"123","nType":"Reminder"}}
		} catch(Exception e) {
			e.printStackTrace();
		}
		PushNotificationService.sendIosPushNotification(devices,notifyObj);
	
	}
}
