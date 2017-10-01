package net.modevelin.common.config.redefinitions;

import javax.xml.bind.annotation.XmlElement;

public class Property {

	@XmlElement(name="name")
	private String name;

	public String getName() {
		return name;
	}
	
	@XmlElement(name="value")
	private String value;

	public String getValue() {
		return value;
	}
	
}
