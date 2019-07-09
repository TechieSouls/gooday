package com.cg.threads;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cg.dao.EventServiceDao;
import com.cg.events.bo.Event;
import com.cg.events.bo.GoogleEventItem;
import com.cg.manager.EventManager;
import com.cg.manager.EventTimeSlotManager;
import com.cg.service.UserService;

@Service
public class EventThread {

	@Autowired
	private EventManager eventManager;

	@Autowired
	private UserService userService;
	
	@Autowired
	private EventServiceDao eventServiceDao;
	
	@Autowired
	private EventTimeSlotManager eventTimeSlotManager;
	
	public void runEventThread(Long userId,Map<String, List<Event>> eventMap, Map<String, Object> phoneContacts) {
		
		Thread googleTh = new Thread(new GoogleSyncThread(userId));
		googleTh.start();
		
		Thread outlookTh = new Thread(new OutlookSyncThread(userId));
		outlookTh.start();
		
		/*if (eventMap != null && eventMap.size() > 0) {
			Thread deviceTh = new Thread(new DeviceSyncThread(eventMap));
			deviceTh.start();
		}*/
		
		Thread contactTh = new Thread(new ContactSyncThread(phoneContacts));
		contactTh.start();
	}
	
	public void runDeleteEventThread(List<Event> events) {
		System.out.println("[ Date : "+new Date()+" Delete Thread Run STARTS]");
		
		List<Event> eventsToAllocateToThread = new ArrayList<>();
		int trackElementsTraversed = 0;
		for (Event event : events) {
			eventsToAllocateToThread.add(event);
			trackElementsTraversed += 1;
			if (events.size() == trackElementsTraversed) {

				EventDeleteThread eventDeleteThread = new EventDeleteThread();
				eventDeleteThread.setEventsToDelete(eventsToAllocateToThread);
				eventDeleteThread.run();
				//Releasing Space allocated to List
				eventsToAllocateToThread = null;

				eventsToAllocateToThread = new ArrayList<>();
			} else {
				if (eventsToAllocateToThread.size() == 10) {
					EventDeleteThread eventDeleteThread = new EventDeleteThread();
					eventDeleteThread.setEventsToDelete(eventsToAllocateToThread);
					eventDeleteThread.run();
					//Releasing Space allocated to List
					eventsToAllocateToThread = null;
					
					eventsToAllocateToThread = new ArrayList<>();
				}
			}
		}
		System.out.println("[ Date : "+new Date()+" Delete Thread Run ENDS]");
	}
	

	public void runGoogleEventSyncThread(List<GoogleEventItem> events, Long userId, String timezone) {
		System.out.println("[ Date : "+new Date()+" Googel Event Sync Thread Run STARTS]");
		
		List<GoogleEventItem> eventsToAllocateToThread = new ArrayList<>();
		int trackElementsTraversed = 0;
		for (GoogleEventItem event : events) {
			eventsToAllocateToThread.add(event);
			trackElementsTraversed += 1;
			if (events.size() == trackElementsTraversed) {

				GoogleEventSyncThread googleEventSyncThread = new  GoogleEventSyncThread();
				googleEventSyncThread.setItems(eventsToAllocateToThread);
				googleEventSyncThread.setUserId(userId);
				googleEventSyncThread.setTimezone(timezone);
				googleEventSyncThread.run();
				//Releasing Space allocated to List
				eventsToAllocateToThread = null;

				eventsToAllocateToThread = new ArrayList<>();
			} else {
				if (eventsToAllocateToThread.size() == 5) {
					GoogleEventSyncThread googleEventSyncThread = new GoogleEventSyncThread();
					googleEventSyncThread.setItems(eventsToAllocateToThread);
					googleEventSyncThread.setUserId(userId);
					googleEventSyncThread.setTimezone(timezone);
					googleEventSyncThread.run();
					//Releasing Space allocated to List
					eventsToAllocateToThread = null;
					
					eventsToAllocateToThread = new ArrayList<>();
				}
			}
		}
		System.out.println("[ Date : "+new Date()+" Googel Event Sync Thread Run ENDS]");
	}
	
