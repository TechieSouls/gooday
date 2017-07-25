package com.cg.constant;

public class CgConstants {
	public enum ErrorCodes {
		UserNotFound(100), 
		IncorrectPassword(101), 
		UserNameOrEmailAlreadyTaken(102), 
		EmailLenghtMustBeSixCharater(103),
		NameLenghtMustBeSixCharater(104),
		PasswordLenghtMustBeSixCharater(105), 
		UserNameLenghtMustBeSixCharater(106),
		RowNotFound(107),
		UserDoestNotHavePermission(108),
		FriendAlreadyNotYourFriend(109),
		FriendAlreadyYourFriend(110),
		CantFollowYourSelf(111),
		FacebookLoginNeedAcessToekn(112),
		FacebookAcessToeknExpires(113);
		int errorCode;

		private ErrorCodes(int i) {
			errorCode = i;
		}

		public int getErrorCode() {
			return errorCode;
		}
	}
}
