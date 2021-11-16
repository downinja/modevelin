package net.modevelin.server.config.registrations;

import jakarta.xml.bind.annotation.XmlValue;

public class Redefinition {

	@XmlValue
	private String id;

	public String getId() {
		return id;
	}
		
}
