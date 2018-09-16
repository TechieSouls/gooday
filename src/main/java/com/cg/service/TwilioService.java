package com.cg.service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.authy.AuthyApiClient;
import com.authy.api.Params;
import com.authy.api.PhoneVerification;
import com.authy.api.Verification;

public class TwilioService {
	
	// Find your Account Sid and Token at https://www.twilio.com/console/verify/applications/133573/settings
	public static final String API_KEY = "aWwtiuTOZ9SNNb7dXSAMZ3MrDSbnOeeW";

	//SAND BOX
	//public static final String API_KEY = "bf12974d70818a08199d17d5e2bae630";
	//public static final String apiUrl = "http://sandbox-api.authy.com";
	public static final String apiUrl = "https://api.authy.com"; //Prod
	
	/*public static void main(String[] args) {
		//AuthyApiClient client = new AuthyApiClient(API_KEY);
		TwilioService ts = new TwilioService();
		
		
		String countryCode = "91";
		String phone = "8437375294";
		
		Map<String,Object> sendVerResp = ts.sendVerificationCode(countryCode, phone);
		
		if ((Boolean)sendVerResp.get("success")) {
			String code = (String)sendVerResp.get("code").toString();
			ts.checkVerificationCode(countryCode, phone, code);
		}
	}*/
	
	public Map<String,Object> sendVerificationCode(String countryCode, String phone) {
		
		Map<String,Object> response = new HashMap<>();
		response.put("success", true);
		
		boolean debugMode = true;
		AuthyApiClient client = new AuthyApiClient(API_KEY, apiUrl, debugMode);
		PhoneVerification phoneVerification  = client.getPhoneVerification();

		Verification verification;
		Params params = new Params();
		params.setAttribute("locale", Locale.ENGLISH.toString());

		verification = phoneVerification.start(phone, countryCode, "sms", params);

		//System.out.println(verification.getMessage());
		//System.out.println(verification.getSuccess());
		//System.out.println(verification.isOk());
		response.put("message",verification.getMessage());
		if (!Boolean.valueOf(verification.getSuccess())) {
			response.put("success", false);
			response.put("message",verification.getMessage());
		}
		return response;
	}
	
	public Map<String,Object> checkVerificationCode(String countryCode, String phone, String verificationCode) {
		Map<String,Object> response = new HashMap<>();
		response.put("success", true);
		
		boolean debugMode = true;
		
		AuthyApiClient client = new AuthyApiClient(API_KEY, apiUrl, debugMode);
		PhoneVerification phoneVerification = client.getPhoneVerification();

		Verification verification;
		verification = phoneVerification.check(phone, countryCode, verificationCode);

		//System.out.println(verification.getMessage());
		//System.out.println(verification.getSuccess());
		response.put("message",verification.getMessage());
		if (!Boolean.valueOf(verification.getSuccess())) {
			response.put("success", false);
		}
		return response;
	}
}
