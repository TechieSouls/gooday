package com.cg.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cg.bo.UserFriend;
import com.cg.bo.UserFriend.UserStatus;
import com.cg.constant.CgConstants.ErrorCodes;
import com.cg.repository.UserFriendRepository;
import com.cg.repository.UserRepository;
import com.cg.user.bo.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
public class UserFriendController {

	@Autowired
	UserFriendRepository friendRepository;

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	PubNub pubNub;

	@ApiOperation(value = "Follow a user", notes = "Follow a user", code = 200, httpMethod = "POST", produces = "application/json")
	@ModelAttribute(value = "userFriend")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Friend Followed Succesfully", response = UserFriend.class) })
	@RequestMapping(value = "/api/following/{friendUserId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<UserFriend> followAUser(
			@ApiParam(name = "friendUserId", value = "User id", required = true) 
			@PathVariable("friendUserId") Long friendUserId) {

		User user = (User) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();
		//User user = (User) userRepository.findOne(friendUserId);
		List<UserFriend> friends = friendRepository.findByUserIdAndFriendId(
				user.getUserId(), friendUserId);
		if (friends != null && friends.size() > 0) {
			UserFriend userFriend = friends.get(0);
			userFriend.setErrorCode(ErrorCodes.FriendAlreadyYourFriend
					.getErrorCode());
			userFriend.setErrorDetail(ErrorCodes.FriendAlreadyYourFriend
					.toString());
			return new ResponseEntity<UserFriend>(userFriend, HttpStatus.OK);
		}
		User friendUser = userRepository.findOne(friendUserId);
		if (friendUser == null) {
			UserFriend userFriend = new UserFriend();
			userFriend.setFriendId(friendUserId);
			userFriend.setUserId(user.getUserId());
			userFriend.setStatus(UserStatus.Accepted);
			userFriend.setErrorCode(ErrorCodes.UserNotFound.getErrorCode());
			userFriend.setErrorDetail(ErrorCodes.UserNotFound.toString());
			return new ResponseEntity<UserFriend>(userFriend, HttpStatus.OK);
		}
		if (friendUserId == user.getUserId()) {
			UserFriend userFriend = new UserFriend();
			userFriend.setFriendId(friendUserId);
			userFriend.setUserId(user.getUserId());
			userFriend.setStatus(UserStatus.Accepted);
			userFriend.setErrorCode(ErrorCodes.CantFollowYourSelf
					.getErrorCode());
			userFriend.setErrorDetail(ErrorCodes.CantFollowYourSelf.toString());
			return new ResponseEntity<UserFriend>(userFriend, HttpStatus.OK);
		}
		UserFriend userFriend = new UserFriend();
		userFriend.setFriendId(friendUserId);
		userFriend.setUserId(user.getUserId());
		userFriend.setStatus(UserStatus.Accepted);
		userFriend = friendRepository.save(userFriend);
		Gson gson = new Gson();
		gson.toJson(userFriend);
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("eventObjectId", userFriend.getUserFriendId());
		//jsonObject.addProperty("eventType", EventType.NEW_FOLLOWING.toString());
		 sendPubNub(jsonObject.toString(), userFriend.getFriendId() + "");
		// jsonObject.addProperty("eventType", EventType.NEW_FOLLOWER.toString());
		 sendPubNub(jsonObject.toString(), user.getUserId() + "");
		
		return new ResponseEntity<UserFriend>(userFriend, HttpStatus.OK);
	}

	public void sendPubNub(String value,String chnnel){
	    pubNub.publish().message(value).channel(chnnel)
		.async(new PNCallback<PNPublishResult>() {

			@Override
			public void onResponse(PNPublishResult result,
					PNStatus status) {
				System.out.println("pubnub result" + result);
				System.out.println("pubnub status" + status);

			}
		});
	}
	
	@ApiOperation(value = "Unfollow a user", notes = "Unfollow a user", code = 200, httpMethod = "DELETE", produces = "application/json")
	@ModelAttribute(value = "userFriend")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Friend Unfollowed Succesfully", response = UserFriend.class) })
	@RequestMapping(value = "/api/following/{friendUserId}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<UserFriend> unFollowAUser(
			@ApiParam(name = "friendUserId", value = "User id", required = true) 
			@PathVariable("friendUserId") Long friendUserId) {

		User user = (User) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();
		List<UserFriend> friends = friendRepository.findByUserIdAndFriendId(
				user.getUserId(), friendUserId);
		if (friends != null && friends.size() > 0) {
			friendRepository.delete(friends);
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("eventObjectId", friends.get(0).getUserFriendId());
			//jsonObject.addProperty("eventType", EventType.DELETE_FOLLOWING.toString());
			 sendPubNub(jsonObject.toString(),friends.get(0).getFriendId()  + "");
			// jsonObject.addProperty("eventType", EventType.DELETE_FOLLOWER.toString());
			 sendPubNub(jsonObject.toString(), user.getUserId() + "");
	
			return new ResponseEntity<UserFriend>(friends.get(0), HttpStatus.OK);
		}

		UserFriend userFriend = new UserFriend();
		userFriend.setFriendId(friendUserId);
		userFriend.setUserId(user.getUserId());
		userFriend.setStatus(UserStatus.Accepted);
		userFriend.setErrorCode(ErrorCodes.FriendAlreadyNotYourFriend
				.getErrorCode());
		userFriend.setErrorDetail(ErrorCodes.FriendAlreadyNotYourFriend
				.toString());

		return new ResponseEntity<UserFriend>(userFriend, HttpStatus.OK);
	}

	@ApiOperation(value = "get following ", notes = "getfriendsr", code = 200, httpMethod = "GET", produces = "application/json")
	@ModelAttribute(value = "userFriend")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "friend retrieved ", response = List.class) })
	@RequestMapping(value = "/api/following/{page}/{size}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<UserFriend>> getFriendsList(
			@ApiParam(name = "page", value = "page number", required = true) @PathVariable Integer page,
			@ApiParam(name = "size", value = "size of page", required = true) @PathVariable Integer size) {

		User user = (User) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();
		List<UserFriend> friends = friendRepository.findByUserId(
				user.getUserId(), new PageRequest(page, size));

		return new ResponseEntity<List<UserFriend>>(friends, HttpStatus.OK);
	}

	@ApiOperation(value = "get followers", notes = "getfriendsr", code = 200, httpMethod = "GET", produces = "application/json")
	@ModelAttribute(value = "userFriend")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "friend retrieved ", response = List.class) })
	@RequestMapping(value = "/api/followers/{page}/{size}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<UserFriend>> getFollowersList(
			@ApiParam(name = "page", value = "page number", required = true) @PathVariable Integer page,
			@ApiParam(name = "size", value = "size of page", required = true) @PathVariable Integer size) {

		User user = (User) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();
		List<UserFriend> friends = friendRepository.findByFriendId(
				user.getUserId(), new PageRequest(page, size));

		return new ResponseEntity<List<UserFriend>>(friends, HttpStatus.OK);
	}
}
