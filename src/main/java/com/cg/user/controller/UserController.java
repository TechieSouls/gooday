package com.cg.user.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.codec.binary.Base64;
import org.primefaces.json.JSONArray;
import org.primefaces.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cg.bo.CalendarSyncToken;
import com.cg.bo.CenesProperty.PropertyOwningEntity;
import com.cg.bo.CenesPropertyValue;
import com.cg.bo.FacebookProfile;
import com.cg.bo.HolidayCalendar;
import com.cg.bo.UserFriend;
import com.cg.bo.UserFriend.UserStatus;
import com.cg.bo.UserStat;
import com.cg.bo.CalendarSyncToken.AccountType;
import com.cg.constant.CgConstants.ErrorCodes;
import com.cg.dao.EventServiceDao;
import com.cg.dto.ChangePasswordDto;
import com.cg.enums.CgEnums.AuthenticateType;
import com.cg.events.bo.Event;
import com.cg.events.bo.MeTime;
import com.cg.events.bo.MeTimeEvent;
import com.cg.events.bo.RecurringEvent;
import com.cg.events.bo.RecurringEvent.RecurringEventProcessStatus;
import com.cg.events.bo.RecurringEvent.RecurringEventStatus;
import com.cg.events.bo.RecurringPattern;
import com.cg.manager.EmailManager;
import com.cg.manager.EventManager;
import com.cg.manager.EventTimeSlotManager;
import com.cg.manager.GeoLocationManager;
import com.cg.manager.RecurringManager;
import com.cg.repository.CenesPropertyValueRepository;
import com.cg.repository.UserContactRepository;
import com.cg.repository.UserFriendRepository;
import com.cg.repository.UserRepository;
import com.cg.service.FacebookService;
import com.cg.service.GoogleService;
import com.cg.service.OutlookService;
import com.cg.service.TwilioService;
import com.cg.service.UserService;
import com.cg.stateless.security.TokenAuthenticationService;
import com.cg.threads.UserThread;
import com.cg.user.bo.User;
import com.cg.user.bo.UserContact;
import com.cg.user.bo.UserContact.CenesMember;
import com.cg.user.bo.UserDevice;
import com.cg.utils.CenesUtils;
import com.google.common.collect.Sets;
import com.maxmind.geoip2.record.Country;

@RestController
@Api(value = "User", description = "Create User data")
public class UserController {

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	EventManager eventManager;
	
	@Autowired
	EventTimeSlotManager eventTimeSlotManager;
	
	@Autowired
	RecurringManager recurringManager;
	
	@Autowired
	UserFriendRepository userFriendRepository;

	@Autowired
	UserContactRepository userContactRepository;
	
	@Autowired
	UserService userService;
	
	@Autowired
	EmailManager emailManager;
	
	@Autowired
	GeoLocationManager geoLoactionManager;
	
	@Autowired
	CenesPropertyValueRepository cenesPropertyValueRepository;
	
	@Autowired
	EventServiceDao eventServiceDao;
	
	@Value("${cenes.imageUploadPath}")
	private String imageUploadPath;

	@Value("${cenes.profileImageUploadPath}")
	private String profileImageUploadPath;
	
	@Value("${cenes.recurringEventUploadPath}")
	private String recurringEventUploadPath;
	
	@Value("${cenes.domain}")
	private String domain;
	
	@Value("${cenes.salt}")
	private String salt;
	
