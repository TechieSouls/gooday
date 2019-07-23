package com.cg.constant;

public class CgConstants {
	public enum ErrorCodes {
		UserNotFound(99),
		IncorrectEmailOrPassword(100), 
		IncorrectPassword(101), 
		UserNameOrEmailAlreadyTaken(102), 
		EmailAlreadyTaken(103), 
		EmailLenghtMustBeSixCharater(104),
		NameLenghtMustBeSixCharater(105),
		PasswordLenghtMustBeSixCharater(106), 
		UserNameLenghtMustBeSixCharater(107),
		RowNotFound(108),
		UserDoestNotHavePermission(109),
		FriendAlreadyNotYourFriend(110),
		FriendAlreadyYourFriend(111),
		CantFollowYourSelf(112),
		FacebookLoginNeedAcessToekn(113),
		FacebookAcessTokenExpires(114),
		PasswordNotPresent(115),
		EmailNotPresent(116),
		FacebookIdNotPresent(117),
		FacebookTokenNotPresent(118),
		NameNotPresent(119),
		AuthTypeNotPresent(120),
		InternalServerError(121),
		PhoneAlreadyTaken(122);
		int errorCode;

		private ErrorCodes(int i) {
			errorCode = i;
		}

		public int getErrorCode() {
			return errorCode;
		}
	}
	
	public static String notificationTypeTitle = "title";
	public static String notificationTypeId = "id";
	public static String notificationType = "type";
	public static String notificationTypeStatus = "status";
	public static String maxmindCityDatabase = "/home/ubuntu/garage/beta/gooday/java/gooday/src/main/resources/GeoLite2-City.mmdb";
	
}
