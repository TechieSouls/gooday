package com.cg.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cg.bo.RefreshToken;
import com.cg.bo.RefreshToken.AccountType;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>{

	public List<RefreshToken> findByAccountType(AccountType accountType);
	public void deleteByUserId(Long userId);
	public void deleteByUserIdAndAccountType(Long userId, AccountType accountType);
	public List<RefreshToken> findByUserId(Long userId);
	public RefreshToken findByUserIdAndEmailId(Long userId,String email);
	public void deleteByUserIdAndEmailId(Long userId,String email);
}
