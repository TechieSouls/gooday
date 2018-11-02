package com.cg.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cg.repository.UserContactRepository;
import com.cg.repository.UserDeviceRepository;
import com.cg.repository.UserRepository;
import com.cg.user.bo.User;
import com.cg.user.bo.UserContact;
import com.cg.user.bo.UserContact.CenesMember;
import com.cg.user.bo.UserDevice;

@Service
public class UserService {

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	UserDeviceRepository userDeviceRepository;
	
	@Autowired
	UserContactRepository userContactRepository;
	
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
	
	public User findUserByPhone(String phone) {
		List<User> users = userRepository.findByPhone(phone);
		if (users == null || users.size() == 0) {
			return null;
		}
		
		return users.get(0);
	}

	/**
	 * Method to Sync the user phone contacts.
	 * */
	public void syncPhoneContacts(Map<String,Object> phoneContacts) {
		
		Long userId = Long.valueOf(phoneContacts.get("userId").toString());
		List<Map<String,String>> contacts = (List<Map<String,String>>)phoneContacts.get("contacts");
		
		Map<String,String> uniqueContactMap = new HashMap<>();
		for (Map<String,String> tempContact: contacts) {
			for (Entry<String, String> tempEntryMap: tempContact.entrySet()) {
				if (!uniqueContactMap.containsKey(tempEntryMap.getKey())) {
					uniqueContactMap.put(tempEntryMap.getKey(), tempEntryMap.getValue());
				}
			}
		}
		
		List<Map<String,String>> tempContacts = new ArrayList<>();
		for (Entry<String, String> tempUniqueEntryMap: uniqueContactMap.entrySet()) {
			Map<String, String> tempUniqueMap = new HashMap<>();
			tempUniqueMap.put(tempUniqueEntryMap.getKey(), tempUniqueEntryMap.getValue());
			tempContacts.add(tempUniqueMap);
		}
		contacts = tempContacts;
		
		System.out.println("Contacts List User : "+userId);
		System.out.println("Contacts List Size : "+contacts.size());
		System.out.println("Contacts Data : "+contacts.toString());
		
		contacts = filterUserContactsWhichAreNotCenesMember(contacts,userId);		
		
		Map<String,String> threadContacts = null;
		int index = 1;
		for (Map<String, String> contactMap : contacts) {
			if (index == 1) {
				threadContacts = new HashMap<String,String>();
			}
			Entry<String,String> contactMapEntrySet = contactMap.entrySet().iterator().next();
			threadContacts.put(contactMapEntrySet.getKey(),contactMapEntrySet.getValue());
			if ((threadContacts.size() % 10 == 0) || (index == contacts.size()  && threadContacts.size() % 10 > 0)) {
				PhoneConatctsTask phoneConatctsTask = new PhoneConatctsTask();
				phoneConatctsTask.setContacts(threadContacts);
				phoneConatctsTask.setUserId(userId);
				phoneConatctsTask.setUserContactRepository(userContactRepository);
				phoneConatctsTask.setUserRepository(userRepository);
				phoneConatctsTask.run();
				
				threadContacts = new HashMap<String,String>();
			}
			index ++;
		}
	}
	
	public List<Map<String,String>> filterUserContactsWhichAreNotCenesMember(List<Map<String,String>> phoneContacts,Long userId) {
		List<Map<String,String>> phoneContactsToRemove = new ArrayList<>();
		List<UserContact> userContacts = userContactRepository.findByUserId(userId);
		
		System.out.println("Deleting Contacts");
		userContacts = contactsWhichAreDeleted(userContacts, phoneContacts);
		
		System.out.println("Updating Contacts");
		contactsWhichAreModified(userContacts, phoneContacts);
		
		for (UserContact userContact : userContacts) {
			
			Iterator<Map<String,String>> it = phoneContacts.iterator();
			while (it.hasNext()) {
				Map<String,String> value = it.next();
				if (value.containsKey(userContact.getPhone())) {
					//phoneContacts.remove(value);
					phoneContactsToRemove.add(value);
				}
			}
		}
		phoneContacts.removeAll(phoneContactsToRemove);
		return phoneContacts;
	}
 	
	//This method will be called to delete the contacts from database
	//if they are not found in contacts list
	public List<UserContact> contactsWhichAreDeleted(List<UserContact> dbContacts, List<Map<String,String>> phoneContacts) {
		List<UserContact> tempContactList = new ArrayList<>();
		List<UserContact> contactsDeleted = new ArrayList<>();
		for (UserContact userContact : dbContacts) {
			
			boolean contactDeleted = true;
			Iterator<Map<String,String>> it = phoneContacts.iterator();
			while (it.hasNext()) {
				Map<String,String> value = it.next();
				if (value.containsKey(userContact.getPhone())) {
					tempContactList.add(userContact);
					contactDeleted = false;
					break;
				}
			}
			
			if (contactDeleted) {
				contactsDeleted.add(userContact);
			}
		}
		if (contactsDeleted.size() > 0) {
			this.userContactRepository.delete(contactsDeleted);
		}
		return tempContactList;
	}
	
	//This method will modified the contacts whose name has been changed.
	public void contactsWhichAreModified(List<UserContact> dbContacts, List<Map<String,String>> phoneContacts) {
		List<UserContact> contactsModified = new ArrayList<>();
		for (UserContact userContact : dbContacts) {
			Iterator<Map<String,String>> it = phoneContacts.iterator();
			while (it.hasNext()) {
				Map<String,String> value = it.next();
				if (value.containsKey(userContact.getPhone())) {
					if (!value.get(userContact.getPhone()).equals(userContact.getName())) {
						userContact.setName(value.get(userContact.getPhone()));
						contactsModified.add(userContact);
						break;
					}
				}
			}
		}
		if (contactsModified.size() > 0) {
			this.userContactRepository.save(contactsModified);
		}
	}
	
	class PhoneConatctsTask implements Runnable {
		
		private UserRepository userRepository;
		
		private UserContactRepository userContactRepository;
		
		private Map<String,String> contacts;
		
		private Long userId;
		
		@Override
		public void run() {
			for (Entry<String, String> contactSet : getContacts().entrySet()) {
				UserContact userContact = new UserContact(); 
				List<User> users = this.userRepository.findByPhone(contactSet.getKey());
				UserContact.CenesMember cenesMember = CenesMember.no;
				if (users != null && users.size() > 0) {
					User user = users.get(0);
					cenesMember = CenesMember.yes;
					userContact.setFriendId(user.getUserId());
				}
				userContact.setPhone(contactSet.getKey());
				userContact.setName(contactSet.getValue());
				userContact.setUserId(getUserId());
				userContact.setCenesMember(cenesMember);
				try {
					this.userContactRepository.save(userContact);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					System.out.println("-------------"+userContact.getName());
				}
			}
		}

		public UserRepository getUserRepository() {
			return userRepository;
		}

		public void setUserRepository(UserRepository userRepository) {
			this.userRepository = userRepository;
		}

		public UserContactRepository getUserContactRepository() {
			return userContactRepository;
		}

		public void setUserContactRepository(UserContactRepository userContactRepository) {
			this.userContactRepository = userContactRepository;
		}

		public Map<String, String> getContacts() {
			return contacts;
		}

		public void setContacts(Map<String, String> contacts) {
			this.contacts = contacts;
		}

		public Long getUserId() {
			return userId;
		}

		public void setUserId(Long userId) {
			this.userId = userId;
		}
	}
	
}
