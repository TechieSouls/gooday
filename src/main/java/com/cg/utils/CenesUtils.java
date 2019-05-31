package com.cg.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

public class CenesUtils {

	public static SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
	public static SimpleDateFormat yyyyMMddTHHmmss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static SimpleDateFormat hhmma = new SimpleDateFormat("hh:mma");//24 hour format
	public static SimpleDateFormat hhmm = new SimpleDateFormat("HH:mm");//24 hour format
	public static SimpleDateFormat yyyyMMddTHHmmssX = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

	public static String googleAPIKey = "AIzaSyDZpO7JMmn27iBaXsQuIMwFLBYV5e8S86E";

	
	public static Date getEndOfDay(Date date) {
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(date);
	    calendar.set(Calendar.HOUR_OF_DAY, 0);
	    calendar.set(Calendar.MINUTE, 0);
	    calendar.set(Calendar.SECOND, 0);
	    calendar.set(Calendar.MILLISECOND, 0);
	    calendar.add(Calendar.DAY_OF_MONTH, 1);
	    return calendar.getTime();
	}
	
	public static Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
	
	public static int getDayOfDate(Date date) {
		Calendar calendar = new GregorianCalendar();
		return calendar.get(Calendar.DAY_OF_MONTH);
	}
	
	public static Long getDateAfterAddingMinutes(long eventDateInMilliseconds,int minutes) {
	    Calendar now = Calendar.getInstance();
	    now.setTimeInMillis(eventDateInMilliseconds);
	    now.add(Calendar.MINUTE, minutes);
	    now.set(Calendar.SECOND, 0);
	    return now.getTimeInMillis();
    }
	
	public static Date getDateAfterAddingDays(Date eventDate,int day) {
		Calendar now = Calendar.getInstance();
	    now.setTime(eventDate);
	    now.set(Calendar.HOUR_OF_DAY, 0);
	    now.set(Calendar.MINUTE, 0);
	    now.set(Calendar.SECOND, 0);
	    now.add(Calendar.DAY_OF_MONTH, day);
	    return now.getTime();
	}
	
	public static List<Long> divideTimeIntoMinuteSlots(Date startTime, Date endTime,int slotFrameInMinutes) {
		
		System.out.println("Before Dividing: StartTime : "+startTime+", End Time : "+endTime);
		if (endTime == null) {
			return new ArrayList<Long>();
		}
		Calendar startCalendar = Calendar.getInstance();
		startCalendar.setTime(startTime);
		startCalendar.set(Calendar.SECOND, 0);
	    startCalendar.set(Calendar.MILLISECOND, 0);
	    System.out.println(startCalendar.getTime());
		Long startTimeLong = startCalendar.getTimeInMillis();
		startCalendar = null;
		
		Calendar endCalendar = Calendar.getInstance();
		endCalendar.set(Calendar.MILLISECOND, 0);
		endCalendar.set(Calendar.SECOND, 0);
		endCalendar.setTime(endTime);
	    System.out.println(endCalendar.getTime());
	    System.out.println(endCalendar.getTime());

	    Long endTimeLong = endCalendar.getTimeInMillis();
	    endCalendar = null;
	    
	    List<Long> timeSlotValues = new ArrayList<>();
	    //timeSlotValues.add(startTimeLong);
	    Long incrementalTime = startTimeLong;
	    //System.out.println("Divide time inot slots START");
	    while (incrementalTime < endTimeLong) {
	    	
	    	Calendar now = Calendar.getInstance();
	    	now.setTimeInMillis(incrementalTime);
		    now.add(Calendar.MINUTE, slotFrameInMinutes);
		    now.set(Calendar.SECOND, 0);
		    now.set(Calendar.MILLISECOND, 0);
			timeSlotValues.add(now.getTimeInMillis());
		    incrementalTime = now.getTimeInMillis();
		    //System.out.println(now.getTime());
		    now = null;
	    }
	    //System.out.println("Divide time inot slots END");
	    /*if (timeSlotValues.size() > 0) {
		    timeSlotValues.remove(timeSlotValues.size()-1);
	    }*/
		return timeSlotValues;
	}
	
	public static String getAlphaNumeric(int len) {

	    char[] ch = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

	    char[] c = new char[len];
	    Random random = new Random();
	    for (int i = 0; i < len; i++) {
	      c[i] = ch[random.nextInt(ch.length)];
	    }

	    return new String(c);
	  }
	
}
