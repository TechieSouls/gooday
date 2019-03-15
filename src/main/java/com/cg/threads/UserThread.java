package com.cg.threads;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cg.events.bo.Event;
import com.cg.service.UserService;
import com.cg.user.bo.UserContact;

@Service
public class UserThread {
	
	class UserStatThread implements Runnable {
		
		private List<UserContact> userContacts;
		private List<Event> events;

		private UserService userService;
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (userContacts != null && userContacts.size() > 0) {
				userService.updateCenesMemberCountsByUserContacts(userContacts);
			} else if (events != null && events.size() > 0) {
				userService.updateEventHostedAndAttendedCounts(events);
			}
		}

		public List<UserContact> getUserContacts() {
			return userContacts;
		}

		public void setUserContacts(List<UserContact> userContacts) {
			this.userContacts = userContacts;
		}

		public UserService getUserService() {
			return userService;
		}

		public void setUserService(UserService userService) {
			this.userService = userService;
		}

		public List<Event> getEvents() {
			return events;
		}

		public void setEvents(List<Event> events) {
			this.events = events;
		}
	}

	public void runUpdateUserStatThreadByContacts(List<UserContact> userContacts, UserService userService) {
		UserStatThread userStatThread = new UserStatThread();
		userStatThread.setUserContacts(userContacts);
		userStatThread.setUserService(userService);
		userStatThread.run();
	}
	
	public void runUpdateUserStatThreadByEvents(List<Event> events, UserService userService) {
		UserStatThread userStatThread = new UserStatThread();
		userStatThread.setEvents(events);
		userStatThread.setUserService(userService);
		userStatThread.run();
	}
}