	@ApiOperation(value = "Create user", notes = "create user ", code = 200, httpMethod = "POST", produces = "application/json")
	@ModelAttribute(value = "user")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "product updated successfuly") })
	@RequestMapping(value = "/api/users/", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<User> createUser(
			HttpServletResponse httpServletResponse,
			@ApiParam(name = "User", value = "user dummy data", required = true) @RequestBody User user) {
		
		User userInfo = null;
		
		//Setting auth type in case it is not passed in the api
		if (user.getAuthType() == null) {
			if (user.getFacebookId() != null) {
				user.setAuthType(AuthenticateType.facebook);
			} else if (user.getEmail() != null) {
				user.setAuthType(AuthenticateType.email);
			}
			if (user.getAuthType() == null) {
				user.setErrorCode(ErrorCodes.AuthTypeNotPresent.getErrorCode());
				user.setErrorDetail(ErrorCodes.AuthTypeNotPresent.toString());
				return new ResponseEntity<User>(user,HttpStatus.PARTIAL_CONTENT);
			}
		}
		
		if (user.getAuthType().equals(AuthenticateType.email)) {
			if (user.getPassword() == null) {
				user.setErrorCode(ErrorCodes.PasswordNotPresent.getErrorCode());
				user.setErrorDetail(ErrorCodes.PasswordNotPresent.toString());
				return new ResponseEntity<User>(user,HttpStatus.PARTIAL_CONTENT);
			}
			if (user.getEmail() == null) {
				user.setErrorCode(ErrorCodes.EmailNotPresent.getErrorCode());
				user.setErrorDetail(ErrorCodes.EmailNotPresent.toString());
				return new ResponseEntity<User>(user,HttpStatus.PARTIAL_CONTENT);
			}
			if (user.getName() == null) {
				user.setErrorCode(ErrorCodes.NameNotPresent.getErrorCode());
				user.setErrorDetail(ErrorCodes.NameNotPresent.toString());
				return new ResponseEntity<User>(user,HttpStatus.PARTIAL_CONTENT);
			}
			System.out.println("[ Date : "+new Date()+" ] ,UserType : Email, Message : Email Type User");
			userInfo = userService.findUserByEmail(user.getEmail());
			if (userInfo != null) {
				user.setPassword(null);
				user.setErrorCode(ErrorCodes.EmailAlreadyTaken.getErrorCode());
				user.setErrorDetail(ErrorCodes.EmailAlreadyTaken.toString());
				System.out.println("[ Date : "+new Date()+" ] ,UserType : Email, Message : Email Already Exists");
				
				return new ResponseEntity<User>(user, HttpStatus.OK);
			}
			userInfo = userService.findUserByPhone(user.getPhone());
			if (userInfo != null) {
				user.setPassword(null);
				user.setErrorCode(ErrorCodes.PhoneAlreadyTaken.getErrorCode());
				user.setErrorDetail(ErrorCodes.PhoneAlreadyTaken.toString());
				System.out.println("[ Date : "+new Date()+" ] ,UserType : Email, Message : Phone Already Exists");
				
				return new ResponseEntity<User>(user, HttpStatus.OK);
			}
			
			if (userInfo == null) {
				System.out.println("[ Date : "+new Date()+" ] ,UserType : Email, Message : New signup request");
				try {
					user.setUsername(user.getName().toLowerCase().replaceAll(" ",".")+System.currentTimeMillis());
					user.setPassword(new Md5PasswordEncoder().encodePassword(user.getPassword(), salt));
					user.setToken(establishUserAndLogin(httpServletResponse, user));
					userInfo = userService.saveUser(user);
					
					String userNumber = user.getPhone().replaceAll("\\+", "").substring(2, user.getPhone().replaceAll("\\+", "").length());
					List<UserContact> userContactInOtherContacts = userContactRepository.findByPhoneContaining(userNumber);
					if (userContactInOtherContacts != null && userContactInOtherContacts.size() > 0) {
						for (UserContact userContact : userContactInOtherContacts) {
							userContact.setCenesMember(CenesMember.yes);
							userContact.setFriendId(user.getUserId());
						}
						userContactRepository.save(userContactInOtherContacts);
						
						//Now lets update the counts of cenes member counts under user stats
						UserThread userThread = new UserThread();
						userThread.runUpdateUserStatThreadByContacts(userContactInOtherContacts, userService);
					}
					
				} catch(Exception e) {
					e.printStackTrace();
					user.setPassword(null);
					user.setErrorCode(ErrorCodes.EmailAlreadyTaken.getErrorCode());
					user.setErrorDetail(ErrorCodes.EmailAlreadyTaken.toString());
					return new ResponseEntity<User>(user, HttpStatus.OK);
				}
			}
			
			recurringManager.saveDefaultMeTime(userInfo.getUserId());
			
			System.out.println("[ Date : "+new Date()+" ] ,UserType : Email, Message : User Details -> "+userInfo.toString());
			return new ResponseEntity<User>(userInfo, HttpStatus.ACCEPTED);
		} else if (user.getAuthType().equals(AuthenticateType.facebook)) {
			if (user.getFacebookAuthToken() == null) {
				user.setErrorCode(ErrorCodes.FacebookTokenNotPresent.getErrorCode());
				user.setErrorDetail(ErrorCodes.FacebookTokenNotPresent.toString());
				return new ResponseEntity<User>(user, HttpStatus.PARTIAL_CONTENT);
			}
			if (user.getFacebookId() == null) {
				user.setErrorCode(ErrorCodes.FacebookIdNotPresent.getErrorCode());
				user.setErrorDetail(ErrorCodes.FacebookIdNotPresent.toString());
				return new ResponseEntity<User>(user, HttpStatus.PARTIAL_CONTENT);
			}
			
			System.out.println("[ Date : "+new Date()+" ] ,UserType : Facebook, Message : Facebook Type User");
			userInfo = userRepository.findUserByFacebookId(user.getFacebookId());
			if (userInfo == null) {
				System.out.println("[ Date : "+new Date()+" ] ,UserType : Facebook, Message : New signup request");
				try {
					//Create new user if not exists.
					FacebookService facebookService = new FacebookService();
					FacebookProfile facebookProfile = facebookService.facebookProfile(user.getFacebookAuthToken());
					if (facebookProfile.getName() != null) {
						user.setName(facebookProfile.getName());
						user.setUsername(user.getName().toLowerCase().replaceAll(" ",".")+System.currentTimeMillis());
					}
					if (facebookProfile.getPicture() != null) {
						Map<String,Object> pictureMap = facebookProfile.getPicture();
						Map<String,String> dataMap = (Map<String,String>)pictureMap.get("data");
						user.setPhoto(dataMap.get("url"));
					}
					if (user.getUsername() == null) {
						user.setUsername(user.getName().toLowerCase().replaceAll(" ",".")+System.currentTimeMillis());
					}
					if (facebookProfile.getEmail() != null && facebookProfile.getEmail().length() > 0) {
						user.setEmail(facebookProfile.getEmail());
					}
					user.setGender(facebookProfile.getGender());
					user.setToken(establishUserAndLogin(httpServletResponse, user));
					userInfo = userService.saveUser(user);
					userInfo.setIsNew(true);
				} catch(Exception e) {
					e.printStackTrace();
					user.setErrorCode(ErrorCodes.FacebookAcessTokenExpires.getErrorCode());
					user.setErrorDetail(ErrorCodes.FacebookAcessTokenExpires.toString());
					return new ResponseEntity<User>(user, HttpStatus.FORBIDDEN);
				}
			} else {
				FacebookService facebookService = new FacebookService();
				FacebookProfile facebookProfile = facebookService.facebookProfile(user.getFacebookAuthToken());
				if (facebookProfile.getPicture() != null) {
					Map<String,Object> pictureMap = facebookProfile.getPicture();
					Map<String,String> dataMap = (Map<String,String>)pictureMap.get("data");
					user.setPhoto(dataMap.get("url"));
				}
				userInfo.setFacebookAuthToken(user.getFacebookAuthToken());
				if (user.getPhoto() != null) {
					userInfo.setPhoto(user.getPhoto());
				}
				if (facebookProfile.getEmail() != null && facebookProfile.getEmail().length() > 0) {
					userInfo.setEmail(facebookProfile.getEmail());
				}
				userInfo.setGender(facebookProfile.getGender());
				userInfo = userService.saveUser(userInfo);
				userInfo.setIsNew(false);
				System.out.println("[ Date : "+new Date()+" ] ,UserType : Facebook, Message : Old user retrived from database");
			}
			System.out.println("[ Date : "+new Date()+" ] ,UserType : Facebook, Message : User Details -> "+userInfo.toString());
			return new ResponseEntity<User>(userInfo, HttpStatus.ACCEPTED);
		}
		user.setErrorCode(ErrorCodes.InternalServerError.getErrorCode());
		user.setErrorDetail(ErrorCodes.InternalServerError.toString());
		return new ResponseEntity<User>(user, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/api/users/signupstep1", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> signupUserStep1(@RequestBody User user, HttpServletResponse httpServletResponse) {

		
		boolean isNewUser = false;
		
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		
		//Lets check if same phone number exists for the user.
		if (user.getPhone() == null) {
			response.put("success", false);
			response.put("message", "Please visit Phone Verification Steps");
			return new ResponseEntity<Map<String,Object>>(response, HttpStatus.OK);
		}

		//If User signup via Email
		if (user.getAuthType().equals(AuthenticateType.email)) {
			
			User userByPhone = userRepository.findByPhone(user.getPhone());
			if (userByPhone != null) {
				response.put("success", false);
				response.put("message", "Phone Already Exists.");
				return new ResponseEntity<Map<String,Object>>(response, HttpStatus.OK);
			}
			
			
			
			User emailUser = userRepository.findUserByEmail(user.getEmail());
			if (emailUser != null) {
				response.put("success", false);
				response.put("message", "This Account Already Exists");
				return new ResponseEntity<Map<String,Object>>(response, HttpStatus.OK);
			}
			
			user.setUsername(user.getEmail().split("@")[0].replaceAll(" ",".")+System.currentTimeMillis());
			user.setPassword(new Md5PasswordEncoder().encodePassword(user.getPassword(), salt));
			user.setToken(establishUserAndLogin(httpServletResponse, user));
			user = userService.saveUser(user);
			
			isNewUser = true;
		}
		
		User emailUser = null;
		//If its a Facebook User Request
		if (user.getAuthType().equals(AuthenticateType.facebook)) {
			
			User userByPhone = userRepository.findByPhone(user.getPhone());

			//Lets first find the user for the email used in facebook
			if (user.getEmail() != null) {
				emailUser = userRepository.findByEmailAndFacebookIdIsNull(user.getEmail());
				
				if (userByPhone != null && !user.getEmail().equals(userByPhone.getEmail())) {
					response.put("success", false);
					response.put("message", "Phone Already Exists For Other Account");
					return new ResponseEntity<Map<String,Object>>(response, HttpStatus.OK);
				}
			}
			
			User facebookIdUser = userRepository.findUserByFacebookId(user.getFacebookId());
			if (facebookIdUser != null) {
				
				if (facebookIdUser.getPhone() == null && user.getPhone() != null) {
					facebookIdUser.setPhone(user.getPhone());
					facebookIdUser = userService.saveUser(facebookIdUser);
				}
				
				facebookIdUser.setIsNew(false);
				response.put("success", true);
				response.put("data", facebookIdUser);
				return new ResponseEntity<Map<String,Object>>(response, HttpStatus.OK);
			}
			
			
		}
		
		//If its a Google User Request
		if (user.getAuthType().equals(AuthenticateType.google)) {
			
			User userByPhone = userRepository.findByPhone(user.getPhone());
			
			//Lets first find the user for the email used in google
			if (user.getEmail() != null) {
				emailUser = userRepository.findByEmailAndGoogleIdIsNull(user.getEmail());
				
				if (userByPhone != null && !user.getEmail().equals(userByPhone.getEmail())) {
					
					response.put("success", false);
					response.put("message", "Phone Already Exists For Other Account");
					return new ResponseEntity<Map<String,Object>>(response, HttpStatus.OK);

				}
			}
			
			User googleIdUser = userRepository.findUserByGoogleId(user.getGoogleId());
			if (googleIdUser != null) {
				
				if (googleIdUser.getPhone() == null && user.getPhone() != null) {
					googleIdUser.setPhone(user.getPhone());
					googleIdUser = userService.saveUser(googleIdUser);
				}
				
				googleIdUser.setIsNew(false);
				response.put("success", true);
				response.put("data", googleIdUser);
				return new ResponseEntity<Map<String,Object>>(response, HttpStatus.OK);
			}
		}

		//We will get email user if user did not logged in via Facebook/Google
		if (emailUser != null) {
			 
			if (emailUser.getPhone() == null && user.getPhone() != null) {
				emailUser.setPhone(user.getPhone());
			}
			emailUser.setPhoto(user.getPhoto());
			emailUser.setName(user.getName());
			emailUser.setGender(user.getGender());
			if (user.getAuthType().equals(AuthenticateType.facebook)) {
				emailUser.setFacebookId(user.getFacebookId());
				emailUser.setFacebookAuthToken(user.getFacebookAuthToken());
			} else if (user.getAuthType().equals(AuthenticateType.google)) {
				emailUser.setGoogleId(user.getGoogleId());
				emailUser.setGoogleAuthToken(user.getGoogleAuthToken());
			}
		
			user = emailUser;
			user.setIsNew(false);

		} else {
			
			isNewUser = true;
			
			user.setIsNew(true);
			user.setUsername(user.getEmail().split("@")[0].replaceAll(" ",".")+System.currentTimeMillis());
			user.setToken(establishUserAndLogin(httpServletResponse, user));
		}
		
		user = userService.saveUser(user);
		try {
			//Updating this user in other users contact list.
			if (user.getPhone() != null) {
				String userNumber = user.getPhone().replaceAll("\\+", "").substring(2, user.getPhone().replaceAll("\\+", "").length());
				List<UserContact> userContactInOtherContacts = userContactRepository.findByPhoneContaining(userNumber);
				if (userContactInOtherContacts != null && userContactInOtherContacts.size() > 0) {
					for (UserContact userContact : userContactInOtherContacts) {
						userContact.setCenesMember(CenesMember.yes);
						userContact.setFriendId(user.getUserId());
					}
					userContactRepository.save(userContactInOtherContacts);
					
					//Now lets update the counts of cenes member counts under user stats
					//UserThread userThread = new UserThread();
					//userThread.runUpdateUserStatThreadByContacts(userContactInOtherContacts, userService);`
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			user.setErrorCode(ErrorCodes.InternalServerError.ordinal());
			user.setErrorDetail(ErrorCodes.InternalServerError.toString());
			return new ResponseEntity<Map<String,Object>>(response, HttpStatus.OK);
		}
		
		if (isNewUser) {
			recurringManager.saveDefaultMeTime(user.getUserId());
		}
		
		
		response.put("data", user);
		return new ResponseEntity<Map<String,Object>>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/api/users/signupstep2", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> signupUserStep2(@RequestBody User user) {

		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		
		User emailUser = userRepository.findUserByEmail(user.getEmail());
		if (emailUser != null) {
			response.put("success", false);
			response.put("message", "This Account Already Exists");
			return new ResponseEntity<Map<String,Object>>(response, HttpStatus.OK);
		}
		response.put("data", user);
		return new ResponseEntity<Map<String,Object>>(response, HttpStatus.OK);
	}
	

	@ResponseBody
	@RequestMapping(value="/api/user/update/", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> updateUser(@RequestBody User user) {
		
		User dbUser = userService.findUserById(user.getUserId());
		dbUser.setName(user.getName());
		//dbUser.setEmail(user.getEmail());
		if (user.getPhoto() != null && user.getPhoto().length() > 0) {
			dbUser.setPhoto(user.getPhoto());
		}
		dbUser.setGender(user.getGender());
		dbUser.setBirthDate(user.getBirthDate());
		dbUser.setBirthDayStr(user.getBirthDayStr());

		
		Map<String, Object> response = new HashMap<>();
		try {
			dbUser = userRepository.save(dbUser);
			response.put("success", true);
			response.put("data", dbUser);
			response.put("errorCode", 0);
			response.put("errorDetail", null);
		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("data", "");
			response.put("errorCode", HttpStatus.INTERNAL_SERVER_ERROR.ordinal());
			response.put("errorDetail", HttpStatus.INTERNAL_SERVER_ERROR.toString());
		}
		return new ResponseEntity<Map<String,Object>>(response, HttpStatus.OK);
	}
	
	@ResponseBody
	@RequestMapping(value="/api/user/calendarsyncstatus/{user_id}", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> userCalendarSyncStatus(@PathVariable("user_id") String userId) {
		Map<String, Object> response = new HashMap<>();

		List<CenesPropertyValue> cenesPropertyValues = cenesPropertyValueRepository
				.findByEntityIdAndPropertyOwningEntity(Long.parseLong(userId), PropertyOwningEntity.User);
		try {
			response.put("success", true);
			response.put("errorCode", 0);
			response.put("errorDetail", null);
			if (cenesPropertyValues.size() > 0) {
				response.put("data", cenesPropertyValues);
			} else {
				response.put("data", new ArrayList<>());
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("data", new ArrayList<>());
			response.put("errorCode", HttpStatus.INTERNAL_SERVER_ERROR.ordinal());
			response.put("errorDetail", HttpStatus.INTERNAL_SERVER_ERROR.toString());
		}

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);

	}
	
	@ApiOperation(value = "Facebeook Friends", notes = "Get Facebook Friends", code = 200, httpMethod = "GET", produces = "application/json")
	@ModelAttribute(value = "friends")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Friends Retrived Successfully") })
	@RequestMapping(value = "/api/facebook/friends/{facebook_id}/{auth_token}/{user_id}", method = RequestMethod.GET,
		produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> getFacebookFriends(@ApiParam(name = "facebook_id", value = "facebook id of the  user", required = true) 
		@PathVariable("facebook_id") String facebookId, @ApiParam(name = "auth_token", value = "auth_token of the  user", required = true) 
		@PathVariable("auth_token") String auth_token, @PathVariable("user_id") String userId) {
		try {
			
			 String secret = "8f3199da15c6b58a18efd02177824f3a";
		     String message = auth_token;

		     Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
		     SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
		     sha256_HMAC.init(secret_key);

		     String hash = Base64.encodeBase64String(sha256_HMAC.doFinal(message.getBytes()));
		     System.out.println(hash);
			
			/*String url = "https://graph.facebook.com/"+facebookId+"/friends?access_token="
					+ auth_token+"&appsecret_proof="+hash;*/
		     String url = "https://graph.facebook.com/"+facebookId+"/friends?access_token=" + auth_token;
			
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			System.out.println(con.getContent().toString());
			if (responseCode == 200) {

				System.out.println("Response Code : " + responseCode);

				BufferedReader in = new BufferedReader(new InputStreamReader(
						con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				org.primefaces.json.JSONObject jObject = new org.primefaces.json.JSONObject(
						response.toString());
				JSONArray jsonArray =  (JSONArray) jObject.get("data");
				if (jsonArray.length() > 0) {
					List<UserFriend> userFriendList = new ArrayList<>();
					for(int i = 0 ; i < jsonArray.length();i++){
						JSONObject jsonObject = (JSONObject) jsonArray.get(i);
						jsonObject.put("profile_picture", "https://graph.facebook.com/v2.9/"+jsonObject.getString("id")+"/picture?width=640&height=640");
						UserFriend userFriend = new UserFriend();
						userFriend.setCreatedAt(new Date());
						userFriend.setUpdateAt(new Date());
						userFriend.setSource("Facebook");
						userFriend.setStatus(UserStatus.Requested);
						userFriend.setFriendId(Long.parseLong(userId));
						userFriend.setSourceId(Long.parseLong(jsonObject.get("id").toString()));
						userFriendList.add(userFriend);
					}
					userFriendRepository.save(userFriendList);
					return new ResponseEntity<String>(jObject.toString(), HttpStatus.OK);
				} else {
					System.out.println("Empity friend list of the user");
					return null;
				}
				
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@RequestMapping(value = "/api/profile/upload", method = RequestMethod.POST)
    public ResponseEntity<User> uploadImages(@RequestParam("mediaFile") MultipartFile uploadfile,@RequestParam("userId") String userId) {
    	
		User user = userRepository.findOne(Long.valueOf(userId));
		String dirPath = imageUploadPath.replaceAll("\\[userId\\]", userId);
		
		File file = new File(dirPath);
		if (file != null) {
			File[] files = file.listFiles(); 
	        if (files != null && files.length > 0) {
	            for (File f:files) {
	            	if (f.isFile() && f.exists()) { 
	            		f.delete();
	            		System.out.println("successfully deleted");
	                } else {
	                	System.out.println("cant delete a file due to open or error");
	                } 
	            }
	        }
		}

		InputStream inputStream = null;
        OutputStream outputStream = null;
        String extension = uploadfile.getOriginalFilename().substring(uploadfile.getOriginalFilename().trim().lastIndexOf("."),uploadfile.getOriginalFilename().length());
        
        String fileName = UUID.randomUUID().toString()+extension;

        File f = new File(dirPath);
        if(!f.exists()) { 
        	try {
				f.mkdirs();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }        
        File newFile = new File(dirPath+fileName);
        try {
            inputStream = uploadfile.getInputStream();

            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            outputStream = new FileOutputStream(newFile);
            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
           
            String profilePicUrl = domain+"/assets/uploads/"+userId+"/profile/"+fileName;
            user.setPhoto(profilePicUrl);
            userService.saveUser(user);
            
            eventManager.updateEventMemberPicture(profilePicUrl,user.getUserId());
            
        } catch (Exception e) {
        	e.printStackTrace();
        	user = new User();
        	user.setErrorCode(HttpStatus.BAD_REQUEST.ordinal());
        	user.setErrorDetail(HttpStatus.BAD_REQUEST.toString());
        	return new ResponseEntity<User>(user, HttpStatus.OK);
        }
        return new ResponseEntity<User>(user, HttpStatus.OK);
    }
	
	@RequestMapping(value = "/api/user/profile/upload", method = RequestMethod.POST)
    public ResponseEntity<User> uploadImages(MultipartFile mediaFile, Long userId) {
    	
		User user = userRepository.findOne(Long.valueOf(userId));
		
		/*File file = new File(profileImageUploadPath);
		if (file != null) {
			File[] files = file.listFiles(); 
	        if (files != null && files.length > 0) {
	            for (File f:files) {
	            	if (f.isFile() && f.exists()) { 
	            		f.delete();
	            		System.out.println("successfully deleted");
	                } else {
	                	System.out.println("cant delete a file due to open or error");
	                } 
	            }
	        }
		}*/

		InputStream inputStream = null;
        OutputStream outputStream = null;
        String extension = mediaFile.getOriginalFilename().substring(mediaFile.getOriginalFilename().trim().lastIndexOf("."),mediaFile.getOriginalFilename().length());
        
        String fileName = UUID.randomUUID().toString()+extension;

        File f = new File(profileImageUploadPath);
        if(!f.exists()) { 
        	try {
				f.mkdirs();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }        
        File newFile = new File(profileImageUploadPath+fileName);
        try {
            inputStream = mediaFile.getInputStream();

            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            outputStream = new FileOutputStream(newFile);
            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
           
            String profilePicUrl = domain+"/assets/uploads/profile/"+fileName;
            user.setPhoto(profilePicUrl);
            userService.saveUser(user);
            
            //eventManager.updateEventMemberPicture(profilePicUrl,user.getUserId());
            
        } catch (Exception e) {
        	e.printStackTrace();
        	user = new User();
        	user.setErrorCode(HttpStatus.BAD_REQUEST.ordinal());
        	user.setErrorDetail(HttpStatus.BAD_REQUEST.toString());
        	return new ResponseEntity<User>(user, HttpStatus.OK);
        }
        return new ResponseEntity<User>(user, HttpStatus.OK);
    }
	
	@RequestMapping(value = "/api/user/profile/upload/v2", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> uploadImagev2(MultipartFile mediaFile, Long userId) {
    	
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("success", true);
		
		User user = userRepository.findOne(Long.valueOf(userId));
		
		InputStream inputStream = null;
        OutputStream outputStream = null;
        String extension = mediaFile.getOriginalFilename().substring(mediaFile.getOriginalFilename().trim().lastIndexOf("."),mediaFile.getOriginalFilename().length());
        
        String fileName = UUID.randomUUID().toString()+extension;

        File f = new File(profileImageUploadPath);
        if(!f.exists()) { 
        	try {
				f.mkdirs();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }        
        File newFile = new File(profileImageUploadPath+fileName);
        try {
            inputStream = mediaFile.getInputStream();

            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            outputStream = new FileOutputStream(newFile);
            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
           
            String profilePicUrl = domain+"/assets/uploads/profile/"+fileName;
            user.setPhoto(profilePicUrl);
            userService.saveUser(user);
    		response.put("data", profilePicUrl);

                        
        } catch (Exception e) {
        	e.printStackTrace();
    		response.put("success", false);
    		response.put("message", e.getLocalizedMessage());
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);

        }
        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }
	
	
	@RequestMapping(value = "/api/recurring/upload", method = RequestMethod.POST)
    public ResponseEntity<RecurringEvent> uploadRecurringEventImage(MultipartFile uploadfile,Long recurringEventId) {

		InputStream inputStream = null;
        OutputStream outputStream = null;
        String extension = uploadfile.getOriginalFilename().substring(uploadfile.getOriginalFilename().trim().lastIndexOf("."),uploadfile.getOriginalFilename().length());
        
        String fileName = recurringEventId.toString()+extension;

        File f = new File(recurringEventUploadPath);
        if(!f.exists()) { 
        	try {
				f.mkdirs();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }        
        File newFile = new File(recurringEventUploadPath+fileName);
        try {
            inputStream = uploadfile.getInputStream();

            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            outputStream = new FileOutputStream(newFile);
            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
           
            String photoUrl = "/assets/uploads/recurring/"+fileName;
            RecurringEvent recurringEvent = recurringManager.findByRecurringEventId(recurringEventId);
            if (recurringEvent != null) {
            	recurringEvent.setPhoto(photoUrl);
            	recurringManager.saveRecurringEvent(recurringEvent);
            }
            return new ResponseEntity<RecurringEvent>(recurringEvent, HttpStatus.OK);
            
        } catch (Exception e) {
        	e.printStackTrace();
        	RecurringEvent recurringEvent = new RecurringEvent();
        	return new ResponseEntity<RecurringEvent>(recurringEvent, HttpStatus.OK);
        }
    }
	
	
	@RequestMapping(value = "/api/user/friends", method = RequestMethod.GET)
	@ResponseBody
/*	public ResponseEntity<List<UserFriend>> getUserFriends(@RequestParam("user_id") Long userId) {
		
		List<UserFriend> friends = userFriendRepository.findByFriendId(userId, null);
		return new ResponseEntity<List<UserFriend>>(friends,HttpStatus.OK);
	}
*/	public ResponseEntity<List<User>> getUserFriends(@RequestParam("user_id") Long userId) {
		List<User> friends = null;
		friends = (List)userRepository.findAll();	
		List<User> userFriends = new ArrayList<>();
		for (User user : friends) {
			if (user.getUserId().equals(userId)) {
				continue;
			}
			userFriends.add(user);
		}
		return new ResponseEntity<List<User>>(userFriends,HttpStatus.OK);
	}
	
	@RequestMapping(value = "/api/user/metime", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Map<String, Object>> saveMeTime(@RequestBody MeTime meTime) {
		
		String imageUrl = null;
		if (meTime != null && meTime.getRecurringEventId() != null) {
			
			RecurringEvent recurringEvent = recurringManager.findByRecurringEventId(meTime.getRecurringEventId());
			if (recurringEvent != null) {
				 if (recurringEvent.getPhoto() != null) {
						imageUrl = recurringEvent.getPhoto();

				 }
					recurringEvent.setCreatedById(null);
					recurringManager.saveRecurringEvent(recurringEvent);
			}
			deleteMeTimeByRecurringEventId(meTime.getRecurringEventId());
		}
		
		Long starTimeMillis = new Date().getTime();
		System.out.println("[METIME EVENTS : STARTS TIME "+starTimeMillis+"]");
		System.out.println("[METIME EVENTS : STARTS]");
		Map<String,Integer> dayOfWeekMap = new HashMap<>();
		dayOfWeekMap.put("Sunday", 1);
		dayOfWeekMap.put("Monday",2);
		dayOfWeekMap.put("Tuesday", 3);
		dayOfWeekMap.put("Wednesday", 4);
		dayOfWeekMap.put("Thursday", 5);
		dayOfWeekMap.put("Friday", 6);
		dayOfWeekMap.put("Saturday", 7);
		
		Map<String, Object> responseObject = new HashMap<>();
		RecurringEvent recurringEventToReturn = null;
		try {
			Map<String,RecurringEvent> recurringEventMap = new HashMap<>();
			for (MeTimeEvent meEvent : meTime.getEvents()) {
				Calendar startCal = Calendar.getInstance();
				startCal.setTimeInMillis(meEvent.getStartTime());
				
				System.out.println("MeTime Start Time : "+meEvent.getStartTime());
				
				if (!recurringEventMap.containsKey(meEvent.getTitle())) {
					RecurringEvent recurringEvent = new RecurringEvent();
					recurringEvent.setTitle(meEvent.getTitle());
					//recurringEvent.setDescription(meEvent.getDescription());
					recurringEvent.setCreatedById(meTime.getUserId());
					recurringEvent.setTimezone(meTime.getTimezone());
					recurringEvent.setCreationTimestamp(new Date());
					recurringEvent.setUpdateTimestamp(new Date());
					recurringEvent.setSource("Cenes");
					
					System.out.println(meEvent.getStartTime());
					
			        recurringEvent.setStartTime(startCal.getTime());
			        
			        Calendar endCal =  Calendar.getInstance();
			        endCal.setTimeInMillis(meEvent.getEndTime());
			        if (meEvent.getStartTime() > meEvent.getEndTime()) {
				        endCal.add(Calendar.DAY_OF_MONTH, 1);
			        }
			        System.out.println(endCal.getTime());
			        recurringEvent.setEndTime(endCal.getTime());
					
					recurringEvent.setProcessed(RecurringEventProcessStatus.unprocessed.ordinal());
					recurringEvent.setStatus(RecurringEventStatus.Started.toString());
				
					if (imageUrl != null) {
						recurringEvent.setPhoto(imageUrl);
					}
					
					List<RecurringPattern> recurringPatterns = new ArrayList<>();
					RecurringPattern recurringPattern = new RecurringPattern();
					recurringPattern.setRecurringEventId(recurringEvent.getRecurringEventId());
					//recurringPattern.setDayOfWeek(startCal.get(Calendar.DAY_OF_WEEK));
					//System.out.println(startCal.get(Calendar.DAY_OF_WEEK));
					
					Calendar dayOfWeekCal = Calendar.getInstance();
					dayOfWeekCal.setTimeInMillis(meEvent.getStartTime());
					
					recurringPattern.setDayOfWeek(dayOfWeekCal.get(Calendar.DAY_OF_WEEK));
					//recurringPattern.setDayOfWeek(Integer.valueOf(meEvent.getDayOfWeek()));
					recurringPatterns.add(recurringPattern);
					
					recurringEvent.setRecurringPatterns(recurringPatterns);
					
					recurringEventMap.put(meEvent.getTitle(), recurringEvent);
				} else {
					RecurringEvent recurringEvent = recurringEventMap.get(meEvent.getTitle());
					if (imageUrl != null) {
						recurringEvent.setPhoto(imageUrl);
					}
					List<RecurringPattern> recurringPatterns = recurringEvent.getRecurringPatterns();
					RecurringPattern recurringPattern = new RecurringPattern();
					recurringPattern.setRecurringEventId(recurringEvent.getRecurringEventId());
					
					Calendar dayOfWeekCal = Calendar.getInstance();
					dayOfWeekCal.setTimeInMillis(meEvent.getStartTime());
					recurringPattern.setDayOfWeek(dayOfWeekCal.get(Calendar.DAY_OF_WEEK));
					//recurringPattern.setDayOfWeek(Integer.valueOf(meEvent.getDayOfWeek()));

					//recurringPattern.setDayOfWeek(dayOfWeekMap.get(meEvent.getDayOfWeek()));
					//recurringPattern.setDayOfWeek(startCal.get(Calendar.DAY_OF_WEEK));
					//System.out.println(startCal.get(Calendar.DAY_OF_WEEK));
					recurringPatterns.add(recurringPattern);
				}
			}
			
			List<RecurringEvent> recurringEvents = new ArrayList<>();
			for (Entry<String,RecurringEvent> entryMap : recurringEventMap.entrySet()) {
				RecurringEvent recurringEvent = recurringManager.saveRecurringEvent(entryMap.getValue());
				recurringEvents.add(recurringEvent);
				recurringEventToReturn = recurringEvent;
			}
			
			//Generating time slots for MeTimeEvents
			if (recurringEvents.size() > 0) {
				recurringManager.generateSlotsForRecurringEventList(recurringEvents);
			}
			
			responseObject.put("status", "success");
			responseObject.put("saved", "true");
			responseObject.put("recurringEvent", recurringEventToReturn);
			Long endTimeMillis = new Date().getTime();
			System.out.println("[METIME EVENTS TOTAL TIME TAKEN : ENDS "+(endTimeMillis - starTimeMillis)/1000+"]");
			System.out.println("[METIME EVENTS : ENDS]");

			return new ResponseEntity<Map<String, Object>>(responseObject,HttpStatus.OK);
		} catch(Exception e) {
			return new ResponseEntity<Map<String, Object>>(responseObject,HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(value = "/api/user/metime/deleteByRecurringId", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity deleteMeTimeByRecurringEventId(Long recurringEventId) {
		
		
		RecurringEvent recurringEvent = recurringManager.findByRecurringEventId(recurringEventId);
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		try {
			eventManager.deleteEventsByRecurringEventId(recurringEventId);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		try {
			eventTimeSlotManager.deleteEventTimeSlotsByRecurringEventId(recurringEventId);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		try {
			recurringManager.deleteRecurringPatternsByRecurringEventId(recurringEventId);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		try {
			recurringManager.deleteRecurringEventByRecurringEventId(recurringEventId);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		/*try{
			if (recurringEvent.getPhoto() != null) {
				String fileName = recurringEvent.getPhoto().substring(recurringEvent.getPhoto().lastIndexOf("/")+1, recurringEvent.getPhoto().length());
	    		File file = new File(recurringEventUploadPath+fileName);
	    		if(file.delete()){
	    			System.out.println(file.getName() + " is deleted!");
	    		}else{
	    			System.out.println("Delete operation is failed.");
	    		}
			}
    	} catch(Exception e){
    		
    		e.printStackTrace();
    		
    	}*/
		//recurringEvent.setCreatedById(null);
		//recurringManager.saveRecurringEvent(recurringEvent);
		return new ResponseEntity(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/api/user/getmetimes", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Map<String,Object>> getMeTimes(Long userId) {
		Map<String,Object> response = new HashMap<>();
		List<RecurringEvent> meTimeEvents = recurringManager.findRecurringEventsByCreatedById(userId);
		if (meTimeEvents != null && meTimeEvents.size() > 0) {
			for (RecurringEvent meTimeEvent : meTimeEvents) {
				if (meTimeEvent.getStartTime() != null) {
					Calendar metimeCalendar = Calendar.getInstance();
					metimeCalendar.setTime(meTimeEvent.getStartTime());
					for (RecurringPattern rp : meTimeEvent.getRecurringPatterns()) {
						metimeCalendar.set(Calendar.DAY_OF_WEEK, rp.getDayOfWeek());
						rp.setDayOfWeekTimestamp(metimeCalendar.getTimeInMillis());
					}
				}
			}
			response.put("data", meTimeEvents);
		} else {
			response.put("data", new ArrayList<>());
		}
		response.put("success", true);
		response.put("errorCode", 0);
		response.put("errorDetail", null);
		
		try {
			return new ResponseEntity<Map<String,Object>>(response,HttpStatus.OK);
		} catch(Exception e) {
			response.put("success", false);
			response.put("errorCode", 1);
			response.put("errorDetail", null);
			return new ResponseEntity<Map<String,Object>>(response,HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	@RequestMapping(value="/api/user/registerdevice",method=RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> registerDevice(@RequestBody UserDevice userDevice) {
		Map<String,Object> response = new HashMap<>();
		try {
			
			System.out.println("User Device : "+userDevice.toString());
			
			UserDevice savedUser = userService.findUserDeviceByDeviceTypeAndUserId(userDevice.getDeviceType(), userDevice.getUserId());
			if (savedUser != null) {
				savedUser.setDeviceToken(userDevice.getDeviceToken());
				savedUser.setModel(userDevice.getModel());
				savedUser.setManufacturer(userDevice.getManufacturer());
				savedUser.setVersion(userDevice.getVersion());
				userService.saveUserDeviceToken(savedUser);
			} else {
				userService.saveUserDeviceToken(userDevice);
			}
			
			response.put("success", true);
			response.put("output","registred");
			response.put("errorCode",0);
			response.put("errorDetail",null);
		} catch(Exception e){
			e.printStackTrace();
			response.put("success", false);
			response.put("output", e.getMessage());
			response.put("errorCode",ErrorCodes.InternalServerError.ordinal());
			response.put("errorDetail",ErrorCodes.InternalServerError.toString());
		}
		return new ResponseEntity<Map<String,Object>>(response,HttpStatus.OK);
	}
	
	@RequestMapping(value="/api/user/logout")
	public ResponseEntity<Map<String, Object>> registerDevice(Long userId,String deviceType) {
		Map<String,Object> response = new HashMap<>();
		try {
			
			userService.deleteUserDeviceByUserIdAndDeviceType(userId, deviceType.toLowerCase());
			response.put("success", true);
			response.put("output","Logout Successfully");
			response.put("errorCode",0);
			response.put("errorDetail",null);
		} catch(Exception e){
			e.printStackTrace();
			response.put("success", false);
			response.put("output", e.getMessage());
			response.put("errorCode",ErrorCodes.InternalServerError.ordinal());
			response.put("errorDetail",ErrorCodes.InternalServerError.toString());
		}
		return new ResponseEntity<Map<String,Object>>(response,HttpStatus.OK);
	}
	
	@RequestMapping(value = "/auth/forgetPassword", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> getForgetPassword(@RequestParam("email") String email) {
		
		User user = null;
		Map<String, Object> response = new HashMap<>();
		try {
			if (email != null && !email.isEmpty()) {
				user = userService.findUserByEmail(email);
				if (user != null) {
					
					String resetPasswordToken = CenesUtils.getAlphaNumeric(40);
					user.setResetToken(resetPasswordToken);
					user.setResetTokenCreatedAt(new Date());
					user = userService.saveUser(user);
					
					emailManager.sendForgotPasswordLink(user);
					response.put("success", true);
				} else {
					response.put("success", false);
					response.put("errorDetail", "Email does not exist");
					response.put("message", "Email does not exist");
				}
				response.put("errorCode", 0);
			} else {
				response.put("success", false);
				response.put("errorCode", 0);
				response.put("errorDetail", null);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("errorCode", HttpStatus.INTERNAL_SERVER_ERROR.ordinal());
			response.put("errorDetail", HttpStatus.INTERNAL_SERVER_ERROR.toString());
			response.put("message", HttpStatus.INTERNAL_SERVER_ERROR.toString());

		}
		return new ResponseEntity<Map<String,Object>>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/auth/forgetPassword/v2", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> getForgetPasswordV2(String email) {
		
		User user = null;
		Map<String, Object> response = new HashMap<>();
		try {
			if (email != null && !email.isEmpty()) {
				user = userService.findUserByEmail(email);
				if (user != null) {
					
					String resetPasswordToken = CenesUtils.getAlphaNumeric(40);
					user.setResetToken(resetPasswordToken);
					user.setResetTokenCreatedAt(new Date());
					user = userService.saveUser(user);
					response.put("success", true);
				} else {
					response.put("success", false);
					response.put("errorDetail", "Email does not exist");
					response.put("message", "Email does not exist");
				}
				response.put("errorCode", 0);
			} else {
				response.put("success", false);
				response.put("errorCode", 0);
				response.put("errorDetail", null);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("errorCode", HttpStatus.INTERNAL_SERVER_ERROR.ordinal());
			response.put("errorDetail", HttpStatus.INTERNAL_SERVER_ERROR.toString());
			response.put("message", HttpStatus.INTERNAL_SERVER_ERROR.toString());

		}
		return new ResponseEntity<Map<String,Object>>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/auth/forgetPassword/v2/sendEmail", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> sendForgetPasswordEmail(String email) {
		
		User user = null;
		Map<String, Object> response = new HashMap<>();
		try {
			if (email != null && !email.isEmpty()) {
				user = userService.findUserByEmail(email);
				if (user != null) {
					
					emailManager.sendForgotPasswordConfirmationLink(user);
					response.put("success", true);
				} else {
					response.put("success", false);
					response.put("errorDetail", "Email does not exist");
					response.put("message", "Email does not exist");
				}
				response.put("errorCode", 0);
			} else {
				response.put("success", false);
				response.put("errorCode", 0);
				response.put("errorDetail", null);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("errorCode", HttpStatus.INTERNAL_SERVER_ERROR.ordinal());
			response.put("errorDetail", HttpStatus.INTERNAL_SERVER_ERROR.toString());
			response.put("message", HttpStatus.INTERNAL_SERVER_ERROR.toString());

		}
		return new ResponseEntity<Map<String,Object>>(response, HttpStatus.OK);
	}
	
	
	@RequestMapping(value = "/auth/forgetPasswordConfirmation", method = RequestMethod.GET)
	public ResponseEntity<Object> forgetPasswordConfirmationLinRequest(String resetToken, HttpServletRequest request) {
		
		User user = null;
		Map<String, Object> response = new HashMap<>();
		try {
			
			user = userService.findUserByResetToken(resetToken);
			
			if (user == null) {
				response.put("success", false);
				response.put("message", "Invalid Reset Token");
				String url = request.getScheme()+"://thankyou.html?success=false";
				
			    URI yahoo = new URI(url);
			    HttpHeaders httpHeaders = new HttpHeaders();
			    httpHeaders.setLocation(yahoo);
			    return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);
				//return response.toString();
				
			}

			user.setResetToken(null);
			userService.saveUser(user);
			response.put("success", true);
			response.put("message", "Email Confirmed Successfully");

			String userAgent = request.getHeader("User-Agent");
			System.out.println("User Agent : "+userAgent);
			if (userAgent.toLowerCase().indexOf("iphone") != -1 || userAgent.toLowerCase().indexOf("ipad") != -1  || userAgent.toLowerCase().indexOf("android") != -1 || 
					userAgent.toLowerCase().indexOf("blackberry") != -1 || userAgent.toLowerCase().indexOf("nokia") != -1 || userAgent.toLowerCase().indexOf("opera mini") != -1 || 
					userAgent.toLowerCase().indexOf("windows mobile") != -1 || userAgent.toLowerCase().indexOf("windows phone") != -1 || userAgent.toLowerCase().indexOf("iemobile") != -1 ) {
				
				String url = domain+"://thankyou.html?success=true";
				
			    URI yahoo = new URI(url);
			    HttpHeaders httpHeaders = new HttpHeaders();
			    httpHeaders.setLocation(yahoo);
			    return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);
				
			} else {
			    return new ResponseEntity<>(response, HttpStatus.OK);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("errorCode", HttpStatus.INTERNAL_SERVER_ERROR.ordinal());
			response.put("errorDetail", HttpStatus.INTERNAL_SERVER_ERROR.toString());
			response.put("message", HttpStatus.INTERNAL_SERVER_ERROR.toString());

		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	/**
	 * This API is to reset the password when user request from forget password
	 * 
	 * */
	@RequestMapping(value = "/auth/updatePassword", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> updatePassword(@RequestBody Map<String, Object> requestBody) {
		
		System.out.println(requestBody.toString());
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		
		User user = null;
		if (!requestBody.containsKey("password")) {
			response.put("success", false);
			response.put("message", "Password is missing");
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
		}
		
		if (requestBody.containsKey("requestFrom") && requestBody.get("requestFrom").toString().equals("App")) {
			
			String email = requestBody.get("email").toString();
			user = userRepository.findByEmailAndResetTokenIsNull(email);
			
			if (user == null) {
				response.put("success", false);
				response.put("message", "Please click confirmation link in Email");
				return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
			}

		} else {
			if (!requestBody.containsKey("resetToken")) {
				response.put("success", false);
				response.put("message", "ResetToken is missing");
				return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
			}
			
			String resetToken = requestBody.get("resetToken").toString();
			user = userService.findUserByResetToken(resetToken);
			
			if (user == null) {
				response.put("success", false);
				response.put("message", "Invalid Reset Token");
				return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
			}
		}
		
		try {
			user.setPassword(new Md5PasswordEncoder().encodePassword(requestBody.get("password").toString(), salt));
			user = userService.saveUser(user);
			response.put("success", true);
		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("data", null);
			response.put("errorCode", HttpStatus.INTERNAL_SERVER_ERROR.ordinal());
			response.put("errorDetail", HttpStatus.INTERNAL_SERVER_ERROR.toString());
		}
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/api/user/changePassword", method = RequestMethod.POST)
	public Map<String, Object> changePassword(@RequestBody ChangePasswordDto changePasswordDto) {
		
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		
		User user = userService.findUserById(changePasswordDto.getUserId());
		if (user == null) {
			response.put("success", false);
			response.put("message", "User does not exists.");
			return response;
		}
		
		String currentPassword = changePasswordDto.getCurrentPassword();
		
		if (!user.getPassword().equals(new Md5PasswordEncoder().encodePassword(currentPassword, salt))) {
			response.put("success", false);
			response.put("message", "Current Password is not correct.");
			return response;
		}
		
		//Now lets create new password and update it.
		String newPassword = new Md5PasswordEncoder().encodePassword(changePasswordDto.getNewPassword(), salt);
		user.setPassword(newPassword);
		user = userService.saveUser(user);
		response.put("success", true);
		response.put("data", user);
		return response;
	}
	
	
	@RequestMapping(value = "/api/user/updateDetails", method = RequestMethod.POST)
	public Map<String, Object> updateUserDetails(@RequestBody Map<String, Object> updateUserDetails) {
		
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		
		Long userId = Long.valueOf(updateUserDetails.get("userId").toString());
		
		if (updateUserDetails.containsKey("username")) {
			String userName =  updateUserDetails.get("username").toString();
			
			userService.updateNameByUserId(userName, userId);
		} else if (updateUserDetails.containsKey("gender")) {
			String gender =  updateUserDetails.get("gender").toString();
			userService.updateGenderByUserId(gender, userId);
		} else if (updateUserDetails.containsKey("birthDayStr")) {
			String birthDayStr =  updateUserDetails.get("birthDayStr").toString();
			userService.updateBirthDayByUserId(birthDayStr, userId);
		} else if (updateUserDetails.containsKey("newPassword")) {
			
			String newPassword = updateUserDetails.get("newPassword").toString();
			//Now lets create new password and update it.
			String newPass = new Md5PasswordEncoder().encodePassword(newPassword, salt);
			userService.updatePasswordByUserId(newPass, userId);
		} else if (updateUserDetails.containsKey("profilePic")) {
			String profilePic =  updateUserDetails.get("profilePic").toString();
			userService.updateProfilePicByUserId(profilePic, userId);
		}
		
		response.put("success", true);
		return response;
	}
	
	@RequestMapping(value = "/api/user/holidayCalendar", method = RequestMethod.POST)
	public Map<String, Object> saveHolidayCalendar(@RequestBody HolidayCalendar holidayCalendar) {
		
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		
		User user = userService.findUserById(holidayCalendar.getUserId());
		
		
		eventManager.deleteEventsByCreatedByIdScheduleAs(holidayCalendar.getUserId(), Event.ScheduleEventAs.Holiday.toString());
		
		//Find the holiday calendar is any in db and update it.
		HolidayCalendar dbHolidayCalendar = null;
		List<HolidayCalendar> holidayCaledars = userService.findHolidayCalendarByUserId(holidayCalendar.getUserId());
		if (holidayCaledars != null && holidayCaledars.size() > 0) {
			dbHolidayCalendar = holidayCaledars.get(0);
		}
		
		if (dbHolidayCalendar != null) {
			dbHolidayCalendar.setCountryCalendarId(holidayCalendar.getCountryCalendarId());
			dbHolidayCalendar.setCountryName(holidayCalendar.getCountryName());
			dbHolidayCalendar.setCountryCode(holidayCalendar.getCountryCode());

			userService.saveHolidayCalendar(dbHolidayCalendar);		
			eventManager.syncHolidays(dbHolidayCalendar.getCountryCalendarId(), user);
			
		} else {
			userService.saveHolidayCalendar(holidayCalendar);		
			eventManager.syncHolidays(holidayCalendar.getCountryCalendarId(), user);
		
		}
		holidayCalendar = userService.saveHolidayCalendar(holidayCalendar);
		if (holidayCalendar == null) {
			response.put("success", false);
			response.put("message", "HolidayCalendar cannot be saved.");
			return response;
		}
		
		response.put("data", holidayCalendar);
		return response;
	}
	
	@RequestMapping(value = "/api/user/holidayCalendarByUserId")
	public Map<String, Object> findHolidayCalendarByUserId(Long userId) {
		
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		
		List<HolidayCalendar> holidayCalendars = userService.findHolidayCalendarByUserId(userId);
		if (holidayCalendars == null) {
			response.put("success", false);
			response.put("message", "No Holiday Calendar Selected.");
			return response;
		}
		response.put("data", holidayCalendars);
		return response;
	}
	
	
	@RequestMapping(value = "/auth/validateResetToken", method = RequestMethod.GET)
	public Map<String, Object> validateResetToken(String resetToken) {
		try {
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			User user = userService.findUserByResetToken(resetToken);
			if (user == null) {
				response.put("success", false);
				response.put("message", "Reset Password Link Expired");
				return response;
			}
			return response;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/auth/responseFromOutlook", method = RequestMethod.GET)
	public Map<String, Object> responeFromOutlook(HttpServletRequest request) {
		try {
			System.out.println(request.getParameterMap().toString());
			System.out.println(request.getQueryString());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	@RequestMapping(value = "/api/user/validatePassword", method = RequestMethod.POST)
	public Map<String, Object> validatePassword(@RequestBody ChangePasswordDto changePasswordDto) {
		try {
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			
			User user = userService.findUserById(changePasswordDto.getUserId());
			if (user == null) {
				response.put("success", false);
				response.put("message", "User does not exists.");
				return response;
			}
			
			String currentPassword = changePasswordDto.getCurrentPassword();
			
			if (!user.getPassword().equals(new Md5PasswordEncoder().encodePassword(currentPassword, salt))) {
				response.put("success", false);
				response.put("message", "Wrong Password.");
				return response;
			}
			
			response.put("success", true);
			response.put("data", user);
			return response;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@RequestMapping(value = "/api/user/phonefriends", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<UserContact>> getUserPhoneFriends(@RequestParam("user_id") Long userId) {
		List<UserContact> friends = (List)userContactRepository.findByUserIdOrderByNameAsc(userId);
		return new ResponseEntity<List<UserContact>>(friends,HttpStatus.OK);
	}
	
	@RequestMapping(value = "/api/user/phonefriends/v2", method = RequestMethod.GET)
	public Map<String, Object> getUserPhoneFriendsList(Long userId) {
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		
		List<UserContact> friends = (List)userContactRepository.findByUserIdOrderByNameAsc(userId);
		response.put("data", friends);
		return response;
	}
	
	@RequestMapping(value = "/api/syncContacts", method = RequestMethod.POST)
	public void syncUserContacts(@RequestBody Map<String,Object> contactsMap) {
		userService.syncPhoneContacts(contactsMap);
		
		Long userId = Long.valueOf(contactsMap.get("userId").toString());
		if (userId != null) {
			UserStat userStat = userService.findUserStatByUserId(userId);
			if (userStat == null) {
				userStat = new UserStat();
			}
			
			Long cenesMemberCounts = userService.findCenesMemberCountsByUserId(userId);
			
			userStat.setCenesMemberCounts(cenesMemberCounts);
			userService.saveUpdateUserStat(userStat);
		}
	}
	
	@RequestMapping(value = "/api/guest/sendVerificationCode", method = RequestMethod.POST)
	public ResponseEntity<?> sendPhoneVerificationCode(@RequestBody Map<String,String> phoneMap) {
		
		/*Map<String, Object> phoneExistingMap = new HashMap<>();
		phoneExistingMap.put("success", false);
		phoneExistingMap.put("message","Phone Number Already Exists");
		List<User> users = userRepository.findByPhoneContaining(phoneMap.get("phone").toString());
		if (users != null && users.size() > 0) {
			return new ResponseEntity<>(phoneExistingMap, HttpStatus.OK);
		}*/
		
		TwilioService ts = new TwilioService();
		Map<String, Object> response = ts.sendVerificationCode(phoneMap.get("countryCode").toString(), phoneMap.get("phone").toString());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/api/guest/checkVerificationCode", method = RequestMethod.POST)
	public ResponseEntity<?> checkPhoneVerificationCode(@RequestBody Map<String,String> phoneMap) {
		TwilioService ts = new TwilioService();
		Map<String, Object> response = ts.checkVerificationCode(phoneMap.get("countryCode").toString(), phoneMap.get("phone").toString(), phoneMap.get("code").toString());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/api/guest/users", method = RequestMethod.GET)
	public ResponseEntity<?> getAllCenesUsers() {
		List<User> users = userService.findAllUsers();
		Map<String, Object> response = new HashMap<>();
		response.put("data", users);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/api/guest/findUserByEmailOrPhone", method = RequestMethod.GET)
	public User findUserByEmailOrPhone(String emailOrPhone) {
		if (emailOrPhone.contains("@")) {
			return this.userService.findUserByEmail(emailOrPhone);
		} else {
			String phone = emailOrPhone.replaceAll("\\s", "").replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("-", "");
			return this.userService.findUserByPhone(phone);
		}
	}
	
	@RequestMapping(value = "/api/deleteUserByEmail", method = RequestMethod.GET)
	public Map<String, Object> deleteUserByEmail(String email) {
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("message", "User Deleted SuccessFully");
		
		User user = userService.findUserByEmail(email);
		if (user == null) {
			response.put("success", false);
			response.put("message", "User not found");
			return response;
		}
		
		//eventManager.deleteEventsByCreatedById(user.getUserId());
		//eventTimeSlotManager.deleteEventTimeSlotsByUserId(user.getUserId());
		eventServiceDao.deleteEventTimeSlotsAndEventsByCreatedById(user.getUserId());
		recurringManager.deleteRecurringEventsByUserId(user.getUserId());
		userService.updateContactsByFriendIdAndUserId(null, user.getPhone().substring(4, user.getPhone().length()));
		userService.deleteContactsByUserId(user.getUserId());
		//eventTimeSlotManager.deleteEventTimeSlotsByUserId(user.getUserId());
		//recurringManager.deleteRecurringEventsByUserId(user.getUserId());
		userService.deleteCalendarSyncTokensByUserId(user.getUserId());
		userService.deleteUserDeviceByUserId(user.getUserId());
		userService.deleteUserByUserId(user.getUserId());
		return response;
	}
	
	@RequestMapping(value = "/api/deleteUserByPhonePassword", method = RequestMethod.POST)
	public Map<String, Object> deleteUserByPhoneAndPassword(@RequestBody User user) {
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("message", "User Deleted SuccessFully");
		
		System.out.println("User : "+user.toString());
		
		try {
			List<User> usersToDelete = userRepository.findListByPhone(user.getPhone());
			if (usersToDelete == null || usersToDelete.size() == 0) {
				response.put("success", false);
				response.put("message", "User not found");
				return response;
			} else {					
					
				for (User userByPhone: usersToDelete) {
					
					userService.updateContactsByFriendIdAndUserId(null, userByPhone.getPhone().substring(4, userByPhone.getPhone().length()));
					
					if (user.getPassword() != null) {
						System.out.println(new Md5PasswordEncoder().encodePassword(user.getPassword(), salt));
						if (userByPhone.getPassword() != null && !userByPhone.getPassword().equals(new Md5PasswordEncoder().encodePassword(user.getPassword(), salt))) {
							response.put("success", false);
							response.put("message", "Incorrect Password");
							return response;
						}
					}
					
					
					//eventManager.deleteEventsByCreatedById(user.getUserId());
					userService.deleteContactsByUserId(userByPhone.getUserId());
					//eventTimeSlotManager.deleteEventTimeSlotsByUserId(user.getUserId());
					//recurringManager.deleteRecurringEventsByUserId(user.getUserId());
					userService.deleteCalendarSyncTokensByUserId(userByPhone.getUserId());
					userService.deleteUserDeviceByUserId(userByPhone.getUserId());
					userService.deleteUserByUserId(userByPhone.getUserId());
				}
				
			}
			
			
		} catch(Exception e) {
			e.printStackTrace();
			
			response.put("success", false);
			response.put("message", e.getMessage());

			
		}
		
		return response;
	}
	
	
	@RequestMapping(value = "/api/user/userStatsByUserId", method = RequestMethod.GET)
	public Map<String, Object> findUserStatsByUserId(Long userId) {
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		
		UserStat userStat = userService.findUserStatByUserId(userId);
		if (userStat == null) {
			userStat = new UserStat();
		}
		
		response.put("data", userStat);
		response.put("message", "User not found");
		return response;
	}
	
	@RequestMapping(value = "/api/user/syncDetails", method = RequestMethod.GET)
	public Map<String, Object> findUserSyncDetailsByUserId(Long userId) {
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		
		List<CalendarSyncToken> syncTokens = userService.fincSyncTokensByUserId(userId);
		if (syncTokens == null) {
			syncTokens = new ArrayList<CalendarSyncToken>();
		}
		
		response.put("data", syncTokens);
		return response;
		
	}
	
	@RequestMapping(value = "/api/user/deleteSyncBySyncId", method = RequestMethod.DELETE)
	public Map<String, Object> deleteSyncTokenBySyncTokenId(Long calendarSyncTokenId) {
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		
		CalendarSyncToken calendarSyncToken = eventManager.findByCalendarSyncTokenId(calendarSyncTokenId);
		if (calendarSyncToken.getAccountType() == AccountType.Google) {
			/*GoogleService gs = new GoogleService();
			try {
				gs.unsubscribeGoogleNotification(calendarSyncToken);

			} catch(Exception e) {
				System.out.println("Exception Google : "+e.getMessage());
			}*/
			
			eventManager.deleteEventTimeSlotsByCreatedByIdAndSourceAndScheduleAs(calendarSyncToken.getUserId(), Event.EventSource.Google.toString(), Event.ScheduleEventAs.Event.toString());
			List<Event> events = eventManager.findEventsByCreatedByIdAndSourceAndScheduleAs(calendarSyncToken.getUserId(), Event.EventSource.Google.toString(), Event.ScheduleEventAs.Event.toString());
			//eventManager.runEventDeleteThread(events);
			eventManager.deleteEventBatch(events);
			
		} else if (calendarSyncToken.getAccountType() == AccountType.Outlook) {
			try {
				
				eventManager.deleteEventTimeSlotsByCreatedByIdAndSourceAndScheduleAs(calendarSyncToken.getUserId(), Event.EventSource.Outlook.toString(), Event.ScheduleEventAs.Event.toString());
				List<Event> events = eventManager.findEventsByCreatedByIdAndSourceAndScheduleAs(calendarSyncToken.getUserId(), Event.EventSource.Outlook.toString(), Event.ScheduleEventAs.Event.toString());
				//eventManager.runEventDeleteThread(events);
				eventManager.deleteEventBatch(events);

			} catch(Exception e) {
				System.out.println("Exception Outlook : "+e.getMessage());
			}
			
		} else if (calendarSyncToken.getAccountType() == AccountType.Apple) {
			try {
				
				eventManager.deleteEventTimeSlotsByCreatedByIdAndSourceAndScheduleAs(calendarSyncToken.getUserId(), Event.EventSource.Apple.toString(), Event.ScheduleEventAs.Event.toString());
				List<Event> events = eventManager.findEventsByCreatedByIdAndSourceAndScheduleAs(calendarSyncToken.getUserId(), Event.EventSource.Apple.toString(), Event.ScheduleEventAs.Event.toString());
				//eventManager.runEventDeleteThread(events);
				eventManager.deleteEventBatch(events);

			} catch(Exception e) {
				System.out.println("Exception Apple Event : "+e.getMessage());
			}
		}
		
		userService.deleteCalendarSyncTokenByCalendarSyncTokenId(calendarSyncTokenId);
		//calendarSyncToken.setIsActive(CalendarSyncToken.ActiveStatus.InActive);
		return response;
		
	}

	@RequestMapping(value = "/auth/getCountryByIpAddress", method = RequestMethod.GET)
	public Map<String, Object> getCountryByIpAddress(String ipAddress) {
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		
		Country country = geoLoactionManager.getLocation(ipAddress);
		if (country != null) {
			response.put("data", country);
		} else {
			response.put("data", null);
			response.put("success", false);
		}
		
		return response;
		
	}
	
	@Autowired
	TokenAuthenticationService tokenAuthenticationService;

	private String establishUserAndLogin(HttpServletResponse response,
			User email) {
		// Find user, create if necessary
		org.springframework.security.core.userdetails.User user;

		user = new org.springframework.security.core.userdetails.User(
				email.getUsername(), UUID.randomUUID().toString(),
				Sets.<GrantedAuthority> newHashSet());
		// userService.addUser(user);
		com.cg.stateless.security.UserAuthentication authentication = new com.cg.stateless.security.UserAuthentication(
				user, email);
		String token = tokenAuthenticationService.addAuthentication(response,
				authentication);
		return token;
	}
	
	
	public String getUserToken(User user) {
		user.setUsername(user.getUsername());
		user.setPassword(new Md5PasswordEncoder().encodePassword(user.getPassword(), salt));
		return establishUserAndLogin(null, user);
	}
	
}
