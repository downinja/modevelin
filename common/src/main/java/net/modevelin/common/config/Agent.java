package net.modevelin.common.config;

import javax.xml.bind.annotation.XmlValue;

public class Agent {

	@XmlValue
	private String name;

	public String getName() {
		return name;
	}
		
}
