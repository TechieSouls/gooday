package com.cg.jobs;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cg.bo.CalendarSyncToken;
import com.cg.manager.EventManager;

@Service
public class CalendarSyncJob {
	
	@Autowired
	EventManager eventManager;
	
	public void syncGoogleCalendar() {
		System.out.println("[Google Calendar Sync Job STARTS]");
		List<CalendarSyncToken> refreshTokens = eventManager.getAllGoogleSyncTokens();
		if (refreshTokens != null && refreshTokens.size() > 0) {
			int numberOfThreadsToGenerate = refreshTokens.size() / 10;
			int numberOfThreadsLeftAsRemainder = refreshTokens.size() % 10;
			
			for (int number=0 ; number < numberOfThreadsToGenerate; number++) {
				List<CalendarSyncToken> refTokens = refreshTokens.subList(number, (number+1)*10);
				RefershTokenThread refreshTokenThread = new RefershTokenThread();
				refreshTokenThread.setRefreshTokens(refTokens);
				refreshTokenThread.run();
			}
			
			List<CalendarSyncToken> refTokens = refreshTokens.subList(refreshTokens.size()-numberOfThreadsLeftAsRemainder, refreshTokens.size());
			RefershTokenThread refreshTokenThread = new RefershTokenThread();
			refreshTokenThread.setRefreshTokens(refTokens);
			refreshTokenThread.run();
		}
		System.out.println("[Google Calendar Sync Job ENDS]");

	}
	
	class RefershTokenThread implements Runnable {
		
		private List<CalendarSyncToken> refreshTokens;
		
		private EventManager eventManager;
		
		public List<CalendarSyncToken> getRefreshTokens() {
			return refreshTokens;
		}

		public void setRefreshTokens(List<CalendarSyncToken> refreshTokens) {
			this.refreshTokens = refreshTokens;
		}

		public EventManager getEventManager() {
			return eventManager;
		}

		public void setEventManager(EventManager eventManager) {
			this.eventManager = eventManager;
		}

		@Override
		public void run() {
			for (CalendarSyncToken refreshToken : refreshTokens) {
				eventManager.syncGoogleEvents(true,refreshToken.getRefreshToken(), refreshToken.getUser());
			}
		}
	}
}
