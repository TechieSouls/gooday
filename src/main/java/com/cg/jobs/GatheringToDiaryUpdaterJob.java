package com.cg.jobs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.cg.bo.Diary;
import com.cg.bo.Member;
import com.cg.bo.Diary.DiarySource;
import com.cg.bo.Member.MemberType;
import com.cg.events.bo.Event;
import com.cg.events.bo.EventMember;
import com.cg.events.controller.EventController;
import com.cg.manager.NotificationManager;
import com.cg.manager.ReminderManager;
import com.cg.reminders.bo.Reminder;
import com.cg.reminders.bo.ReminderMember;
import com.cg.repository.DiaryRepository;
import com.cg.service.EventService;
import com.cg.service.UserService;

@Service
public class GatheringToDiaryUpdaterJob {

	@Autowired
	private DiaryRepository diaryRepository;
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private UserService userService;
	
	@Autowired 
	private EventController eventController;
	
	@Autowired
	private NotificationManager notificationManager;
	
	@Autowired
	private ReminderManager reminderManager;
	
	@Scheduled(cron="0 0/60 * * * *") //At every hour minutes	
	public void runUpdateGatheringToDiary() {
		System.out.println("[Date : "+new Date()+", GatheringToDiaryUpdaterJob STARTS]");
		
		moveGatheringsToDiary();
		
		//moveRemindersToDiary();
		
		System.out.println("[Date : "+new Date()+", Gathering To Diary Updater Job ENDS]");
	}
	
	public void moveGatheringsToDiary() {
		List<Event> events = eventService.findUserPastGatherings();
		System.out.println("[Date : "+new Date()+", GatheringToDiaryUpdaterJob events to update : "+events.size()+"]");
		if (events != null && events.size() > 0) {
			List<Diary>  diaries = new ArrayList<>();
			for (Event gathering : events) {
				
				Diary diary = new Diary();
				diary.setTitle(gathering.getTitle());
				diary.setCreatedById(gathering.getCreatedById());
				diary.setDetail(gathering.getDescription());
				diary.setLocation(gathering.getLocation());
				diary.setPictures(gathering.getEventPicture());
				diary.setDiaryTime(gathering.getStartTime());
				diary.setCreatedAt(gathering.getStartTime());
				diary.setSource(DiarySource.Gathering.toString());
				
				if (gathering.getEventMembers() != null && gathering.getEventMembers().size() > 0) {
					List<Member> members = new ArrayList<>();
					for (EventMember eventMember : gathering.getEventMembers()) {
						Member member = new Member();
						member.setName(eventMember.getName());
						member.setUserId(eventMember.getUserId());
						member.setStatus(eventMember.getStatus());
						member.setType(MemberType.Diary.toString());
						member.setPicture(eventMember.getPicture());
						members.add(member);
					}
					diary.setMembers(members);
				}
				diaries.add(diary);
				notificationManager.deleteNotificationByNotificationTypeId(gathering.getEventId());
			}
			System.out.println("Diaries Size : "+diaries.size());
			diaryRepository.save(diaries);
			eventService.deleteEventsBatch(events);
		}
	}
	
	public void moveRemindersToDiary() {
		List<Reminder> reminders = reminderManager.findAllCompletedReminders();
		if (reminders != null && reminders.size() > 0) {
			List<Diary>  diaries = new ArrayList<>();
			for (Reminder reminder : reminders) {
				Diary diary = new Diary();
				diary.setTitle(reminder.getTitle());
				diary.setCreatedById(reminder.getCreatedById());
				diary.setLocation(reminder.getLocation());
				if (reminder.getReminderTime() != null) {
					diary.setDiaryTime(reminder.getReminderTime());
				}
				diary.setCreatedAt(reminder.getCreatedAt());
				diary.setSource(DiarySource.Reminder.toString());
				
				if (reminder.getReminderMembers() != null && reminder.getReminderMembers().size() > 0) {
					List<Member> members = new ArrayList<>();
					for (ReminderMember reminderMember : reminder.getReminderMembers()) {
						Member member = new Member();
						member.setName(reminderMember.getName());
						member.setUserId(reminderMember.getMemberId());
						member.setStatus(reminderMember.getStatus());
						member.setType(MemberType.Diary.toString());
						member.setPicture(reminderMember.getPicture());
						members.add(member);
					}
					diary.setMembers(members);
				}
				diaries.add(diary);
				notificationManager.deleteNotificationByNotificationTypeId(reminder.getReminderId());
			}
			System.out.println("Diaries Size : "+diaries.size());
			diaryRepository.save(diaries);
			reminderManager.deleteRemindersBatch(reminders);
		}
	}
}
