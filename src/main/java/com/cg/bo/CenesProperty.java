package com.cg.bo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="cenes_properties")
public class CenesProperty {

	/**
	 * Entities that can own the properties.
	 * @author Mandeep Singh
	 *
	 */	
	public enum PropertyOwningEntity{
		User,
		Event,
		Reminder,
		Diary,
		Alarm
		};
	
	public enum PropertyType{Text,Number,Date,Custom};
	
	@Id
	@GeneratedValue (strategy=GenerationType.AUTO)
	@Column(name="cenes_property_id")
	private Long cenesPropertyId;
	
	private String name;
	
	@Enumerated(EnumType.STRING)
	@Column(name="property_type")
	private PropertyType propertyType;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name="property_owner")
	private PropertyOwningEntity propertyOwner;
		
	/**
	 * Java class of this property. Populated when this property type is Custom
	 */
	@Column(name="property_class")
	private String propertyClass;

	/**
	 * java properties format config values that can be used by property class 
	 */
	@Column(name="property_config_values")
	private String propertyConfigValues;	
	
	/**
	 * Enforce values to be from the options only.
	 */ 
	@Column(name="option_values_only")
	private Boolean optionValuesOnly;

	

	/*@OneToMany(targetEntity=OdandyPropertyOption.class,cascade=CascadeType.ALL,fetch=FetchType.EAGER)
	//@Cascade( { org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
	@JoinColumn(name="odandy_property_id",insertable=false,updatable=false)
	@Fetch(FetchMode.SELECT)
	private List<OdandyPropertyOption> options;*/
	
	/**
	 * Id of the entity that this value belongs to.  
	 */
	
	@Column(name="property_owner_entity_id")
	private Long propertyOwnerEntityId;
	
	public Long getCenesPropertyId() {
		return cenesPropertyId;
	}

	public void setCenesPropertyId(Long cenesPropertyId) {
		this.cenesPropertyId = cenesPropertyId;
	}

	public String getName() {
		return name;
	}


	public PropertyType getPropertyType() {
		return propertyType;
	}


	public PropertyOwningEntity getPropertyOwner() {
		return propertyOwner;
	}


	public String getPropertyClass() {
		return propertyClass;
	}


	public Boolean getOptionValuesOnly() {
		return optionValuesOnly;
	}


	public void setName(String name) {
		this.name = name;
	}


	public void setPropertyType(PropertyType propertyType) {
		this.propertyType = propertyType;
	}


	public void setPropertyOwner(PropertyOwningEntity propertyOwner) {
		this.propertyOwner = propertyOwner;
	}


	public void setPropertyClass(String propertyClass) {
		this.propertyClass = propertyClass;
	}


	public void setOptionValuesOnly(Boolean optionValuesOnly) {
		this.optionValuesOnly = optionValuesOnly;
	}


	public Long getPropertyOwnerEntityId() {
		return propertyOwnerEntityId;
	}


	public void setPropertyOwnerEntityId(Long propertyOwnerEntityId) {
		this.propertyOwnerEntityId = propertyOwnerEntityId;
	}


	public String getPropertyConfigValues() {
		return propertyConfigValues;
	}


	public void setPropertyConfigValues(String propertyConfigValues) {
		this.propertyConfigValues = propertyConfigValues;
	}
}
