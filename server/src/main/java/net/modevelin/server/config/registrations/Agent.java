package net.modevelin.server.config.registrations;

import jakarta.xml.bind.annotation.XmlValue;

public class Agent {

	@XmlValue
	private String name;

	public String getName() {
		return name;
	}
		
}
