package com.cg.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cg.bo.Notification;
import com.cg.constant.CgConstants.ErrorCodes;
import com.cg.repository.NotificationRepository;

@RestController
public class NotificationController {
	
	@Autowired
	NotificationRepository notificationRepository;
	
	@RequestMapping(value="/api/notification/byuser", method=RequestMethod.GET)
	public ResponseEntity<Map<String,Object>> getUserNotifications(@RequestParam("userId") Long userId) {
	
		Map<String,Object> responseMap = new HashMap<>();
		responseMap.put("errorCode", 0);
		responseMap.put("errorDetail", null);
		responseMap.put("success", true);
		try {
			List<Notification> notificatoins = notificationRepository.findByRecepientIdOrderByCreatedAtDesc(userId);
			
			if (notificatoins != null && notificatoins.size() > 0) {
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
}
