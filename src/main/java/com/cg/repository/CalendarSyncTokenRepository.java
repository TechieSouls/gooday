package com.cg.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cg.bo.CalendarSyncToken;
import com.cg.bo.CalendarSyncToken.AccountType;

@Repository
public interface CalendarSyncTokenRepository extends JpaRepository<CalendarSyncToken, Long>{

	public List<CalendarSyncToken> findByAccountType(AccountType accountType);
	public void deleteByUserId(Long userId);
	public void deleteByUserIdAndAccountType(Long userId, AccountType accountType);
	public List<CalendarSyncToken> findByUserId(Long userId);
	public CalendarSyncToken findByUserIdAndEmailId(Long userId,String email);
	public CalendarSyncToken findByUserIdAndAccountType(Long userId,AccountType accountType);
	public void deleteByUserIdAndEmailId(Long userId,String email);
}
