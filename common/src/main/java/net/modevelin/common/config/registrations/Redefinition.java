package net.modevelin.common.config.registrations;

import jakarta.xml.bind.annotation.XmlValue;

public class Redefinition {

	@XmlValue
	private String id;

	public String getId() {
		return id;
	}
		
}
