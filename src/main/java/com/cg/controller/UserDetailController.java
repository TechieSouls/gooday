package com.cg.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cg.constant.CgConstants.ErrorCodes;
import com.cg.enums.CgEnums.AuthenticateType;
import com.cg.repository.UserContactRepository;
import com.cg.repository.UserRepository;
import com.cg.stateless.security.TokenAuthenticationService;
import com.cg.stateless.security.UserService;
import com.cg.user.bo.User;
import com.cg.user.bo.UserContact;
import com.cg.user.bo.UserContact.CenesMember;
import com.google.common.collect.Sets;

@RestController
@Api(value = "User", description = "fetch User data")
public class UserDetailController {

	// @Autowired
	UserService userService;

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	TokenAuthenticationService tokenAuthenticationService;
	
	@Autowired
	UserContactRepository userContactRepository;

	@Value("${cenes.salt}")
	private String salt;
	/*
	 * { "username":"Blue", "password":200, "name":"1234" }
	 */

	
	@ApiOperation(value = "create a token", notes = "Create  ", code = 200, httpMethod = "GET", produces = "application/json")
	@ModelAttribute(value = "user")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "product updated successfuly", response = User.class) })
	@RequestMapping(value = "/auth/user/authenticate", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<User> createToken(
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestBody User user) {
		
		
		String phone = user.getPhone();
		
		System.out.println("[ Date : "+new Date()+", UserType : Email, Message : Creating new user");
		if (user.getAuthType() == AuthenticateType.email && user.getEmail() != null) {
			String password = new Md5PasswordEncoder().encodePassword(user.getPassword(), salt);
			user = userRepository.findUserByEmailAndPassword(user.getEmail(),password);
		}
		
		if (user == null) {
			user = new User();
			user.setErrorCode(ErrorCodes.IncorrectEmailOrPassword.getErrorCode());
			user.setErrorDetail(ErrorCodes.IncorrectEmailOrPassword.toString());
		} else if (user.getUserId() == null) {
			user = new User();
			user.setErrorCode(HttpStatus.BAD_REQUEST.ordinal());
			user.setErrorDetail(HttpStatus.BAD_REQUEST.name());
		} else if (user != null) {
			if (user.getPhone() == null) {
				user.setPhone(phone);
			
				List<UserContact> userContactInOtherContacts = userContactRepository.findByPhone(user.getPhone());
				if (userContactInOtherContacts != null && userContactInOtherContacts.size() > 0) {
					for (UserContact userContact : userContactInOtherContacts) {
						userContact.setCenesMember(CenesMember.yes);
						userContact.setFriendId(user.getUserId());
					}
					userContactRepository.save(userContactInOtherContacts);
				}
				userRepository.save(user);
			}
		}
		System.out.println("[ Date : "+new Date()+", UserType : Email, Message : Creating new user "+user.toString());	
		user.setPassword("password");
		return new ResponseEntity<User>(user, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/auth/user/webauthenticate", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Map<String, Object>> loginInWeb(
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestBody User user) {
		
		
		Map<String, Object> responseMap = new HashMap<String, Object>();
		responseMap.put("success", true);
		
		System.out.println("[ Date : "+new Date()+", UserType : Email, Message : Creating new user");
		
		if (user.getAuthType() != null) {
			
		}
		
		if (user.getAuthType() == AuthenticateType.email && user.getEmail() != null) {
			
			User emailUser = userRepository.findUserByEmail(user.getEmail());
			
			if (emailUser == null) {
				
				responseMap.put("success", false);
				responseMap.put("errorCode", 1001);
				responseMap.put("message", "Account does not exist");
				return new ResponseEntity<Map<String, Object>>(responseMap, HttpStatus.OK);
			}
			
			String password = new Md5PasswordEncoder().encodePassword(user.getPassword(), salt);
			user = userRepository.findUserByEmailAndPassword(user.getEmail(),password);
			if (user == null) {
				
				responseMap.put("success", false);
				responseMap.put("errorCode", 1002);
				responseMap.put("message", ErrorCodes.IncorrectEmailOrPassword.toString());
				return new ResponseEntity<Map<String, Object>>(responseMap, HttpStatus.OK);
			}
		} else if (user.getAuthType() == AuthenticateType.facebook) {
			
			user = this.userRepository.findUserByFacebookId(user.getFacebookId());
			if (user == null) {
				responseMap.put("success", false);
				responseMap.put("errorCode", 1001);
				responseMap.put("message", "Account does not exist");
				return new ResponseEntity<Map<String, Object>>(responseMap, HttpStatus.OK);
			}
		} else if (user.getAuthType() == AuthenticateType.google) {
			
			user = this.userRepository.findUserByFacebookId(user.getGoogleId());
			if (user == null) {
				responseMap.put("success", false);
				responseMap.put("errorCode", 1001);
				responseMap.put("message", "Account does not exist");
				return new ResponseEntity<Map<String, Object>>(responseMap, HttpStatus.OK);
			}
		}
		
		responseMap.put("data", user);
		System.out.println("[ Date : "+new Date()+", Message : Logging user "+user.toString());	
		return new ResponseEntity<Map<String, Object>>(responseMap, HttpStatus.OK);
	}
	
	
	@ApiOperation(value = "fetch user detail", notes = "Fecth user detail", code = 200, httpMethod = "GET", produces = "application/json")
	@ModelAttribute(value = "product")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "product updated successfuly", response = User.class) })
	@RequestMapping(value = "/api/users/{userId}", method = RequestMethod.GET)
	@ResponseBody
		public ResponseEntity<User> getUserDetail(@ApiParam(name = "userId", value = "User id", required = true) 
		@PathVariable("userId") Long userId) {
		User user = new User();
		try {
			return new ResponseEntity<User>(userRepository.findOne(userId),
					HttpStatus.OK);
		} catch (Exception ex) {

		}
		user.setErrorCode(ErrorCodes.RowNotFound.getErrorCode());
		user.setErrorDetail(ErrorCodes.RowNotFound.toString());
		return new ResponseEntity<User>(user, HttpStatus.OK);
	}
	

	@ApiOperation(value = "fetch own user detail", notes = "Fecth own user detail", code = 200, httpMethod = "GET", produces = "application/json")
	@ModelAttribute(value = "user")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "User fetched successfuly", response = User.class) })
	@RequestMapping(value = "/api/users/me", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<User> getUserDetail() {
		User user = new User();
		try {
			
			 user = (User) SecurityContextHolder.getContext()
					.getAuthentication().getPrincipal();
			 user = userRepository.findOne(user.getUserId());
			
			return new ResponseEntity<User>(user,
					HttpStatus.OK);
		} catch (Exception ex) {

		}
		user.setErrorCode(ErrorCodes.RowNotFound.getErrorCode());
		user.setErrorDetail(ErrorCodes.RowNotFound.toString());
		return new ResponseEntity<User>(user, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/auth/user/findByEmail", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> findUserByEmail(String email) {
		
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("success", true);
		
		User user = null;
		try {
			user = userRepository.findUserByEmail(email);
			if (user != null) {
				response.put("success", false);
				response.put("message", "This Account Already Exists");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}

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
