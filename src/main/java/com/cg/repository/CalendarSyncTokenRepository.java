package com.cg.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.cg.bo.CalendarSyncToken;
import com.cg.bo.CalendarSyncToken.AccountType;

@Repository
public interface CalendarSyncTokenRepository extends JpaRepository<CalendarSyncToken, Long>{

	public List<CalendarSyncToken> findByAccountType(AccountType accountType);
	
	
	@Query("select cst from CalendarSyncToken cst where cst.accountType = 'Google' and cst.accountType = 'Outlook' and cst.DATEDIFF(now(), cst.subExpiryDate) > 2")
	public List<CalendarSyncToken> findBySubExpiryDateGreaterThanThreeDays();
	
	
	@Transactional
	@Modifying
	@Query("delete from CalendarSyncToken cst where cst.userId = :userId")
	public void deleteByUserId(@Param("userId")  Long userId);
	
	@Transactional
	@Modifying
	public void deleteByUserIdAndAccountType(Long userId, AccountType accountType);
	
	public CalendarSyncToken findByAccountTypeAndSubscriptionId(AccountType accountType, String subscriptionId);
	public List<CalendarSyncToken> findByUserId(Long userId);
	public CalendarSyncToken findByUserIdAndEmailId(Long userId,String email);
	public CalendarSyncToken findByUserIdAndAccountType(Long userId,AccountType accountType);
	public void deleteByUserIdAndEmailId(Long userId,String email);
	
	
	
	@Transactional
	@Modifying
	@Query("delete from CalendarSyncToken cst where cst.refreshTokenId = :refreshTokenId")
	public void deleteByRefreshTokenId(@Param("refreshTokenId") Long refreshTokenId);

}
