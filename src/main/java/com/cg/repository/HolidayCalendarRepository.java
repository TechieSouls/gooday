package com.cg.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cg.bo.HolidayCalendar;

@Repository
public interface HolidayCalendarRepository extends JpaRepository<HolidayCalendar, Long>{

	public List<HolidayCalendar> findByUserId(Long userId);
	
	public HolidayCalendar findByCountryCalendarId(String countryCalendarId);
}
