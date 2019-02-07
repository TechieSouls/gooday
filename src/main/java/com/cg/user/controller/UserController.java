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
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.codec.binary.Base64;
import org.primefaces.json.JSONArray;
import org.primefaces.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import com.cg.bo.CenesProperty.PropertyOwningEntity;
import com.cg.bo.CenesPropertyValue;
import com.cg.bo.FacebookProfile;
import com.cg.bo.UserFriend;
import com.cg.bo.UserFriend.UserStatus;
import com.cg.constant.CgConstants.ErrorCodes;
import com.cg.dto.ChangePasswordDto;
import com.cg.enums.CgEnums.AuthenticateType;
import com.cg.events.bo.MeTime;
import com.cg.events.bo.MeTimeEvent;
import com.cg.events.bo.RecurringEvent;
import com.cg.events.bo.RecurringEvent.RecurringEventProcessStatus;
import com.cg.events.bo.RecurringEvent.RecurringEventStatus;
import com.cg.events.bo.RecurringPattern;
import com.cg.manager.EmailManager;
import com.cg.manager.EventManager;
import com.cg.manager.EventTimeSlotManager;
import com.cg.manager.RecurringManager;
import com.cg.repository.CenesPropertyValueRepository;
import com.cg.repository.UserContactRepository;
import com.cg.repository.UserFriendRepository;
import com.cg.repository.UserRepository;
import com.cg.service.FacebookService;
import com.cg.service.OutlookService;
import com.cg.service.TwilioService;
import com.cg.service.UserService;
import com.cg.stateless.security.TokenAuthenticationService;
import com.cg.user.bo.User;
import com.cg.user.bo.UserContact;
import com.cg.user.bo.UserContact.CenesMember;
import com.cg.user.bo.UserDevice;
import com.cg.utils.CenesUtils;
import com.google.common.collect.Sets;

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
	CenesPropertyValueRepository cenesPropertyValueRepository;
	
	@Value("${cenes.imageUploadPath}")
	private String imageUploadPath;
	
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
			if (user.getFacebookID() != null) {
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
			if (userInfo == null) {
				System.out.println("[ Date : "+new Date()+" ] ,UserType : Email, Message : New signup request");
				try {
					user.setUsername(user.getName().toLowerCase().replaceAll(" ",".")+System.currentTimeMillis());
					user.setPassword(new Md5PasswordEncoder().encodePassword(user.getPassword(), salt));
					user.setToken(establishUserAndLogin(httpServletResponse, user));
					userInfo = userService.saveUser(user);
					
					List<UserContact> userContactInOtherContacts = userContactRepository.findByPhone(user.getPhone());
					if (userContactInOtherContacts != null && userContactInOtherContacts.size() > 0) {
						for (UserContact userContact : userContactInOtherContacts) {
							userContact.setCenesMember(CenesMember.yes);
							userContact.setFriendId(user.getUserId());
						}
						userContactRepository.save(userContactInOtherContacts);
					}
					
				} catch(Exception e) {
					e.printStackTrace();
					user.setPassword(null);
					user.setErrorCode(ErrorCodes.EmailAlreadyTaken.getErrorCode());
					user.setErrorDetail(ErrorCodes.EmailAlreadyTaken.toString());
					return new ResponseEntity<User>(user, HttpStatus.OK);
				}
			} else {
				user.setPassword(null);
				user.setErrorCode(ErrorCodes.EmailAlreadyTaken.getErrorCode());
				user.setErrorDetail(ErrorCodes.EmailAlreadyTaken.toString());
				System.out.println("[ Date : "+new Date()+" ] ,UserType : Email, Message : Email Already Exists");
				
				return new ResponseEntity<User>(user, HttpStatus.OK);
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
			if (user.getFacebookID() == null) {
				user.setErrorCode(ErrorCodes.FacebookIdNotPresent.getErrorCode());
				user.setErrorDetail(ErrorCodes.FacebookIdNotPresent.toString());
				return new ResponseEntity<User>(user, HttpStatus.PARTIAL_CONTENT);
			}
			
			System.out.println("[ Date : "+new Date()+" ] ,UserType : Facebook, Message : Facebook Type User");
			userInfo = userRepository.findUserByFacebookID(user.getFacebookID());
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

	@ResponseBody
	@RequestMapping(value="/api/user/update/", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> updateUser(@RequestBody User user) {
		
		User dbUser = userService.findUserById(user.getUserId());
		dbUser.setName(user.getName());
		dbUser.setEmail(user.getEmail());
		if (user.getPhoto() != null && user.getPhoto().length() > 0) {
			dbUser.setPhoto(user.getPhoto());
		}
		dbUser.setGender(user.getGender());
		
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
           
            String profilePicUrl = "http://"+domain+"/assets/uploads/"+userId+"/profile/"+fileName;
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
		
		if (meTime != null && meTime.getRecurringEventId() != null) {
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
				
					List<RecurringPattern> recurringPatterns = new ArrayList<>();
					RecurringPattern recurringPattern = new RecurringPattern();
					recurringPattern.setRecurringEventId(recurringEvent.getRecurringEventId());
					//recurringPattern.setDayOfWeek(startCal.get(Calendar.DAY_OF_WEEK));
					//System.out.println(startCal.get(Calendar.DAY_OF_WEEK));
					recurringPattern.setDayOfWeek(dayOfWeekMap.get(meEvent.getDayOfWeek()));
					recurringPatterns.add(recurringPattern);
					
					recurringEvent.setRecurringPatterns(recurringPatterns);
					
					recurringEventMap.put(meEvent.getTitle(), recurringEvent);
				} else {
					RecurringEvent recurringEvent = recurringEventMap.get(meEvent.getTitle());
					List<RecurringPattern> recurringPatterns = recurringEvent.getRecurringPatterns();
					RecurringPattern recurringPattern = new RecurringPattern();
					recurringPattern.setRecurringEventId(recurringEvent.getRecurringEventId());
					recurringPattern.setDayOfWeek(dayOfWeekMap.get(meEvent.getDayOfWeek()));
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
		
		try{
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
    		
    	}
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
			
			UserDevice savedUser = userService.findUserDeviceByDeviceTypeAndUserId(userDevice.getDeviceType(), userDevice.getUserId());
			if (savedUser != null) {
				savedUser.setDeviceToken(userDevice.getDeviceToken());
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
		}
		return new ResponseEntity<Map<String,Object>>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/auth/updatePassword/{email}/{password}", method = RequestMethod.GET)
	public ResponseEntity<User> updatePassword(@PathVariable("email") String email,
			@PathVariable("password") String newPassword, User user) {
		Map<String, Object> response = new HashMap<>();
		try {
			if (email != null && !email.isEmpty()) {
				user = userService.findUserByEmail(email);
				if (user != null) {
					user.setPassword(new Md5PasswordEncoder().encodePassword(newPassword, salt));
					user = userService.saveUser(user);
					response.put("success", true);
					response.put("data", user);
				} else {
					response.put("success", false);
					response.put("data", null);
				}
				response.put("errorCode", 0);
				response.put("errorDetail", null);

			} else {
				response.put("success", false);
				response.put("data", null);
				response.put("errorCode", 0);
				response.put("errorDetail", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("data", null);
			response.put("errorCode", HttpStatus.INTERNAL_SERVER_ERROR.ordinal());
			response.put("errorDetail", HttpStatus.INTERNAL_SERVER_ERROR.toString());
		}
		ResponseEntity<User> responseEntity = new ResponseEntity<User>(user, HttpStatus.OK);
		return responseEntity;
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
		String newPassword = new Md5PasswordEncoder().encodePassword(changePasswordDto.getCurrentPassword(), salt);
		user.setPassword(newPassword);
		user = userService.saveUser(user);
		response.put("success", true);
		response.put("data", user);
		return response;
	}
	
	
	@RequestMapping(value = "/auth/validateResetToken", method = RequestMethod.GET)
	public User validateResetToken(String resetToken) {
		try {
			User user = userService.findUserByResetToken(resetToken);
			return user;
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
	
	@RequestMapping(value = "/api/syncContacts", method = RequestMethod.POST)
	public void syncUserContacts(@RequestBody Map<String,Object> contactsMap) {
		userService.syncPhoneContacts(contactsMap);
	}
	
	@RequestMapping(value = "/api/guest/sendVerificationCode", method = RequestMethod.POST)
	public ResponseEntity<?> sendPhoneVerificationCode(@RequestBody Map<String,String> phoneMap) {
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
		
		eventManager.deleteEventsByCreatedById(user.getUserId());
		userService.deleteContactsByUserId(user.getUserId());
		userService.updateContactsByFriendIdAndUserId(null, user.getPhone());
		eventTimeSlotManager.deleteEventTimeSlotsByUserId(user.getUserId());
		recurringManager.deleteRecurringEventsByUserId(user.getUserId());
		userService.deleteUserDeviceByUserId(user.getUserId());
		userService.deleteUserByUserId(user.getUserId());
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
}
