package com.cg.bo;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.cg.bo.CenesProperty.PropertyOwningEntity;
import com.cg.bo.CenesProperty.PropertyType;
import com.cg.utils.CenesUtils;


/**
 * Values of Cenes properties
 * @author Mandeep Singh
 *
 */
@Entity
@Table(name="cenes_property_values")
public class CenesPropertyValue{
	
	/**
	 * PK
	 */
	@Id
	@GeneratedValue (strategy=GenerationType.AUTO)
	@Column(name="cenes_property_value_id")
	private Long cenesPropertyValueId;
	
	
	/**
	 * Property that this value belongs to
	 */
	@Column(name="cenes_property_id")
	private Long cenesPropertyId;
	
	@ManyToOne
	@JoinColumn(name="cenes_property_id",insertable=false,updatable=false)
	private CenesProperty cenesProperty;
	
	/**
	 * Id of the entity that this value belongs to.
	 */
	@Column(name="entity_id")
	private Long entityId;
	
	/**
	 * type of entity that owns this property and ofcourse the value.
	 */
	@Enumerated(EnumType.ORDINAL)
	@Column(name="owning_entity")
	private PropertyOwningEntity owningEntity;
	
	/**
	 * Id of the option that is the value. This  can be null if a non option value is used.
	 */
	@Column(name="org_property_option_id")
	private Long cenesPropertyOptionId;
	
	/**
	 * String value is always there with the string representation of the value
	 */	
	private String value;
	
	/**
	 * If the value is a number then number value should be populated. String value will still be populated
	 */
	@Column(name="number_value")
	private Double numberValue;
	
	/**
	 * If the value is a date, then date value should be populated. String value will still be populated
	 */
	@Column(name="date_value")
	private Date dateValue;


	public Long getEntityId() {
		return entityId;
	}


	public String getValue() {
		return value;
	}

	public Double getNumberValue() {
		return numberValue;
	}

	public Date getDateValue() {
		return dateValue;
	}


	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}

	public void setOwningEntity(PropertyOwningEntity owningEntity) {
		this.owningEntity = owningEntity;
	}

	public void setCenesPropertyOptionId(Long cenesPropertyOptionId) {
		this.cenesPropertyOptionId = cenesPropertyOptionId;
	}

	public void setValue(String value) {
		this.value = value;
		if ( this.cenesProperty != null){
			if ( this.cenesProperty.getPropertyType() ==PropertyType.Number ){
				try{ this.numberValue = Double.parseDouble(this.value); } catch(Exception e){}
			}else if ( this.cenesProperty.getPropertyType() ==PropertyType.Date ){
				try{ this.dateValue = CenesUtils.yyyyMMddTHHmmss.parse(this.value); } catch(Exception e){}
			}
		}
	}

	public void setNumberValue(Double numberValue) {
		this.numberValue = numberValue;
	}

	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
	}

	public CenesProperty getCenesProperty() {
		return cenesProperty;
	}

	public void setCenesProperty(CenesProperty cenesProperty) {
		this.cenesProperty = cenesProperty;
		if ( this.cenesProperty != null){
			this.cenesPropertyId = this.cenesProperty.getCenesPropertyId();
		}
	}
	
	
}
