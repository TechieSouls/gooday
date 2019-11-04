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
import com.cg.bo.Notification.NotificationTypeAction;
import com.cg.bo.Notification.NotificationTypeStatus;
import com.cg.bo.NotificationCountData;
import com.cg.constant.CgConstants;
import com.cg.dao.NotificationDao;
import com.cg.events.bo.Event;
import com.cg.events.bo.Event.EventUpdateFor;
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
	NotificationDao notificationDao;
	
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
	
	public void sendRemoveUserNotification(Event event, List<EventMember> eventMembers) {
		
			String pushMessage = "Host has removed you from the event "+event.getTitle()+"";

			for (EventMember eventMember : eventMembers) {
				
				if (eventMember.getUserId() == null) {
					continue;
				}
				JSONArray toAndroidArray = new JSONArray();
				List toIosArray = new ArrayList<>();
				
				List<UserDevice> userDevices = userService.findUserDeviceInfoByUserId(eventMember.getUserId());
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
						payloadObj.put(CgConstants.notificationType,Event.ScheduleEventAs.Gathering.toString());
						payloadObj.put(CgConstants.notificationTypeStatus,"AcceptAndDecline");
						
						JSONObject notifyObj = new JSONObject();
						notifyObj.put("title", "");
						notifyObj.put("body", pushMessage);
						notifyObj.put("payload", payloadObj);
						
						PushNotificationService.sendAndroidPush(toAndroidArray,notifyObj);
					}
				} catch(Exception e) {
					e.printStackTrace();
				}

				try {
					System.out.println("IOS Push to send : "+toIosArray.size());
					if (toIosArray.size() > 0) {
						
						List<UserDevice> iosUserDevices = toIosArray;
						for (UserDevice userDevice : iosUserDevices) {
							JSONObject notifyObj = new JSONObject();
							JSONObject payloadObj = new JSONObject();
							JSONObject alert = new JSONObject();
							alert.put("title",event.getTitle());
							alert.put("body",pushMessage);
							payloadObj.put("alert",alert);
							payloadObj.put("content-available",1);
							payloadObj.put("badge",getBadgeCountsByUserId(userDevice.getUserId()));
							payloadObj.put("sound","cenes-notification-ringtone.aiff");

							notifyObj.put("aps", payloadObj);
							
							List deviceTokenList = new ArrayList();
							deviceTokenList.add(userDevice.getDeviceToken());
							System.out.println("IOS Device token : "+userDevice.getDeviceToken());
							PushNotificationService.sendIosPushNotification(deviceTokenList,notifyObj);
						}
						
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		
	}
	
	public void sendDeleteNotification(Event event) {
				
		List<EventMember> eventMembers = event.getEventMembers();
		EventMember owner = null;
		for (EventMember ownerMember: event.getEventMembers()) {
			if (ownerMember.getUserId().equals(event.getCreatedById())) {
				owner = ownerMember;
				break;
			}
		}
		if (owner != null) {
			String pushMessage = "Event deleted by "+owner.getName();

			for (EventMember eventMember : eventMembers) {
				if (eventMember.getUserId() != null && !eventMember.getUserId().equals(event.getCreatedById())) {
					
					//Notification notification = notificationRepository.findByNotificationTypeIdAndRecepientIdAndAction(event.getEventId(),eventMember.getUserId(), NotificationTypeAction.Delete );
					//if (notification == null) {
					//	notification = new Notification();
					//}
					Notification notification = new Notification();
					notification.setSenderId(owner.getUserId());
					notification.setSender(owner.getName());
					notification.setNotificationTypeStatus(NotificationTypeStatus.New);
					notification.setMessage("Event deleted by "+owner.getName());
					notification.setTitle(event.getTitle());
					notification.setRecepientId(eventMember.getUserId());
					notification.setNotificationTypeId(event.getEventId());
					notification.setType(NotificationType.Gathering);
					notification.setAction(NotificationTypeAction.Delete);
					notification.setCreatedAt(new Date());
					notification.setUpdateAt(new Date());
					notificationRepository.save(notification);
					
					
					sendPushForAcceptAndDeclineRequest(event.getTitle(), pushMessage,eventMember.getUserId(),eventMember.getName(),Notification.NotificationType.Gathering);
				}
			}
		}
		
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
	
	//Welcome notificatoi
	public void sendWelcomeNotification(Event event) {
		User toUser = userService.findUserById(event.getCreatedById());
		if (event.getEventMembers() != null && event.getEventMembers().size() > 0) {
			System.out.println("[CreateEvent : "+new Date()+", Event Member Size : "+event.getEventMembers().size()+"]");
		}
		
		List<EventMember> eventMembers = event.getEventMembers();
		boolean notificationAlreadySent = false;
		
		String eventMessage = "";
		
		Notification notification = new Notification();
		notification.setSender("Cenes");
		
		eventMessage = "You got a message from CENES!";	
		notification.setNotificationTypeStatus(NotificationTypeStatus.New);
		
		notification.setMessage(eventMessage);
		notification.setTitle("Welcome To Cenes!");
		notification.setSenderId(toUser.getUserId());
		notification.setRecepientId(toUser.getUserId());
		notification.setNotificationTypeId(event.getEventId());
		notification.setType(NotificationType.Welcome);
		notification.setCreatedAt(new Date());
		notification.setUpdateAt(new Date());
		notificationRepository.save(notification);		
	}
	
	
	public void sendGatheringNotification(Event event) {
		User fromUser = userService.findUserById(event.getCreatedById());
		if (event.getEventMembers() != null && event.getEventMembers().size() > 0) {
			System.out.println("[CreateEvent : "+new Date()+", Event Member Size : "+event.getEventMembers().size()+"]");
		}
		
		Map<String,JSONArray> androidMap = new HashMap<>();
		Map<String,List> iOSMap = new HashMap<>();
		
		Map<Long, Integer> userIdBadgeCountMap = new HashMap<>();
		
		List<EventMember> eventMembers = event.getEventMembers();
		for (EventMember eventMember : eventMembers) {
			if (eventMember.getUserId() != null && !eventMember.getUserId().equals(event.getCreatedById())) {
				boolean notificationAlreadySent = false;
				
				String eventMessage = "";
				
				//Notification notification = notificationRepository.findByNotificationTypeIdAndRecepientIdAndAction(event.getEventId(),eventMember.getUserId(), NotificationTypeAction.Create);
				//if (notification == null) {
				//	notification = new Notification();
				//}
				Notification notification = new Notification();
				notification.setSenderId(fromUser.getUserId());
				notification.setSender(fromUser.getName());
				if (fromUser.getPhoto() != null) {
					notification.setSenderPicture(fromUser.getPhoto());
				}
				
				if (eventMember.isAlreadyInvited()) {
					
					if (EventUpdateFor.Title.equals(event.getUpdatedFor())) {
						eventMessage = fromUser.getName()+" added a new event name";
					} else if (EventUpdateFor.Image.equals(event.getUpdatedFor())) {
						eventMessage = fromUser.getName()+" added a new event image";
					} else if (EventUpdateFor.Time.equals(event.getUpdatedFor())) {
						eventMessage = "Time modified by "+fromUser.getName();
					} else if (EventUpdateFor.Description.equals(event.getUpdatedFor())) {
						eventMessage = "New message from "+fromUser.getName();
					} else if (EventUpdateFor.GuestList.equals(event.getUpdatedFor())) {
						eventMessage = "Guest list updated by "+fromUser.getName();
					} else if (EventUpdateFor.MultipleChanges.equals(event.getUpdatedFor())) {
						eventMessage = fromUser.getName()+" made changes";
					} else if (EventUpdateFor.Location.equals(event.getUpdatedFor())) {
						eventMessage = fromUser.getName()+" changed the location";
					}
					notification.setNotificationTypeStatus(NotificationTypeStatus.Old);
				} else {
					eventMessage = "Invitation from "+fromUser.getName();
				}
				
				notification.setMessage(eventMessage);
				notification.setTitle(event.getTitle());
				notification.setRecepientId(eventMember.getUserId());
				notification.setNotificationTypeId(event.getEventId());
				notification.setType(NotificationType.Gathering);
				notification.setCreatedAt(new Date());
				notification.setUpdateAt(new Date());
				notificationRepository.save(notification);
				
				userIdBadgeCountMap.put(eventMember.getUserId(), getBadgeCountsByUserId(eventMember.getUserId()));
				
				List<UserDevice> toUserDeviceInfo = userService.findUserDeviceInfoByUserId(eventMember.getUserId());
				if (toUserDeviceInfo != null && toUserDeviceInfo.size() > 0) {
					for (UserDevice userDevice : toUserDeviceInfo) {
						if ("android".equals(userDevice.getDeviceType())) {
							JSONArray toAndroidArray = new JSONArray();
							if (androidMap.containsKey(eventMessage)) {
								toAndroidArray = androidMap.get(eventMessage);
							}
							
 							toAndroidArray.put(userDevice.getDeviceToken());
 							androidMap.put(eventMessage, toAndroidArray);
						} else if ("ios".equals(userDevice.getDeviceType())) {
							
							List toIosArray = new ArrayList();
							if (iOSMap.containsKey(eventMessage)) {
								toIosArray = iOSMap.get(eventMessage);
							}
							toIosArray.add(userDevice);
							iOSMap.put(eventMessage, toIosArray);
						}
					}
				}
			}
		}
		
		try {
			for (Entry<String,JSONArray> androidSet : androidMap.entrySet()) {

				//String pushMessage = " sent you an invitation ";
				
				JSONObject payloadObj = new JSONObject();
				payloadObj.put(CgConstants.notificationTypeTitle,event.getTitle());
				payloadObj.put(CgConstants.notificationTypeId,event.getEventId());
				payloadObj.put(CgConstants.notificationType,NotificationType.Gathering.toString());
				/*if (androidSet.getKey().equals("old")) {
					payloadObj.put(CgConstants.notificationTypeStatus,"Old");
					pushMessage = " updated an invitation ";
				} else {
					payloadObj.put(CgConstants.notificationTypeStatus,"New");
				}*/
				
				JSONObject notifyObj = new JSONObject();
				notifyObj.put("title", event.getTitle());
				notifyObj.put("body", androidSet.getKey());
				notifyObj.put("payload", payloadObj);
				
				PushNotificationService.sendAndroidPush(androidSet.getValue(),notifyObj);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		try {
			for (Entry<String,List> iosSet : iOSMap.entrySet()) {
				
				for (UserDevice userDevice : (List<UserDevice>)iosSet.getValue()) {
					//String pushMessage = " sent you an invitation ";
					JSONObject notifyObj = new JSONObject();
					
					JSONObject payloadObj = new JSONObject();
					payloadObj.put(CgConstants.notificationTypeTitle,event.getTitle());
					payloadObj.put(CgConstants.notificationTypeId,event.getEventId());
					payloadObj.put(CgConstants.notificationType,NotificationType.Gathering.toString());
					/*if (iosSet.getKey().equals("old")) {
						payloadObj.put(CgConstants.notificationTypeStatus,"Old");
						pushMessage = " updated an invitation ";
					} else {
						payloadObj.put(CgConstants.notificationTypeStatus,"New");
					}*/
					
					JSONObject alert = new JSONObject();
					alert.put("title",event.getTitle());
					alert.put("body",iosSet.getKey());

					payloadObj.put("alert",alert);
					payloadObj.put("content-available",1);
					//payloadObj.put("badge",getBadgeCountsByUserId(userDevice.getUserId()));
					payloadObj.put("badge",userIdBadgeCountMap.get(userDevice.getUserId()));
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

		Event event = eventManager.findEventByEventId(eventMember.getEventId());
		
		Notification notification = null;
		if (eventMember.getStatus().equals(MemberStatus.Going.toString())) {
			//notification = notificationRepository.findByNotificationTypeIdAndRecepientIdAndAction(eventMember.getEventId(),eventMember.getUserId(), NotificationTypeAction.Accept);
			pushMessage = "[username] accepted your invitation [title]";
		} else if (eventMember.getStatus().equals(MemberStatus.NotGoing.toString())) {
			//notification = notificationRepository.findByNotificationTypeIdAndRecepientIdAndAction(eventMember.getEventId(),eventMember.getUserId(), NotificationTypeAction.Decline);
			pushMessage = "[username] declined your invitation [title]";
		}
		
		pushMessage = pushMessage.replace("[username]",eventMember.getName()).replace("[title]", event.getTitle());

		if (notification == null) {
			notification = new Notification();
		}
		
		notification.setSenderId(eventMember.getUserId());
		notification.setSender(eventMember.getName());
		
		notification.setNotificationTypeStatus(NotificationTypeStatus.New);
		
		if (eventMember.getStatus().equals(MemberStatus.Going.toString())) {
			notification.setMessage(eventMember.getName()+" accepted your invitation");
		} else if (eventMember.getStatus().equals(MemberStatus.NotGoing.toString())) {
			notification.setMessage(eventMember.getName()+" declined your invitation");
		}
		notification.setTitle(event.getTitle());
		notification.setRecepientId(event.getCreatedById());
		notification.setNotificationTypeId(event.getEventId());
		notification.setType(NotificationType.Gathering);
		notification.setAction(NotificationTypeAction.AcceptDecline);
		notification.setCreatedAt(new Date());
		notification.setUpdateAt(new Date());
		notificationRepository.save(notification);
		

		sendPushForAcceptAndDeclineRequest(event.getTitle(), pushMessage,event.getCreatedById(),eventMember.getName(),Notification.NotificationType.Gathering);
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
		sendPushForAcceptAndDeclineRequest(reminder.getTitle(), pushMessage,reminder.getCreatedById(),reminderMember.getName(),Notification.NotificationType.Reminder);
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
				if (eventMember.getStatus() != null && eventMember.getStatus().equals(EventMember.MemberStatus.Going.toString())) {
					System.out.println("Condition Passed");
					List<UserDevice> userDevices = userService.findUserDeviceInfoByUserId(eventMember.getUserId());
					System.out.println("Number of devices to send alert : "+userDevices.size());
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
					notifyObj.put("title", "Happening Now:");
					notifyObj.put("body", pushMessage);
					
					PushNotificationService.sendAndroidPush(toAndroidArray,notifyObj);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}

			try {
				System.out.println("IOS Push to send : "+toIosArray.size());
				if (toIosArray.size() > 0) {
					List<UserDevice> userDevices = toIosArray;
					for (UserDevice userDevice : userDevices) {
						JSONObject notifyObj = new JSONObject();
						JSONObject payloadObj = new JSONObject();
						JSONObject alert = new JSONObject();
						alert.put("title","Happening Now");
						alert.put("body",pushMessage);

						payloadObj.put("alert",alert);
						payloadObj.put("badge",getBadgeCountsByUserId(userDevice.getUserId()));
						payloadObj.put("sound","cenes-notification-ringtone.aiff");

						notifyObj.put("aps", payloadObj);
						
						List userDeviceTokenList = new ArrayList();
						userDeviceTokenList.add(userDevice.getDeviceToken());
						System.out.println("IOS Device token : "+userDevice.getDeviceToken());
						PushNotificationService.sendIosPushNotification(userDeviceTokenList,notifyObj);
					}
					
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void sendPushForAcceptAndDeclineRequest(String title, String pushMessage,Long organizerId,String receipentName,Notification.NotificationType type) {
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
				payloadObj.put(CgConstants.notificationType,type.toString());
				payloadObj.put(CgConstants.notificationTypeStatus,"AcceptAndDecline");
				
				JSONObject notifyObj = new JSONObject();
				notifyObj.put("title", "");
				notifyObj.put("body", pushMessage);
				notifyObj.put("payload", payloadObj);
				
				PushNotificationService.sendAndroidPush(toAndroidArray,notifyObj);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		try {
			System.out.println("IOS Push to send : "+toIosArray.size());
			if (toIosArray.size() > 0) {
				
				List<UserDevice> iosUserDevices = toIosArray;
				for (UserDevice userDevice : iosUserDevices) {
					JSONObject notifyObj = new JSONObject();
					JSONObject payloadObj = new JSONObject();
					JSONObject alert = new JSONObject();
					alert.put("title",title);
					alert.put("body",pushMessage);
					payloadObj.put("alert",alert);
					payloadObj.put("badge",getBadgeCountsByUserId(userDevice.getUserId()));
					payloadObj.put("sound","cenes-notification-ringtone.aiff");

					notifyObj.put("aps", payloadObj);
					
					List deviceTokenList = new ArrayList();
					deviceTokenList.add(userDevice.getDeviceToken());
					System.out.println("IOS Device token : "+userDevice.getDeviceToken());
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
	
	public List<Notification> findPageableNotificationsByRecepientId(Long recepientId, int pageNumber, int offset) {
		
		return notificationDao.findPageableNotificationsByUserId(recepientId, pageNumber, offset);
	}
	
	public int findNotificationsCountsByRecepientId(Long recepientId) {
		
		return notificationDao.findTotalNotificationCountsByRecepientId(recepientId);
	}
	
	public void sendRefreshPushNotification(Long userId) {
		List toIosArray = new ArrayList<>();

		List<UserDevice> userDevices = userService.findUserDeviceInfoByUserId(userId);
		if (userDevices != null && userDevices.size() > 0) {
			for (UserDevice userDevice : userDevices) {
				if ("ios".equals(userDevice.getDeviceType())){
					toIosArray.add(userDevice);
				}
			}
		}
		
		try {
			System.out.println("IOS Push to send : "+toIosArray.size());
			if (toIosArray.size() > 0) {
				
				List<UserDevice> iosUserDevices = toIosArray;
				for (UserDevice userDevice : iosUserDevices) {
					JSONObject notifyObj = new JSONObject();
					JSONObject payloadObj = new JSONObject();
					JSONObject alert = new JSONObject();
					//alert.put("title","Refresh Home Screen");
					payloadObj.put("content-available",1);
					payloadObj.put("type","HomeRefresh");
					notifyObj.put("aps", payloadObj);
					
					List deviceTokenList = new ArrayList();
					deviceTokenList.add(userDevice.getDeviceToken());
					System.out.println("IOS Device token : "+userDevice.getDeviceToken());
					PushNotificationService.sendIosPushNotification(deviceTokenList,notifyObj);
				}
				
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
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
