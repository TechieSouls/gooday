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
import com.cg.constant.CgConstants;
import com.cg.reminders.bo.Reminder;
import com.cg.reminders.bo.ReminderMember;
import com.cg.repository.ReminderMemberRepository;
import com.cg.repository.ReminderRepository;
import com.cg.service.PushNotificationService;
import com.cg.service.UserService;
import com.cg.user.bo.User;
import com.cg.user.bo.UserDevice;

@Service
public class ReminderManager {
	
	@Autowired
	ReminderRepository reminderRepository;
	
	@Autowired
	ReminderMemberRepository reminderMemberRepository;
	
	@Autowired
	UserService userService;
	
	@Autowired
	NotificationManager notificationManager;
	
	public void deleteRemindersBatch(List<Reminder> reminders) {
		reminderRepository.delete(reminders);
	}
	
	public void updateReminderToFinish(Long reminderId,Long userId) {
		
		reminderRepository.updateReminderToFinish(reminderId);

		if (!userId.equals(0L)) {
			Reminder reminder = reminderRepository.findOne(reminderId);
			
			User user = userService.findUserById(userId);
			
			notificationManager.sendReminderCompletedPush(reminder, user);
		}
	}
	
	public void deleteReminderByReminderId(Long reminderId) {
		reminderRepository.delete(reminderId);
	}
	
	public Reminder saveReminder(Reminder reminder) {
		return reminderRepository.save(reminder);
	}
	
	public Reminder findReminderByReminderId(Long reminderId) {
		return reminderRepository.findOne(reminderId);
	}
	
	public List<Reminder> findAllUserRemindersByCreatedById(Long userId) {
		return reminderRepository.findAllUserRemidners(userId);
	}
	
	public ReminderMember findReminderMemberByReminderMemberId(Long reminderMemberId) {
		 return reminderMemberRepository.findByReminderMemberId(reminderMemberId);
	}
	
	public ReminderMember saveReminderMember(ReminderMember reminderMember) {
		 return reminderMemberRepository.save(reminderMember);
	}
	
	public void updateReminderMemberStatus(Long reminderMemberId, String status) {
		reminderMemberRepository.updateStatus(reminderMemberId, status);
	}
	
	public List<Reminder> findRemindersToSendAlerts() {
		return reminderRepository.findAllRemindersWithTimeDifferenceEqualToOne();
	}
	
	public List<Reminder> findAllCompletedReminders() {
		return reminderRepository.findCompletedReminders();
	}
	
	public void sendReminderNotification(Reminder reminder) {
		User fromUser = userService.findUserById(reminder.getCreatedById());
		if (reminder.getReminderMembers() != null && reminder.getReminderMembers().size() > 0) {
			System.out.println("[CreateReminder : "+new Date()+", Reminder Member Size : "+reminder.getReminderMembers().size()+"]");

			List<ReminderMember> reminderMembers = reminder.getReminderMembers();
			Map<String,JSONArray> androidMap = new HashMap<>();
			Map<String,List> iOSMap = new HashMap<>();

			for (ReminderMember reminderMember : reminderMembers) {
				if (!reminderMember.getMemberId().equals(reminder.getCreatedById())) {
					boolean notificationAlreadySent = false;
					String reminderMessage = "sent you a reminder";
					
					Notification notification = notificationManager.findNotificationByNotificationTypeIdRecepientId(reminder.getReminderId(), reminderMember.getMemberId());
					if (notification == null) {
						notification = new Notification();
					}
					
					notification.setSenderId(fromUser.getUserId());
					notification.setSender(fromUser.getName());
					if (fromUser.getPhoto() != null) {
						notification.setSenderPicture(fromUser.getPhoto());
					}
					
					if (reminderMember.getStatus() != null) {
						reminderMessage = " updated a reminder ";
						notificationAlreadySent = true;
						notification.setNotificationTypeStatus(NotificationTypeStatus.Old);
					}
					
					notification.setMessage(reminderMessage);
					notification.setTitle(reminder.getTitle());
					notification.setRecepientId(reminderMember.getMemberId());
					notification.setNotificationTypeId(reminder.getReminderId());
					notification.setType(NotificationType.Reminder);
					notification.setCreatedAt(new Date());
					notification.setUpdateAt(new Date());

					if (!notificationAlreadySent) {
						notificationManager.saveNotification(notification);
					}
					
					List<UserDevice> toUserDeviceInfo = userService.findUserDeviceInfoByUserId(reminderMember.getMemberId());
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

					String pushMessage = " sent you a reminder ";
					
					JSONObject payloadObj = new JSONObject();
					payloadObj.put(CgConstants.notificationTypeTitle,reminder.getTitle());
					payloadObj.put(CgConstants.notificationTypeId,reminder.getReminderId());
					//payloadObj.put("notificationLocation",reminder.getLocation());
					//payloadObj.put("notificationTime",reminder.getReminderTime());
					payloadObj.put(CgConstants.notificationType,NotificationType.Reminder.toString());
					if (androidSet.getKey().equals("old")) {
						payloadObj.put(CgConstants.notificationTypeStatus,"Old");
						pushMessage = " updated a reminder ";
					} else {
						payloadObj.put(CgConstants.notificationTypeStatus,"New");
					}
					
					JSONObject notifyObj = new JSONObject();
					notifyObj.put("title", fromUser.getName());
					notifyObj.put("body", pushMessage+reminder.getTitle());
					notifyObj.put("payload", payloadObj);
					
					PushNotificationService.sendAndroidPush(androidSet.getValue(),notifyObj);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}

			try {
				for (Entry<String,List> iosSet : iOSMap.entrySet()) {
					
					List<UserDevice> userDevices = iosSet.getValue();
					for (UserDevice userDevice : userDevices) {
						String pushMessage = " sent you a reminder ";
						JSONObject notifyObj = new JSONObject();
						
						JSONObject payloadObj = new JSONObject();
						payloadObj.put(CgConstants.notificationTypeTitle,reminder.getTitle());
						payloadObj.put(CgConstants.notificationTypeId,reminder.getReminderId());
						payloadObj.put(CgConstants.notificationType,NotificationType.Reminder.toString());
						if (iosSet.getKey().equals("old")) {
							payloadObj.put(CgConstants.notificationTypeStatus,"Old");
							pushMessage = " updated a reminder ";
						} else {
							payloadObj.put(CgConstants.notificationTypeStatus,"New");
						}
						
						JSONObject alert = new JSONObject();
						alert.put("title",fromUser.getName()+pushMessage+reminder.getTitle());
						payloadObj.put("alert",alert);
						payloadObj.put("badge",notificationManager.getBadgeCountsByUserId(userDevice.getUserId()));
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
		
	}
}
