package com.cg.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.cg.bo.CenesProperty;
import com.cg.bo.CenesPropertyValue;

public class CenesPropertyMapper implements RowMapper<CenesProperty> {

	public CenesProperty mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		CenesProperty cenesProperty = new CenesProperty();

		cenesProperty.setCenesPropertyId(rs.getLong("cenes_property_id"));
		cenesProperty.setName(rs.getString("name"));
		cenesProperty.setCenesPropertyValue(loadCenesPropertyValue(rs));

		return cenesProperty;
	}
	
	public CenesPropertyValue loadCenesPropertyValue(ResultSet rs) {
		
		CenesPropertyValue cenesPropertyValue = new CenesPropertyValue();
		try {
			cenesPropertyValue.setEntityId(rs.getLong("entity_id"));
			cenesPropertyValue.setValue(rs.getString("value"));
		} catch(Exception e) {
			e.printStackTrace();
		}
		return cenesPropertyValue;
	}
}