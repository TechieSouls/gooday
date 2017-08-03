package com.cg.events.bo;

public class GoogleEventAttendees {
	
		private String email;
		private Boolean self;
		private String displayName;
		private String responseStatus;
		
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		public Boolean getSelf() {
			return self;
		}
		public void setSelf(Boolean self) {
			this.self = self;
		}
		public String getDisplayName() {
			return displayName;
		}
		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}
		public String getResponseStatus() {
			return responseStatus;
		}
		public void setResponseStatus(String responseStatus) {
			this.responseStatus = responseStatus;
		}
}
