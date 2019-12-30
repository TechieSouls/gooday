package com.cg.threads;

import com.cg.manager.EventManager;
import com.cg.manager.EventTimeSlotManager;
import com.cg.manager.RecurringManager;

public class RecurringEventThread implements Runnable {

	private EventManager eventManager;
	private EventTimeSlotManager eventTimeSlotManager;
	private RecurringManager recurringManager;
	private Long recurringEventId;

	public EventManager getEventManager() {
		return eventManager;
	}

	public void setEventManager(EventManager eventManager) {
		this.eventManager = eventManager;
	}

	public EventTimeSlotManager getEventTimeSlotManager() {
		return eventTimeSlotManager;
	}

	public void setEventTimeSlotManager(EventTimeSlotManager eventTimeSlotManager) {
		this.eventTimeSlotManager = eventTimeSlotManager;
	}

	public RecurringManager getRecurringManager() {
		return recurringManager;
	}

	public void setRecurringManager(RecurringManager recurringManager) {
		this.recurringManager = recurringManager;
	}

	public Long getRecurringEventId() {
		return recurringEventId;
	}

	public void setRecurringEventId(Long recurringEventId) {
		this.recurringEventId = recurringEventId;
	}

	class DeleteEventThread implements Runnable {

		private EventManager eventManager;
		private Long recurringEventId;
		
		public EventManager getEventManager() {
			return eventManager;
		}
		public void setEventManager(EventManager eventManager) {
			this.eventManager = eventManager;
		}
		public Long getRecurringEventId() {
			return recurringEventId;
		}
		public void setRecurringEventId(Long recurringEventId) {
			this.recurringEventId = recurringEventId;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				eventManager.deleteEventsByRecurringEventId(recurringEventId);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}
	
	class DeleteEventTimeSlotThread implements Runnable {

		private EventTimeSlotManager eventTimeSlotManager;
		private Long recurringEventId;
		
		public EventTimeSlotManager getEventTimeSlotManager() {
			return eventTimeSlotManager;
		}
		public void setEventTimeSlotManager(EventTimeSlotManager eventTimeSlotManager) {
			this.eventTimeSlotManager = eventTimeSlotManager;
		}
		public Long getRecurringEventId() {
			return recurringEventId;
		}
		public void setRecurringEventId(Long recurringEventId) {
			this.recurringEventId = recurringEventId;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				eventTimeSlotManager.deleteEventTimeSlotsByRecurringEventId(recurringEventId);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}
	
	class DeleteRecurringTablesThread implements Runnable {

		private RecurringManager recurringManager;
		private Long recurringEventId;
		
		public RecurringManager getRecurringManager() {
			return recurringManager;
		}
		public void setRecurringManager(RecurringManager recurringManager) {
			this.recurringManager = recurringManager;
		}
		public Long getRecurringEventId() {
			return recurringEventId;
		}
		public void setRecurringEventId(Long recurringEventId) {
			this.recurringEventId = recurringEventId;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				recurringManager.deleteRecurringPatternsByRecurringEventId(recurringEventId);
				recurringManager.deleteRecurringEventByRecurringEventId(recurringEventId);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void runDeleteEventsThread(EventManager eventManager, Long recurringEventId) {
		DeleteEventThread deleteEventThread = new DeleteEventThread();
		deleteEventThread.setEventManager(eventManager);
		deleteEventThread.setRecurringEventId(recurringEventId);
		deleteEventThread.run();
	}
	
	public void runDeleteEventTimeSlotsThread(EventTimeSlotManager eventTimeSlotManager, Long recurringEventId) {
		DeleteEventTimeSlotThread deleteEventTimeSlotThread = new DeleteEventTimeSlotThread();
		deleteEventTimeSlotThread.setEventTimeSlotManager(eventTimeSlotManager);
		deleteEventTimeSlotThread.setRecurringEventId(recurringEventId);
		deleteEventTimeSlotThread.run();
	}
	
	public void runDeleteRecurringEventsThread(RecurringManager recurringManager, Long recurringEventId) {
		DeleteRecurringTablesThread deleteRecurringTablesThread = new DeleteRecurringTablesThread();
		deleteRecurringTablesThread.setRecurringManager(recurringManager);
		deleteRecurringTablesThread.setRecurringEventId(recurringEventId);
		deleteRecurringTablesThread.run();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		eventManager.deleteEventsByRecurringEventId(recurringEventId);
		eventTimeSlotManager.deleteEventTimeSlotsByRecurringEventId(recurringEventId);
		recurringManager.deleteRecurringPatternsByRecurringEventId(recurringEventId);
		recurringManager.deleteRecurringEventByRecurringEventId(recurringEventId);
		//runDeleteEventsThread(eventManager, recurringEventId);
		//runDeleteEventTimeSlotsThread(eventTimeSlotManager, recurringEventId);
		//runDeleteRecurringEventsThread(recurringManager, recurringEventId);
	}


}
