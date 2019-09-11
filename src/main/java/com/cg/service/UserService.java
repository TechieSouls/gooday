package com.cg.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cg.bo.CalendarSyncToken;
import com.cg.bo.CenesProperty;
import com.cg.bo.HolidayCalendar;
import com.cg.bo.SimCardInfo;
import com.cg.bo.UserStat;
import com.cg.dao.UserDao;
import com.cg.events.bo.Event;
import com.cg.events.bo.EventMember;
import com.cg.repository.CalendarSyncTokenRepository;
import com.cg.repository.HolidayCalendarRepository;
import com.cg.repository.SimCardInfoRepository;
import com.cg.repository.UserContactRepository;
import com.cg.repository.UserDeviceRepository;
import com.cg.repository.UserRepository;
import com.cg.repository.UserStatRepository;
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
	
	@Autowired
	HolidayCalendarRepository holidayCalendarRepository;
	
	@Autowired
	UserStatRepository userStatRepository;
	
	@Autowired
	CalendarSyncTokenRepository calendarSyncTokenRepository;
	
	@Autowired
	UserDao userDao;
	
	@Autowired
	SimCardInfoRepository simCardInfoRepository;
	
	
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
		return userRepository.findUserByFacebookId(facebookId);
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
		List<User> users = userRepository.findByPhoneContaining(phone.replaceAll("\\+", ""));
		if (users == null || users.size() == 0) {
			return null;
		}
		
		return users.get(0);
	}

	
	public void deleteContactsByUserId(long userId) {
		userContactRepository.deleteByUserId(userId);
	}
	
	public void updateContactsByFriendIdAndUserId(Long friendId, String phone) {
		userContactRepository.updateFrindIdByPhone(friendId, phone);
	}
	
	public void deleteUserDeviceByUserId(Long userId) {
		userDeviceRepository.deleteByUserId(userId);
	}
	
	public List<UserContact> findByPhoneContainingAndFriendIdAndUserId(String phone, Long friendId, Long userId) {
		return userContactRepository.findByPhoneContainingAndFriendIdAndUserId(phone, friendId, userId);
	}
	
	public void deleteUserByUserId(Long userId) {
		userRepository.deleteByUserId(userId);
	}
	
	public HolidayCalendar saveHolidayCalendar(HolidayCalendar holidayCalendar) {
		
		return holidayCalendarRepository.save(holidayCalendar);
	}

	public List<HolidayCalendar> findHolidayCalendarByUserId(Long userId) {
		
		return holidayCalendarRepository.findByUserId(userId);
	}
	
	public void updateNameByUserId(String name, Long userId) {
		userRepository.updateNameByUserId(name, userId);
	}
	
	public void updateGenderByUserId(String gender, Long userId) {
		userRepository.updateGenderByUserId(gender, userId);
	}
	
	public void updateBirthDayByUserId(String birthDayStr, Long userId) {
		userRepository.updateDobStrByUserId(birthDayStr, userId);
	}
	
	public void updatePasswordByUserId(String password, Long userId) {
		userRepository.updatePasswordByUserId(password, userId);
	}
	
	public void updateProfilePicByUserId(String profilePicUrl, Long userId) {
		userRepository.updateProfilePicByUserId(profilePicUrl, userId);
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
		
		System.out.println("Contacts List User : "+userId);
		System.out.println("Contacts List Size : "+contacts.size());
		System.out.println("Contacts Data : "+contacts.toString());
		
		List<Map<String,String>> tempContacts = new ArrayList<>();
		for (Entry<String, String> tempUniqueEntryMap: uniqueContactMap.entrySet()) {
			if (tempUniqueEntryMap.getValue().trim().length() == 0) {
				continue;
			}
			Map<String, String> tempUniqueMap = new HashMap<>();
			tempUniqueMap.put(tempUniqueEntryMap.getKey(), tempUniqueEntryMap.getValue().trim());
			tempContacts.add(tempUniqueMap);
		}
		contacts = tempContacts;
		

		//This will return the list of contacts which are not added in database.
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
		
		//Fetching Contacts of a User
		List<UserContact> userContacts = userContactRepository.findByUserId(userId);
		
		System.out.println("Deleting Contacts");
		userContacts = contactsWhichAreDeleted(userContacts, phoneContacts);
		
		System.out.println("Updating Contacts");
		contactsWhichAreModified(userContacts, phoneContacts);
		
		for (UserContact userContact : userContacts) {
			
			Iterator<Map<String,String>> it = phoneContacts.iterator();
			while (it.hasNext()) {
				Map<String,String> value = it.next();
				
				//+164651067 contains key 0164651067
				//+91 8437375294 contains key 08437375294
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
				
				
				//Check if Phone contact and db contacts are matching
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
	
	public Long findCenesMemberCountsByUserId(Long userId) {
		return userContactRepository.findCenesMemberCountsByUserId(userId);
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
				System.out.println("User Contacts : Name : "+contactSet.getValue()+" "+contactSet.getKey().replaceAll("\\+", ""));
				List<User> users = this.userRepository.findByPhoneContaining(contactSet.getKey().replaceAll("\\+", ""));
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

	/***************   USER STAT FUNCTIONS  ******************/
	public UserStat findUserStatByUserId(Long userId) {
		return userStatRepository.findByUserId(userId);
	}
	
	public UserStat saveUpdateUserStat(UserStat userStat) {
		return userStatRepository.save(userStat);
	}
	
	
	
	/************** Cenes Properties ************************/
	public List<CalendarSyncToken> fincSyncTokensByUserId(Long userId) {
		return calendarSyncTokenRepository.findByUserId(userId);
	}
	public void deleteCalendarSyncTokenByCalendarSyncTokenId(Long calendarSyncTokenId) {
		calendarSyncTokenRepository.deleteByRefreshTokenId(calendarSyncTokenId);
	}
	public void deleteCalendarSyncTokensByUserId(Long userId) {
		calendarSyncTokenRepository.deleteByUserId(userId);
	}
	
	//This function will update the cenes member counts in user contacts.
	public void updateCenesMemberCountsByUserContacts(List<UserContact> userContacts) {
		
		List<UserStat> usersStat = userDao.getUserStatsByUserContacts(userContacts);
		List<UserStat> userStatToUpdate = new ArrayList<>();
		
		for (UserContact userContact: userContacts) {
			
			UserStat userStat = null;
			
			for (UserStat userStatTemp : usersStat) {
				if (userStatTemp.getUserId().equals(userContact.getUserId())) {
					
					userStat = userStatTemp;
					Long counts = userStat.getCenesMemberCounts() + 1;
					userStat.setCenesMemberCounts(counts);
					userStatToUpdate.add(userStat);
					break;
				}
			}
			
			if (userStat == null) {	
				userStat  = new UserStat();
				userStat.setCenesMemberCounts(1l);
				userStatToUpdate.add(userStat);
			}
		}
		
		userStatRepository.save(userStatToUpdate);
	}
	
	public void updateEventHostedAndAttendedCounts(List<Event> events) {
		
		for (Event event: events) {
			
			//Lets take out event members.
			List<EventMember> eventMembers = event.getEventMembers();
			
			//We will not do anything with the stats if the event is just a single event.
			if (eventMembers.size() < 2) {
				continue;
			}
			List<EventMember> goingMembers = new ArrayList<>();
			for (EventMember eventMember: eventMembers) {
				if (eventMember.getStatus() != null && eventMember.getStatus().equals(EventMember.MemberStatus.Going.toString())) {
					goingMembers.add(eventMember);
				}
			}
			
			List<UserStat> usersStatToUpdate = new ArrayList<UserStat>();
			
			//Lets find the stats for Attending Members.
			List<UserStat> usersStat = userDao.getUserStatByEventMembers(goingMembers);
			
			for (EventMember goingMember: goingMembers) {
				
				boolean userStatExist = false;

				
				//Lets check if user is hosting the events. Then we will increase hosted counts
				for (UserStat userStat: usersStat) {
					
					//If User Stat exists for Event Members
					if (userStat.getUserId().equals(goingMember.getUserId())) {
						
						//Check if event Member is a host 
						System.out.println("Is User "+goingMember.getUserId()+" An Host : "+event.getCreatedById().equals(goingMember.getUserId()));
						if (event.getCreatedById().equals(goingMember.getUserId())) {
							
							System.out.println("Event Hosted Count Befor Increment : "+userStat.getEventsHostedCounts());
							
							userStat.setEventsHostedCounts(userStat.getEventsHostedCounts() + 1);
							

							System.out.println("Event Hosted Count After Increment : "+userStat.getEventsHostedCounts());

						} else {
							//If event member is not a host
							//Then we will update his events attended counts.
							
							System.out.println("Event Attended Counts Befor Increment : "+userStat.getEventsAttendedCounts());

							userStat.setEventsAttendedCounts(userStat.getEventsAttendedCounts() + 1);
							
							System.out.println("Event Attended Counts Befor Increment : "+userStat.getEventsAttendedCounts());

						}
						
						usersStatToUpdate.add(userStat);
						userStatExist  = true;
						break;
					}
				}
				

				//If Stats Does not exist for user. Then create new stats
				if (!userStatExist) {
					
					UserStat userStat = new UserStat();
					//If eventMember is a Host, Increment hosted counts
					if (event.getCreatedById().equals(goingMember.getUserId())) {
						userStat.setEventsHostedCounts(1l);
					} else {//Increment Attended counts
						userStat.setEventsAttendedCounts(1l);
					}
					userStat.setUserId(goingMember.getUserId());
					usersStatToUpdate.add(userStat);
				}
			}
			
			
			if (usersStatToUpdate.size() > 0) {
				
				userStatRepository.save(usersStatToUpdate);
			}
		}
	}
	
	public SimCardInfo findSimCardInfoByUserId(Long userId) {
		
		try {
			
			return simCardInfoRepository.findByUserId(userId);
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}
	
	public SimCardInfo saveSimCardInfo(SimCardInfo simCardInfo) {
		
		return simCardInfoRepository.save(simCardInfo);
	}
}
