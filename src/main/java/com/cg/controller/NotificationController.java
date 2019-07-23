package com.cg.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cg.bo.Notification;
import com.cg.bo.Notification.NotificationReadStatus;
import com.cg.bo.NotificationCountData;
import com.cg.constant.CgConstants.ErrorCodes;
import com.cg.manager.NotificationManager;
import com.cg.repository.NotificationCountDataRepository;
import com.cg.repository.NotificationRepository;
import com.cg.repository.UserRepository;
import com.cg.user.bo.User;

@RestController
public class NotificationController {
	
	@Autowired
	NotificationRepository notificationRepository;
	
	@Autowired
	NotificationCountDataRepository notificationCountDataRepository;

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	NotificationManager notificationManager;
	
	@RequestMapping(value="/api/notification/byuser", method=RequestMethod.GET)
	public ResponseEntity<Map<String,Object>> getUserNotifications(@RequestParam("userId") Long userId) {
	
		Map<String,Object> responseMap = new HashMap<>();
		responseMap.put("errorCode", 0);
		responseMap.put("errorDetail", null);
		responseMap.put("success", true);
		try {
			List<Notification> notificatoins = notificationRepository.findByRecepientIdOrderByCreatedAtDesc(userId);
			
			if (notificatoins != null && notificatoins.size() > 0) {
				
				for (Notification notification: notificatoins) {
					User user = userRepository.findOne(notification.getSenderId());
					notification.setUser(user);
				}
				
				//Update Badge Counts to 0
				notificationCountDataRepository.setBadgeCountsToZero(userId);
				
				responseMap.put("data", notificatoins);
			} else {
				responseMap.put("data", new ArrayList<>());
			}
			return new ResponseEntity<Map<String,Object>>(responseMap,HttpStatus.OK);
		} catch(Exception e) {
			e.printStackTrace();
		}
		responseMap.put("errorCode", ErrorCodes.InternalServerError.ordinal());
		responseMap.put("errorDetail", ErrorCodes.InternalServerError.toString());
		responseMap.put("success", false);
		return new ResponseEntity<Map<String,Object>>(responseMap,HttpStatus.OK);
	}
	
	@RequestMapping(value="/api/notification/byuserpageable", method=RequestMethod.GET)
	public ResponseEntity<Map<String,Object>> getPageableNotificationByUserId(Long userId, int pageNumber, int offset) {
	
		Map<String,Object> responseMap = new HashMap<>();
		responseMap.put("errorCode", 0);
		responseMap.put("errorDetail", null);
		responseMap.put("success", true);
		try {
			List<Notification> notificatoins = notificationManager.findPageableNotificationsByRecepientId(userId, pageNumber, offset);
			
			if (notificatoins != null && notificatoins.size() > 0) {
				
				/*for (Notification notification: notificatoins) {
					User user = userRepository.findOne(notification.getSenderId());
					notification.setUser(user);
				}*/
				
				//Update Badge Counts to 0
				notificationCountDataRepository.setBadgeCountsToZero(userId);
				
				responseMap.put("data", notificatoins);
			} else {
				responseMap.put("data", new ArrayList<>());
			}
			return new ResponseEntity<Map<String,Object>>(responseMap,HttpStatus.OK);
		} catch(Exception e) {
			e.printStackTrace();
		}
		responseMap.put("errorCode", ErrorCodes.InternalServerError.ordinal());
		responseMap.put("errorDetail", ErrorCodes.InternalServerError.toString());
		responseMap.put("success", false);
		responseMap.put("message", ErrorCodes.InternalServerError.toString());
		return new ResponseEntity<Map<String,Object>>(responseMap,HttpStatus.OK);
	}
	
	
	
