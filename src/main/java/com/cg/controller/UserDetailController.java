package com.cg.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cg.constant.CgConstants.ErrorCodes;
import com.cg.repository.UserRepository;
import com.cg.stateless.security.TokenAuthenticationService;
import com.cg.stateless.security.UserService;
import com.cg.user.bo.User;
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

	/*
	 * { "username":"Blue", "password":200, "name":"1234" }
	 */

	@ApiOperation(value = "fetch user detail", notes = "Fecth user detail", code = 200, httpMethod = "GET", produces = "application/json")
	@ModelAttribute(value = "product")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "product updated successfuly", response = User.class) })
	@RequestMapping(value = "/api/users/{userId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<User> getUserDetail(@ApiParam(name = "userId", value = "User id", required = true) @PathVariable("userId") Long userId) {
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

	@ApiOperation(value = "create a token", notes = "Create  ", code = 200, httpMethod = "GET", produces = "application/json")
	@ModelAttribute(value = "user")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "product updated successfuly", response = User.class) })
	@RequestMapping(value = "/auth/user/authenticate/{username}/{password}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<User> createToken(
			HttpServletRequest request,
			HttpServletResponse response,
			@ApiParam(name = "username", value = "username of the  user", required = true) @PathVariable("username") String username,
			@ApiParam(name = "password", value = "password of the  user", required = true) @PathVariable("password") String password) {
		;
		User user = userRepository.findUserByUsername(username);
		if (user == null) {
			user = new User();
			user.setErrorCode(ErrorCodes.UserNotFound.getErrorCode());
			user.setErrorDetail(ErrorCodes.UserNotFound.toString());
		} else if (user != null
				&& !user.getPassword().equals(
						new Md5PasswordEncoder().encodePassword(password,
								username))) {
			user = new User();
			user.setErrorCode(ErrorCodes.IncorrectPassword.getErrorCode());
			user.setErrorDetail(ErrorCodes.IncorrectPassword.toString());
		} else if (user != null
				&& user.getPassword().equals(
						new Md5PasswordEncoder().encodePassword(password,
								username))) {
			user.setToken(establishUserAndLogin(response, user));
			user.setPassword(null);
			return new ResponseEntity<User>(user, HttpStatus.OK);
		}
		return new ResponseEntity<User>(user, HttpStatus.NOT_FOUND);

	}
	@ApiOperation(value = "create a token with facebook", notes = "Create a token with facebook ", code = 200, httpMethod = "GET", produces = "application/json")
	@ModelAttribute(value = "user")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Login successfuly", response = User.class) })
	@RequestMapping(value = "/auth/user/authenticate/facebook/{facebook_id}/{auth_token}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<User> createTokenWithFacebook(
			HttpServletRequest request,
			HttpServletResponse response,
			@ApiParam(name = "facebook_id", value = "facebook id of the  user", required = true) @PathVariable("facebook_id") String facebookId,
			@ApiParam(name = "auth_token", value = "auth_token of the  user", required = true) @PathVariable("auth_token") String auth_token) {
		;
		User user = userRepository.findUserByFacebookID(facebookId);
		if (user == null) {
			user = new User();
			user.setErrorCode(ErrorCodes.UserNotFound.getErrorCode());
			user.setErrorDetail(ErrorCodes.UserNotFound.toString());
		} else{
			try{
			String url = "https://graph.facebook.com/me?access_token="+auth_token;
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			if(responseCode == 200){
				
				
				System.out.println("Response Code : " + responseCode);

				BufferedReader in = new BufferedReader(
				        new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer responseString = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					responseString.append(inputLine);
				}
				in.close();
				org.primefaces.json.JSONObject jObject  = new org.primefaces.json.JSONObject(responseString.toString());
				if(user.getFacebookID().equals(jObject.get("id"))){
					user.setToken(establishUserAndLogin(response, user));
					user.setPassword(null);
					return new ResponseEntity<User>(user, HttpStatus.OK);
					
				}else{
					
				}
			
			}else{
			    user.setErrorCode(ErrorCodes.FacebookAcessToeknExpires.getErrorCode());
			    user.setErrorDetail(ErrorCodes.FacebookAcessToeknExpires.toString());
			   
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		
		}
		return new ResponseEntity<User>(user, HttpStatus.NOT_FOUND);

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