	public void runDeviceEventSyncThread(List<Event> events) {
		System.out.println("[ Date : "+new Date()+" Device Event Sync Thread Run STARTS]");
		
		List<Event> eventsToAllocateToThread = new ArrayList<>();
		int trackElementsTraversed = 0;
		for (Event event : events) {
			eventsToAllocateToThread.add(event);
			trackElementsTraversed += 1;
			if (events.size() == trackElementsTraversed) {

				DeviceEventSyncThread deviceEventSyncThread = new  DeviceEventSyncThread();
				deviceEventSyncThread.setEvents(events);
				deviceEventSyncThread.run();
				//Releasing Space allocated to List
				eventsToAllocateToThread = null;

				eventsToAllocateToThread = new ArrayList<>();
			} else {
				if (eventsToAllocateToThread.size() == 10) {
					DeviceEventSyncThread deviceEventSyncThread = new  DeviceEventSyncThread();
					deviceEventSyncThread.setEvents(events);
					deviceEventSyncThread.run();
					//Releasing Space allocated to List
					eventsToAllocateToThread = null;
					
					eventsToAllocateToThread = new ArrayList<>();
				}
			}
		}
		System.out.println("[ Date : "+new Date()+" Device Event Sync Thread Run ENDS]");
	}
	
	
	
	class GoogleSyncThread implements Runnable {

		private Long userId;

		public Long getUserId() {
			return userId;
		}

		public void setUserId(Long userId) {
			this.userId = userId;
		}

		GoogleSyncThread(Long userId) {
			this.userId = userId;
		}

		@Override
		public void run() {
			System.out.println("[Event Thread : GoogleSyncThread  STARTS]");
			eventManager.refreshGoogleEvents(userId);
			System.out.println("[Event Thread : GoogleSyncThread  STOPS]");
		}
	}

	class OutlookSyncThread implements Runnable {
		private Long userId;

		public Long getUserId() {
			return userId;
		}

		public void setUserId(Long userId) {
			this.userId = userId;
		}

		OutlookSyncThread(Long userId) {
			this.userId = userId;
		}
		@Override
		public void run() {
			System.out.println("[Event Thread : OutlookSyncThread  STARTS]");
			eventManager.refreshOutlookEvent(userId);
			System.out.println("[Event Thread : OutlookSyncThread  STOPS]");
		}
	}
	
	class DeviceSyncThread implements Runnable {

		private Map<String,List<Event>> eventMap;
		
		public Map<String, List<Event>> getEventMap() {
			return eventMap;
		}

		public void setEventMap(Map<String, List<Event>> eventMap) {
			this.eventMap = eventMap;
		}

		DeviceSyncThread(Map<String,List<Event>> eventMap) {
			this.eventMap = eventMap;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			System.out.println("[Event Thread : DeviceSyncThread  STARTS]");
			eventManager.syncDeviceCalendar(eventMap);
			System.out.println("[Event Thread : DeviceSyncThread  STOPS]");
		}
	}
	
	class ContactSyncThread implements Runnable {
		
		private Map<String, Object> phoneContacts;
		

		public Map<String, Object> getPhoneContacts() {
			return phoneContacts;
		}

		public void setPhoneContacts(Map<String, Object> phoneContacts) {
			this.phoneContacts = phoneContacts;
		}
		
		ContactSyncThread (Map<String, Object> phoneContacts) {
			this.phoneContacts = phoneContacts;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			System.out.println("[Event Thread : ContactSyncThread  STARTS]");
			userService.syncPhoneContacts(phoneContacts);
			System.out.println("[Event Thread : ContactSyncThread  STOPS]");
		}
	}
	
	class GoogleEventSyncThread implements Runnable {
				
		private List<GoogleEventItem> items;
		private Long userId;
		private String timezone;
		
		public List<GoogleEventItem> getItems() {
			return items;
		}

		public void setItems(List<GoogleEventItem> items) {
			this.items = items;
		}

		public Long getUserId() {
			return userId;
		}

		public void setUserId(Long userId) {
			this.userId = userId;
		}

		public String getTimezone() {
			return timezone;
		}

		public void setTimezone(String timezone) {
			this.timezone = timezone;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			for (GoogleEventItem event: items) {
				eventManager.saveGoogleEventIems(event, userId, timezone);
			}
		}
	}
	
	class DeviceEventSyncThread implements Runnable {
		
		private List<Event> events;
		
		public List<Event> getEvents() {
			return events;
		}
		public void setEvents(List<Event> events) {
			this.events = events;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			eventManager.saveEventsInBatch(events);
		}
	}
	
	class EventDeleteThread implements Runnable {
		
		private List<Event> eventsToDelete;
		
		public List<Event> getEventsToDelete() {
			return eventsToDelete;
		}

		public void setEventsToDelete(List<Event> eventsToDelete) {
			this.eventsToDelete = eventsToDelete;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			for (Event event: eventsToDelete) {
				System.out.println("Therad Deleting Time Slots Starts");
				eventServiceDao.deleteEventTimeSlotsByEventId(event.getEventId());
				System.out.println("Therad Deleting Time Slots ENDs");

			}
			
			eventManager.deleteEventBatch(eventsToDelete);
		}
	}
}
