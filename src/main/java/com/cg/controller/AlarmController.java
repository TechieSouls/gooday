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

import com.cg.bo.Alarm;
import com.cg.constant.CgConstants.ErrorCodes;
import com.cg.repository.AlarmRepository;

@Controller
public class AlarmController {
	
	@Autowired
	AlarmRepository alarmRepository;
	
	@RequestMapping(value="/api/alarm/save", method=RequestMethod.POST)
	public ResponseEntity<Map<String,Object>> saveAlarm(@RequestBody Alarm alarm) {
		Map<String,Object> responseMap = new HashMap<>();
		try {
			
			alarm = alarmRepository.save(alarm);
			responseMap.put("errorCode", 0);
			responseMap.put("errorDetail", null);
			responseMap.put("data", alarm);
			responseMap.put("success", true);
			
		} catch(Exception e) {
			e.printStackTrace();
			responseMap.put("errorCode", ErrorCodes.InternalServerError.ordinal());
			responseMap.put("errorDetail", ErrorCodes.InternalServerError.toString());
			responseMap.put("success", false);
		}
		return new ResponseEntity<Map<String,Object>>(responseMap,HttpStatus.OK);
	}
	
	@RequestMapping(value="/api/alarm/list", method=RequestMethod.POST)
	public ResponseEntity<Map<String,Object>> findAlarmByUser(@RequestParam("user_id") Long userId) {
		Map<String,Object> responseMap = new HashMap<>();
		try {
			
			List<Alarm> alarms = alarmRepository.findByUserId(userId);
			if (alarms == null || alarms.size() == 0) {
				alarms = new ArrayList<>();
			}
				
			responseMap.put("errorCode", 0);
			responseMap.put("errorDetail", null);
			responseMap.put("data", alarms);
			responseMap.put("success", true);
			
		} catch(Exception e) {
			e.printStackTrace();
			responseMap.put("errorCode", ErrorCodes.InternalServerError.ordinal());
			responseMap.put("errorDetail", ErrorCodes.InternalServerError.toString());
			responseMap.put("data", new ArrayList<>());
			responseMap.put("success", false);
		}
		return new ResponseEntity<Map<String,Object>>(responseMap,HttpStatus.OK);
	}
	
	@RequestMapping(value="/api/alarm/delete", method=RequestMethod.GET)
	public ResponseEntity<Map<String,Object>> deleteAlarmById(@RequestParam("alarm_id") Long alarmId) {
		Map<String,Object> responseMap = new HashMap<>();
		try {
			
			alarmRepository.delete(alarmId);
			responseMap.put("errorCode", 0);
			responseMap.put("errorDetail", null);
			responseMap.put("message", "Alarm Deleted Successfully");
			responseMap.put("success", true);
			
		} catch(Exception e) {
			e.printStackTrace();
			responseMap.put("errorCode", ErrorCodes.InternalServerError.ordinal());
			responseMap.put("errorDetail", ErrorCodes.InternalServerError.toString());
			responseMap.put("message", "Error in deleting Alarm");
			responseMap.put("success", false);
		}
		return new ResponseEntity<Map<String,Object>>(responseMap,HttpStatus.OK);
	}
}
