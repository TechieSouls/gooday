package com.cg.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cg.repository.UserDeviceRepository;
import com.cg.repository.UserRepository;
import com.cg.user.bo.User;
import com.cg.user.bo.UserDevice;

@Service
public class UserService {

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	UserDeviceRepository userDeviceRepository;
	
	/***********  USER BLOCK STARTS *****************/
	public User findUserById(Long userId) {
		return userRepository.findOne(userId);
	}
	
	public List<User> findAllUsers() {
		return (List<User>) userRepository.findAll();
	}
	/***********  USER BLOCK ENDS *****************/
	
	
	public void saveUserDeviceToken(UserDevice userDevice) {
		userDeviceRepository.save(userDevice);
	}
	
	public List<UserDevice> findUserDeviceInfoByUserId(Long userId) {
		return userDeviceRepository.findByUserId(userId);
	}
	
	public UserDevice findUserDeviceByDeviceTypeAndUserId(String deviceType,Long userId) {
		return userDeviceRepository.findByDeviceTypeAndUserId(deviceType,userId);
	}
	
	public List<UserDevice> findUserDeviceByDeviceType(String deviceType) {
		return userDeviceRepository.findByDeviceType(deviceType);
	}
	
	public void deleteUserDeviceByUserIdAndDeviceType(Long userId,String deviceType) {
		userDeviceRepository.deleteByUserIdAndDeviceType(userId, deviceType);
	}
	
	public User findUserByFacebookId(String facebookId) {
		return userRepository.findUserByFacebookID(facebookId);
	}
	
	public User findUserByEmail(String email) {
		return userRepository.findUserByEmail(email);
	}
	
	public User findUserByResetToken(String resetToken) {
		return userRepository.findByResetTokenAndResetTokenCreatedAt(resetToken);
	}
	
	public User saveUser(User user) {
		return userRepository.save(user);
	}
}
