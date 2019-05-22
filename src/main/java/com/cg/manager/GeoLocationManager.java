package com.cg.manager;

import java.io.File;
import java.net.InetAddress;

import org.springframework.stereotype.Service;

import com.cg.constant.CgConstants;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.Country;

@Service
public class GeoLocationManager {

	public Country getLocation(String userIpAddress) {
		
		System.out.println("User Ip Address : "+userIpAddress);
		 // A File object pointing to your GeoLite2 database
	       File dbFile = new File(CgConstants.maxmindCityDatabase);
	 
	       // This creates the DatabaseReader object,
	       // which should be reused across lookups.
	       try {
	    	   DatabaseReader reader = new DatabaseReader.Builder(dbFile).build();
	    		 
		       // A IP Address
		       InetAddress ipAddress  = InetAddress.getByName(userIpAddress);
		 
		       // Get City info
		       CityResponse response = reader.city(ipAddress);
		 
		       // Country Info
		       Country country = response.getCountry();
		       System.out.println("Country IsoCode: "+ country.getIsoCode()); // 'US'
		       System.out.println("Country Name: "+ country.getName()); // 'United States'
		       System.out.println(country.getNames().get("zh-CN")); // '美国'
		       return country;
		       /* Subdivision subdivision = response.getMostSpecificSubdivision();
		       System.out.println("Subdivision Name: " +subdivision.getName()); // 'Minnesota'
		       System.out.println("Subdivision IsoCode: "+subdivision.getIsoCode()); // 'MN'
		 
		        // City Info.
		       City city = response.getCity();
		       System.out.println("City Name: "+ city.getName()); // 'Minneapolis'
		 
		       // Postal info
		       Postal postal = response.getPostal();
		       System.out.println(postal.getCode()); // '55455'
		 
		       // Geo Location info.
		       Location location = response.getLocation();
		        
		       // Latitude
		       System.out.println("Latitude: "+ location.getLatitude()); // 44.9733
		        
		       // Longitude
		       System.out.println("Longitude: "+ location.getLongitude()); // -93.2323
		       */
	       } catch(Exception e) {
	    	   e.printStackTrace();
	       }
	       return null;
	   } 
}
