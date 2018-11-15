package com.cg.jobs;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.cg.events.bo.Event;
import com.cg.manager.EventManager;
import com.cg.manager.NotificationManager;
import com.cg.manager.ReminderManager;
import com.cg.reminders.bo.Reminder;

@Service
public class RemindersAlertJob {
	
	@Autowired
	ReminderManager reminderManager;
	
	@Autowired
	EventManager eventManager;
	
	@Autowired
	NotificationManager notificationManager;
	
	@Scheduled(cron="0 0/1 * * * *") //At every hour minutes	
	public void runRemindersAlertJob() {
		System.out.println("Date : "+new Date()+" Reminders Alert Job STARTS");
		List<Reminder> reminders = reminderManager.findRemindersToSendAlerts();
		if (reminders != null && reminders.size() > 0) {
			notificationManager.sendReminderAlertPush(reminders);
		}
		System.out.println("Date : "+new Date()+" Reminders Alert Job ENDS");
	}
	
	
	@Scheduled(cron="0 0/1 * * * *") //At every hour minutes	
	public void runEventsAlertJob() {
		System.out.println("Date : "+new Date()+" Events Alert Job STARTS");
		List<Event> events = eventManager.findEventsToSendAlerts();
		System.out.println("Events to be notified.");
		if (events != null && events.size() > 0) {
			notificationManager.sendEventAlertPush(events);
		}
		System.out.println("Date : "+new Date()+" Events Alert Job ENDS");
	}
}
