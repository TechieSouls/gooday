package com.cg.user.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.codec.binary.Base64;
import org.primefaces.json.JSONArray;
import org.primefaces.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cg.constant.CgConstants.ErrorCodes;
import com.cg.enums.CgEnums.AuthenticateType;
import com.cg.events.bo.Event;
import com.cg.repository.UserRepository;
import com.cg.stateless.security.TokenAuthenticationService;
import com.cg.user.bo.User;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import com.google.gson.JsonObject;

@RestController
@Api(value = "User", description = "Create User data")
public class UserController {

	@Autowired
	UserRepository userRepository;

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
					user.getPassword(), user.getUsername()));
		}
		if (user.getEmail() != null) {

			if (userRepository.findUserByEmail(user.getEmail()) != null) {
				user.setErrorCode(ErrorCodes.UserNameOrEmailAlreadyTaken
						.getErrorCode());
				user.setErrorDetail(ErrorCodes.UserNameOrEmailAlreadyTaken
						.toString());
				return new ResponseEntity<User>(user, HttpStatus.BAD_REQUEST);
			}
		}
		if (user.getUsername() != null) {

			if (userRepository.findUserByUsername(user.getUsername()) != null) {
				user.setErrorCode(ErrorCodes.UserNameOrEmailAlreadyTaken
						.getErrorCode());
				user.setErrorDetail(ErrorCodes.UserNameOrEmailAlreadyTaken
						.toString());
				return new ResponseEntity<User>(user, HttpStatus.BAD_REQUEST);
			}
		}

		if (user.getAuthType() == AuthenticateType.email) {
			if (user.getUsername() == null
					|| user.getUsername().trim().length() < 6) {
				user.setErrorCode(ErrorCodes.UserNameLenghtMustBeSixCharater
						.getErrorCode());
				user.setErrorDetail(ErrorCodes.UserNameLenghtMustBeSixCharater
						.toString());
				return new ResponseEntity<User>(user, HttpStatus.BAD_REQUEST);
			}
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
			try {
				// new Md5PasswordEncoder().encodePassword(user.getPassword(),
				// user.getUsername());
				user.setToken(establishUserAndLogin(httpServletResponse, user));
				user = userRepository.save(user);
			} catch (DataIntegrityViolationException e) {
				user.setErrorCode(ErrorCodes.UserNameOrEmailAlreadyTaken
						.getErrorCode());
				user.setErrorDetail(ErrorCodes.UserNameOrEmailAlreadyTaken
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
				String url = "https://graph.facebook.com/me?access_token="
						+ user.getFacebookAuthToken();
				URL obj = new URL(url);
				HttpURLConnection con = (HttpURLConnection) obj
						.openConnection();
				con.setRequestMethod("GET");
				int responseCode = con.getResponseCode();
				if (responseCode == 200) {

					System.out.println("Response Code : " + responseCode);

					BufferedReader in = new BufferedReader(
							new InputStreamReader(con.getInputStream()));
					String inputLine;
					StringBuffer response = new StringBuffer();

					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}
					in.close();
					org.primefaces.json.JSONObject jObject = new org.primefaces.json.JSONObject(
							response.toString());
					if (user.getFacebookID().equals(jObject.get("id"))) {

						if (user.getUsername() == null) {
							for (int retry = 0; retry < 5; retry++) {
								String raString = UUID.randomUUID().toString();
								if (userRepository.findUserByUsername(raString) == null) {
									user.setUsername(raString);
									break;
								}
							}
						}
						if (user.getUsername() == null) {
							throw new Exception();
						}
						if (user.getName() == null
								&& jObject.get("name") != null) {
							user.setName(jObject.getString("name"));
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
						user = userRepository.save(user);

					} else {
						throw new Exception();
					}

				} else {
					throw new Exception();
				}
			} catch (Exception e) {
				e.printStackTrace();
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