	@RequestMapping(value="/api/notification/unreadbyuser", method=RequestMethod.GET)
	public ResponseEntity<Map<String,Object>> getUserUnReadNotifications(@RequestParam("userId") Long userId) {
	
		Map<String,Object> responseMap = new HashMap<>();
		responseMap.put("errorCode", 0);
		responseMap.put("errorDetail", null);
		responseMap.put("success", true);
		try {
			List<Notification> notificatoins = notificationRepository.findByRecepientIdAndReadStatusOrderByCreatedAtDesc(userId, NotificationReadStatus.UnRead);
			
			if (notificatoins != null && notificatoins.size() > 0) {
				responseMap.put("data", notificatoins.size());
			} else {
				responseMap.put("data", 0);
			}
			return new ResponseEntity<Map<String,Object>>(responseMap,HttpStatus.OK);
		} catch(Exception e) {
			e.printStackTrace();
		}
		responseMap.put("errorCode", ErrorCodes.InternalServerError.ordinal());
		responseMap.put("errorDetail", ErrorCodes.InternalServerError.toString());
		responseMap.put("success", false);
		return new ResponseEntity<Map<String,Object>>(responseMap,HttpStatus.OK);
	}
	
	
	@RequestMapping(value="/api/notification/markReadByUserIdAndNotifyId",  produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String,Object>> getUserNotifications(Long userId,Long notificationTypeId) {
	
		Map<String,Object> responseMap = new HashMap<>();
		responseMap.put("errorCode", 0);
		responseMap.put("errorDetail", null);
		responseMap.put("success", true);
		try {
			if (notificationTypeId == null) {
				notificationRepository.updateReadStatusByreceipientId(NotificationReadStatus.Read, userId);
			} else {
				notificationRepository.updateReadStatusByreceipientIdAndNotificationTypeId(NotificationReadStatus.Read,userId,notificationTypeId);
			}
			return new ResponseEntity<Map<String,Object>>(responseMap,HttpStatus.OK);
		} catch(Exception e) {
			e.printStackTrace();
		}
		responseMap.put("errorCode", ErrorCodes.InternalServerError.ordinal());
		responseMap.put("errorDetail", ErrorCodes.InternalServerError.toString());
		responseMap.put("success", false);
		return new ResponseEntity<Map<String,Object>>(responseMap,HttpStatus.OK);
	}
	
	
	@RequestMapping(value="/api/notification/counts",  produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String,Object>> findTotalNotificationCounts(Long recepientId) {
	
		Map<String,Object> responseMap = new HashMap<>();
		responseMap.put("errorCode", 0);
		responseMap.put("errorDetail", null);
		responseMap.put("success", true);
		try {
			
			int counts = notificationManager.findNotificationsCountsByRecepientId(recepientId);
			responseMap.put("data", counts);
			return new ResponseEntity<Map<String,Object>>(responseMap,HttpStatus.OK);
		} catch(Exception e) {
			e.printStackTrace();
		}
		responseMap.put("errorCode", ErrorCodes.InternalServerError.ordinal());
		responseMap.put("errorDetail", ErrorCodes.InternalServerError.toString());
		responseMap.put("success", false);
		responseMap.put("message", ErrorCodes.InternalServerError.toString());
		return new ResponseEntity<Map<String,Object>>(responseMap,HttpStatus.OK);
	}
	
	
	@RequestMapping(value="/api/notification/markReadByNotificationId",  produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String,Object>> markNotificationReadByNotificationId(Long notificationId) {
	
		Map<String,Object> responseMap = new HashMap<>();
		responseMap.put("errorCode", 0);
		responseMap.put("errorDetail", null);
		responseMap.put("success", true);
		try {
			notificationRepository.updateReadStatusByNotificationId(NotificationReadStatus.Read,notificationId);
			return new ResponseEntity<Map<String,Object>>(responseMap,HttpStatus.OK);
		} catch(Exception e) {
			e.printStackTrace();
		}
		responseMap.put("errorCode", ErrorCodes.InternalServerError.ordinal());
		responseMap.put("errorDetail", ErrorCodes.InternalServerError.toString());
		responseMap.put("success", false);
		responseMap.put("message", ErrorCodes.InternalServerError.toString());
		return new ResponseEntity<Map<String,Object>>(responseMap,HttpStatus.OK);
	}

	@RequestMapping(value="/api/notification/setBadgeCountsToZero",  produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String,Object>> setBadgeCountsToZero(Long userId) {
		Map<String,Object> responseMap = new HashMap<>();
		responseMap.put("errorCode", 0);
		responseMap.put("errorDetail", null);
		responseMap.put("success", true);
		try {
			notificationCountDataRepository.setBadgeCountsToZero(userId);
			return new ResponseEntity<Map<String,Object>>(responseMap,HttpStatus.OK);
		} catch(Exception e) {
			e.printStackTrace();
		}
		responseMap.put("errorCode", ErrorCodes.InternalServerError.ordinal());
		responseMap.put("errorDetail", ErrorCodes.InternalServerError.toString());
		responseMap.put("success", false);
		return new ResponseEntity<Map<String,Object>>(responseMap,HttpStatus.OK);

	}
	
	@RequestMapping(value="/api/notification/getBadgeCounts",  produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String,Object>> getBadgeCounts(Long userId) {
		Map<String,Object> responseMap = new HashMap<>();
		responseMap.put("errorCode", 0);
		responseMap.put("errorDetail", null);
		responseMap.put("success", true);
		try {
			NotificationCountData notificationCountData = notificationCountDataRepository.findByUserId(userId);
			if (notificationCountData == null) {
				responseMap.put("success", false);
			} else {
				responseMap.put("data", notificationCountData);
			}
			return new ResponseEntity<Map<String,Object>>(responseMap,HttpStatus.OK);
		} catch(Exception e) {
			e.printStackTrace();
		}
		responseMap.put("errorCode", ErrorCodes.InternalServerError.ordinal());
		responseMap.put("errorDetail", ErrorCodes.InternalServerError.toString());
		responseMap.put("success", false);
		return new ResponseEntity<Map<String,Object>>(responseMap,HttpStatus.OK);

	}
}
