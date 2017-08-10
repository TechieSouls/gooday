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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletResponse;

import okhttp3.internal.framed.ErrorCode;

import org.apache.tomcat.util.codec.binary.Base64;
import org.primefaces.json.JSONArray;
import org.primefaces.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
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

import com.cg.bo.FacebookProfile;
import com.cg.constant.CgConstants.ErrorCodes;
import com.cg.enums.CgEnums.AuthenticateType;
import com.cg.repository.UserAvailabilityRepository;
import com.cg.repository.UserRepository;
import com.cg.service.FacebookService;
import com.cg.stateless.security.TokenAuthenticationService;
import com.cg.user.bo.User;
import com.cg.user.bo.UserAvailability;
import com.google.common.collect.Sets;

@RestController
@Api(value = "User", description = "Create User data")
public class UserController {

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	UserAvailabilityRepository userAvailabilityRepository;


	@Value("${cenes.imageUploadPath}")
	private String imageUploadPath;
	
	@Value("${cenes.domain}")
	private String domain;
	
	@Value("${cenes.salt}")
	private String salt;
	
	/*
	 * { "username":"Blue", "password":200, "name":"1234" }
	 */

	@ApiOperation(value = "Create user", notes = "create user ", code = 200, httpMethod = "POST", produces = "application/json")
	@ModelAttribute(value = "user")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "product updated successfuly") })
	@RequestMapping(value = "/api/users/", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<User> createUser(
			HttpServletResponse httpServletResponse,
			@ApiParam(name = "User", value = "user dummy data", required = true) @RequestBody User user) {

		if (user.getFacebookID() != null) {
			if (userRepository.findUserByFacebookID(user.getFacebookID()) != null) {
				user.setErrorCode(ErrorCodes.UserNameOrEmailAlreadyTaken
						.getErrorCode());
				user.setErrorDetail(ErrorCodes.UserNameOrEmailAlreadyTaken
						.toString());
				return new ResponseEntity<User>(user, HttpStatus.BAD_REQUEST);
			}

		}
		if (user.getPassword() != null) {
			user.setPassword(new Md5PasswordEncoder().encodePassword(
					user.getPassword(), salt));
		}
		if (user.getEmail() != null) {

			if (userRepository.findUserByEmail(user.getEmail()) != null) {
				user.setErrorCode(ErrorCodes.EmailAlreadyTaken
						.getErrorCode());
				user.setErrorDetail(ErrorCodes.EmailAlreadyTaken
						.toString());
				return new ResponseEntity<User>(user, HttpStatus.BAD_REQUEST);
			}
		}
		/*if (user.getUsername() != null) {

			if (userRepository.findUserByUsername(user.getUsername()) != null) {
				user.setErrorCode(ErrorCodes.UserNameOrEmailAlreadyTaken
						.getErrorCode());
				user.setErrorDetail(ErrorCodes.UserNameOrEmailAlreadyTaken
						.toString());
				return new ResponseEntity<User>(user, HttpStatus.BAD_REQUEST);
			}
		}*/

		if (user.getAuthType() == AuthenticateType.email) {
			if (user.getEmail() == null || user.getEmail().trim().length() < 6) {
				user.setErrorCode(ErrorCodes.EmailLenghtMustBeSixCharater
						.getErrorCode());
				user.setErrorDetail(ErrorCodes.EmailLenghtMustBeSixCharater
						.toString());
				return new ResponseEntity<User>(user, HttpStatus.BAD_REQUEST);
			}
			if (user.getName() == null || user.getName().trim().length() < 3) {
				user.setErrorCode(ErrorCodes.NameLenghtMustBeSixCharater
						.getErrorCode());
				user.setErrorDetail(ErrorCodes.NameLenghtMustBeSixCharater
						.toString());
				return new ResponseEntity<User>(user, HttpStatus.BAD_REQUEST);
			}
			if (user.getPassword() == null
					|| user.getPassword().trim().length() < 6) {
				user.setErrorCode(ErrorCodes.PasswordLenghtMustBeSixCharater
						.getErrorCode());
				user.setErrorDetail(ErrorCodes.PasswordLenghtMustBeSixCharater
						.toString());
				return new ResponseEntity<User>(user, HttpStatus.BAD_REQUEST);
			}
			
			if (user.getUsername() == null) {
				user.setUsername(user.getName().toLowerCase().replaceAll(" ",".")+System.currentTimeMillis());
			}
			
			try {
				// new Md5PasswordEncoder().encodePassword(user.getPassword(),
				// user.getUsername());
				user.setToken(establishUserAndLogin(httpServletResponse, user));
				user = userRepository.save(user);
			} catch (DataIntegrityViolationException e) {
				user.setErrorCode(ErrorCodes.EmailAlreadyTaken
						.getErrorCode());
				user.setErrorDetail(ErrorCodes.EmailAlreadyTaken
						.toString());
				return new ResponseEntity<User>(user, HttpStatus.BAD_REQUEST);
			}
			return new ResponseEntity<User>(user, HttpStatus.CREATED);
		} else if (user.getAuthType() == AuthenticateType.facebook) {

			if (user.getFacebookAuthToken() == null) {
				user.setErrorCode(ErrorCodes.FacebookLoginNeedAcessToekn
						.getErrorCode());
				user.setErrorDetail(ErrorCodes.FacebookLoginNeedAcessToekn
						.toString());
				return new ResponseEntity<User>(user, HttpStatus.BAD_REQUEST);
			}
			try {
					FacebookService facebookService = new FacebookService();
					FacebookProfile facebookProfile = facebookService.facebookProfile(user.getFacebookAuthToken());
					if (facebookProfile.getName() != null) {
						user.setName(facebookProfile.getName());
					}
					if (facebookProfile.getPicture() != null) {
						Map<String,Object> pictureMap = facebookProfile.getPicture();
						Map<String,String> dataMap = (Map<String,String>)pictureMap.get("data");
						user.setPhoto(dataMap.get("url"));
					}

					if (user.getUsername() == null) {
						user.setUsername(user.getName().toLowerCase().replaceAll(" ",".")+System.currentTimeMillis());
					}
					if (user.getName() == null
							|| user.getName().trim().length() < 3) {
						user.setErrorCode(ErrorCodes.NameLenghtMustBeSixCharater
								.getErrorCode());
						user.setErrorDetail(ErrorCodes.NameLenghtMustBeSixCharater
								.toString());
						return new ResponseEntity<User>(user,
								HttpStatus.BAD_REQUEST);
					}
					user.setToken(establishUserAndLogin(httpServletResponse, user));
					user = userRepository.save(user);


			} catch (Exception e) {
				e.printStackTrace();
				user.setErrorCode(ErrorCodes.FacebookAcessTokenExpires.getErrorCode());
				user.setErrorDetail(ErrorCodes.FacebookAcessTokenExpires.toString());
				return new ResponseEntity<User>(user, HttpStatus.BAD_REQUEST);
			}
			user.setToken(establishUserAndLogin(httpServletResponse, user));
			user.setPassword(null);
			return new ResponseEntity<User>(user, HttpStatus.CREATED);
		}
		return new ResponseEntity<User>(user, HttpStatus.BAD_REQUEST);
	}

	
	
	
	@ApiOperation(value = "Facebook Friends", notes = "Get Facebook Friends", code = 200, httpMethod = "GET", produces = "application/json")
	@ModelAttribute(value = "friends")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Friends Retrived Successfully") })
	@RequestMapping(value = "/api/facebook/friends/{facebook_id}/{auth_token}", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> getFacebookFriends(@ApiParam(name = "facebook_id", value = "facebook id of the  user", required = true) @PathVariable("facebook_id") String facebookId,
			@ApiParam(name = "auth_token", value = "auth_token of the  user", required = true) @PathVariable("auth_token") String auth_token) {
		try {
			
			 String secret = "8f3199da15c6b58a18efd02177824f3a";
		     String message = auth_token;

		     Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
		     SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
		     sha256_HMAC.init(secret_key);

		     String hash = Base64.encodeBase64String(sha256_HMAC.doFinal(message.getBytes()));
		     System.out.println(hash);
			
			String url = "https://graph.facebook.com/"+facebookId+"/friends?access_token="
					+ auth_token+"&appsecret_proof="+hash;
			
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
				for(int i = 0 ; i < jsonArray.length();i++){
					JSONObject jsonObject = (JSONObject) jsonArray.get(i);
					jsonObject.put("profile_picture", "https://graph.facebook.com/v2.9/"+jsonObject.getString("id")+"/picture?width=640&height=640");
				}
				return new ResponseEntity<String>(jObject.toString(), HttpStatus.OK);
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
            userRepository.save(user);
        } catch (Exception e) {
        	e.printStackTrace();
        	user = new User();
        	user.setErrorCode(HttpStatus.BAD_REQUEST.ordinal());
        	user.setErrorDetail(HttpStatus.BAD_REQUEST.toString());
        	return new ResponseEntity<User>(user, HttpStatus.OK);
        }
        return new ResponseEntity<User>(user, HttpStatus.OK);
    }
	
	@RequestMapping(value = "/api/user/metime", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<UserAvailability> saveUserBlockedTime(@RequestBody com.cg.user.bo.UserAvailability userAvailability) {
		
		userAvailability.loadMetadata();
		userAvailabilityRepository.save(userAvailability);
		return new ResponseEntity<UserAvailability>(userAvailability,HttpStatus.OK);
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
