package com.cg.bo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name="sim_card_info")
public class SimCardInfo {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="sim_card_info_id")
	private Long simCardInfoId;
	
	@Column(name="user_id")
	private Long userId;
	
	@Column(name="carrier_name")
	private String carrierName;
	
	@Column(name="mobile_country_code")
	private String mobileCountryCode;
	
	@Column(name="mobile_network_code")
	private String mobileNetworkCode;
	
	@Column(name="iso_country_code")
	private String isoCountryCode;

	public Long getSimCardInfoId() {
		return simCardInfoId;
	}

	public void setSimCardInfoId(Long simCardInfoId) {
		this.simCardInfoId = simCardInfoId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getCarrierName() {
		return carrierName;
	}

	public void setCarrierName(String carrierName) {
		this.carrierName = carrierName;
	}

	public String getMobileCountryCode() {
		return mobileCountryCode;
	}

	public void setMobileCountryCode(String mobileCountryCode) {
		this.mobileCountryCode = mobileCountryCode;
	}

	public String getMobileNetworkCode() {
		return mobileNetworkCode;
	}

	public void setMobileNetworkCode(String mobileNetworkCode) {
		this.mobileNetworkCode = mobileNetworkCode;
	}

	public String getIsoCountryCode() {
		return isoCountryCode;
	}

	public void setIsoCountryCode(String isoCountryCode) {
		this.isoCountryCode = isoCountryCode;
	}
}
