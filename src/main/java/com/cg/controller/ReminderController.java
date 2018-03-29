package com.cg.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.cg.constant.CgConstants.ErrorCodes;
import com.cg.manager.NotificationManager;
import com.cg.manager.ReminderManager;
import com.cg.reminders.bo.Reminder;
import com.cg.reminders.bo.ReminderMember;
import com.cg.service.UserService;
import com.cg.user.bo.User;

@Controller
public class ReminderController {

	@Autowired
	UserService userService;
	
	@Autowired
	NotificationManager notificationManager;
	
	@Autowired
	ReminderManager reminderManager;
	
	@RequestMapping(value="/api/reminder/save",method = RequestMethod.POST)
	public ResponseEntity<Map<String,Object>> createReminder(@RequestBody Reminder reminder) {

		Map<String,Object> response = new HashMap<>();
		try {
			Boolean ownerExists = false;
			if (reminder.getReminderMembers() != null) {
				for (ReminderMember  member : reminder.getReminderMembers()) {
					if (reminder.getCreatedById().equals(member.getMemberId())) {
						member.setStatus("Accept");
						ownerExists = true;
						break;
					}
				}
			}
			if (!ownerExists) {
				User user = userService.findUserById(reminder.getCreatedById());
		
				List<ReminderMember> members = null;
				if (reminder.getReminderMembers() != null) {
					members = reminder.getReminderMembers();
				} else {
					members = new ArrayList<>();
				}
				
				ReminderMember reminderMember = new ReminderMember();
				reminderMember.setName(user.getName());
				reminderMember.setPicture(user.getPhoto());
				reminderMember.setStatus("Accept");
				reminderMember.setMemberId(user.getUserId());
				members.add(reminderMember);
				reminder.setReminderMembers(members);
			}
			
			reminder = reminderManager.saveReminder(reminder);
			reminderManager.sendReminderNotification(reminder);			
			
			response.put("data",reminder);
			response.put("success",true);
			response.put("errorCode",0);
			response.put("errorDetail", null);
		} catch(Exception e) {
			e.printStackTrace();
			response.put("success",false);
			response.put("errorCode",ErrorCodes.InternalServerError.ordinal());
			response.put("errorDetail", ErrorCodes.InternalServerError.toString());
		}
		return new ResponseEntity<Map<String,Object>>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value="/api/reminder/list",method = RequestMethod.GET)
	public ResponseEntity<Map<String,Object>> userReminders(@RequestParam("user_id") Long userId) {
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			//List<Reminder> reminders = reminderRepository.findByCreatedByIdOrderByReminderTimeDesc(userId);
			List<Reminder> reminders = reminderManager.findAllUserRemindersByCreatedById(userId);
			if (reminders != null && reminders.size() > 0) {
				response.put("data",reminders);
			} else {
				response.put("data",new ArrayList<>());				
			}
			response.put("success",true);
			response.put("errorCode",0);
			response.put("errorDetail", null);
		} catch(Exception e) {
			e.printStackTrace();
			response.put("success",false);
			response.put("errorCode",ErrorCodes.InternalServerError.ordinal());
			response.put("errorDetail", ErrorCodes.InternalServerError.toString());
		}
		return new ResponseEntity<Map<String,Object>>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value="/api/reminder/updateToFinish",method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> updateReminderToFinish(
			@RequestParam("reminder_id") Long reminderId,
			@RequestParam(value = "user_id", required = false, defaultValue = "") String userIdStr) {
		Map<String,Object> response = new HashMap<>();
		
		try {
			Long userId = 0L;
			if (!"".equals(userIdStr)) {
				userId = Long.valueOf(userIdStr);
			}
			reminderManager.updateReminderToFinish(reminderId,userId);
			
			response.put("success",true);
			response.put("message","Reminder Finished");
			response.put("errorCode",0);
			response.put("errorDetail", null);
		} catch(Exception e) {
			e.printStackTrace();
			response.put("success",false);
			response.put("message","Reminder Not Finished");
			response.put("errorCode",ErrorCodes.InternalServerError.ordinal());
			response.put("errorDetail", ErrorCodes.InternalServerError.toString());
		}
		return new ResponseEntity<Map<String,Object>>(response, HttpStatus.OK);
	}

	@RequestMapping(value="/api/reminder/delete",method = RequestMethod.GET)
	public ResponseEntity<Map<String,Object>> deleteReminder(@RequestParam("reminderId") Long reminderId) {
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			reminderManager.deleteReminderByReminderId(reminderId);
			response.put("success",true);
			response.put("errorCode",0);
			response.put("errorDetail", null);
		} catch(Exception e) {
			e.printStackTrace();
			response.put("success",false);
			response.put("errorCode",ErrorCodes.InternalServerError.ordinal());
			response.put("errorDetail", ErrorCodes.InternalServerError.toString());
		}
		return new ResponseEntity<Map<String,Object>>(response, HttpStatus.OK);
	}

	@RequestMapping(value="/api/reminder/fetch",method = RequestMethod.GET)
	public ResponseEntity<Map<String,Object>> findReminderById(@RequestParam("reminderId") Long reminderId) {
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			Reminder reminder = reminderManager.findReminderByReminderId(reminderId);
			if (reminder != null) {
				response.put("data",reminder);
				response.put("success",true);
				response.put("errorCode",0);
				response.put("errorDetail", null);
			} else {
				response.put("data",null);
				response.put("success",false);
				response.put("errorCode",HttpStatus.NOT_FOUND.ordinal());
				response.put("errorDetail", HttpStatus.NOT_FOUND.toString());
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			response.put("success",false);
			response.put("errorCode",ErrorCodes.InternalServerError.ordinal());
			response.put("errorDetail", ErrorCodes.InternalServerError.toString());
		}
		return new ResponseEntity<Map<String,Object>>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value="/api/reminder/updateReminderMemberStatus", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> updateReminderMemberStatus(@RequestParam("reminderMemberId") Long reminderMemberId, @RequestParam("status") String status) {
		Map<String, Object> response = new HashMap<>();
		try {
			
			ReminderMember reminderMember = reminderManager.findReminderMemberByReminderMemberId(reminderMemberId);
			if (reminderMember != null) {
				notificationManager.deleteNotificationByRecepientIdNotificationTypeId(reminderMember.getMemberId(), reminderMember.getReminderId());
			}
			reminderMember.setStatus(status);
			reminderMember = reminderManager.saveReminderMember(reminderMember);
			//reminderManager.updateReminderMemberStatus(reminderMemberId, status);
			notificationManager.sendReminderAcceptDeclinedPush(reminderMember);
			
			response.put("success", true);
			response.put("errorCode", 0);
			response.put("errorDetail", null);
		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("errorCode", ErrorCodes.InternalServerError.ordinal());
			response.put("errorDetail", ErrorCodes.InternalServerError.toString());
		}
		return new ResponseEntity<Map<String,Object>>(response, HttpStatus.OK);
	}

}
