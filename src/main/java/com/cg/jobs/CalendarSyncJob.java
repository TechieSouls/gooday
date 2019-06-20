package com.cg.jobs;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.primefaces.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.cg.bo.CalendarSyncToken;
import com.cg.bo.CalendarSyncToken.AccountType;
import com.cg.manager.EventManager;
import com.cg.service.GoogleService;
import com.cg.service.OutlookService;

@Service
public class CalendarSyncJob {
	
	@Autowired
	EventManager eventManager;
	
	//				s m h d m y
	@Scheduled(cron="0/1 0 23 * * *") //At every 23rd hour
	//@Scheduled(cron="0/1 32 18 * * *") //At every 23rd hour
	public void syncGoogleCalendar() {
		System.out.println("[Google Calendar Sync Job STARTS]");
		
		GoogleService googleService = new GoogleService();
		OutlookService outlookService = new OutlookService();
		
		List<CalendarSyncToken> refreshTokens = eventManager.findCalendarSyncTokensWithLastExpiryDate();
		if (refreshTokens != null && refreshTokens.size() > 0) {
			int numberOfThreadsToGenerate = refreshTokens.size() / 10;
			int numberOfThreadsLeftAsRemainder = refreshTokens.size() % 10;
			
			for (int number=0 ; number < numberOfThreadsToGenerate; number++) {
				List<CalendarSyncToken> refTokens = refreshTokens.subList(number, (number+1)*10);
				RenewSubScriptionThread refreshTokenThread = new RenewSubScriptionThread();
				refreshTokenThread.setOutlookService(outlookService);
				refreshTokenThread.setGoogleService(googleService);
				refreshTokenThread.setRefreshTokens(refTokens);
				refreshTokenThread.setEventManager(eventManager);
				refreshTokenThread.run();
			}
			
			List<CalendarSyncToken> refTokens = refreshTokens.subList(refreshTokens.size()-numberOfThreadsLeftAsRemainder, refreshTokens.size());
			RenewSubScriptionThread renewSubScriptionThread = new RenewSubScriptionThread();
			renewSubScriptionThread.setRefreshTokens(refTokens);
			renewSubScriptionThread.run();
		}
		System.out.println("[Google Calendar Sync Job ENDS]");

	}
	
	public void renewSubscriptionForPushNotifications() {
		
		System.out.println("[Calendar Subscription Job STARTS]");
		List<CalendarSyncToken> calendarSyncTokensToRenew = eventManager.findCalendarSyncTokensWithLastExpiryDate();
		if (calendarSyncTokensToRenew != null && calendarSyncTokensToRenew.size() > 0) {
			int numberOfThreadsToGenerate = calendarSyncTokensToRenew.size() / 10;
			int numberOfThreadsLeftAsRemainder = calendarSyncTokensToRenew.size() % 10;
			
			for (int number=0 ; number < numberOfThreadsToGenerate; number++) {
				List<CalendarSyncToken> refTokens = calendarSyncTokensToRenew.subList(number, (number+1)*10);
				RefershTokenThread refreshTokenThread = new RefershTokenThread();
				refreshTokenThread.setRefreshTokens(refTokens);
				refreshTokenThread.run();
			}
			
			List<CalendarSyncToken> refTokens = calendarSyncTokensToRenew.subList(calendarSyncTokensToRenew.size()-numberOfThreadsLeftAsRemainder, calendarSyncTokensToRenew.size());
			RefershTokenThread refreshTokenThread = new RefershTokenThread();
			refreshTokenThread.setRefreshTokens(refTokens);
			refreshTokenThread.run();
		}
		System.out.println("[Calendar Subscription Job ENDS]");

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
	
	class RenewSubScriptionThread implements Runnable {

		private List<CalendarSyncToken> refreshTokens;
		private GoogleService googleService;
		private OutlookService outlookService;
		private EventManager eventManager;
		
		public List<CalendarSyncToken> getRefreshTokens() {
			return refreshTokens;
		}
		public void setRefreshTokens(List<CalendarSyncToken> refreshTokens) {
			this.refreshTokens = refreshTokens;
		}

		public GoogleService getGoogleService() {
			return googleService;
		}

		public void setGoogleService(GoogleService googleService) {
			this.googleService = googleService;
		}

		public EventManager getEventManager() {
			return eventManager;
		}

		public void setEventManager(EventManager eventManager) {
			this.eventManager = eventManager;
		}

		public OutlookService getOutlookService() {
			return outlookService;
		}
		public void setOutlookService(OutlookService outlookService) {
			this.outlookService = outlookService;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			for (CalendarSyncToken calSyncTkn: refreshTokens) {
				if (calSyncTkn.getAccountType().equals(AccountType.Google) && calSyncTkn.getSubscriptionId() != null) {
					
					try {
						JSONObject refreshTokenResponse = googleService
								.getAccessTokenFromRefreshToken(calSyncTkn.getRefreshToken());
						
						
						if (refreshTokenResponse != null) {
							System.out.println("[Google Sync] Date : " + new Date() + " Response from Refresh Token : "
									+ refreshTokenResponse.toString());

							String accessToken = refreshTokenResponse.getString("access_token");

							JSONObject subscribeResponse = googleService.subscribeToGoogleEventWatcher(accessToken, calSyncTkn.getSubscriptionId());
							if (subscribeResponse != null) {
								Long expirationTime = subscribeResponse.getLong("expiration");
								calSyncTkn.setResourceId(subscribeResponse.getString("resourceId"));
								calSyncTkn.setSubExpiryDate(new Date(expirationTime));
								eventManager.saveCalendarSyncToken(calSyncTkn);
							}
						}
					} catch(Exception e) {
						System.out.println("Exceptoion in Google Renew Subscription : ");
						System.out.println(e.getMessage());
					}
					
				} else if (calSyncTkn.getAccountType().equals(AccountType.Outlook) && calSyncTkn.getSubscriptionId() != null) {
					
					try {
							Map<String, Object> subscribeResponse = outlookService.renewOutlookService(calSyncTkn);
							if (subscribeResponse != null && subscribeResponse.containsKey("renew_date")) {
								
									calSyncTkn.setSubExpiryDate((Date)subscribeResponse.get("renew_date"));
									eventManager.saveCalendarSyncToken(calSyncTkn);
							}
					} catch(Exception e) {
						System.out.println("Exceptoion in Google Renew Subscription : ");
						System.out.println(e.getMessage());
					}
				}
			} 
		
		}
	}
}
