package com.cg.controller;


import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommonController {/*
	
	@Autowired
	CountryRepository countryRepository;
	
	@RequestMapping(value="/api/load/countries",method = RequestMethod.GET)
	public void updateCountries() {
		
		GoogleService googleService = new GoogleService();
		GoogleCountries gc = googleService.getCountries();
		if (gc != null) {
			List<GoogleCountryItem> countryItems = gc.getResponse();
			countryItems.forEach(countryItem->{
				Country country = new Country();
				country.setName(countryItem.getName());
				country.setFlag(countryItem.getFlag());
				country.setLatitude(countryItem.getLatitude());
				country.setLongitude(countryItem.getLongitude());
				countryRepository.save(country);
			});
		}
	}
*/}


