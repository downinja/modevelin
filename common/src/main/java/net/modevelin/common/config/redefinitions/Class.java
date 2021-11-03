package net.modevelin.common.config.redefinitions;

import jakarta.xml.bind.annotation.XmlValue;

public class Class {

	@XmlValue
	private String name;

	public String getName() {
		return name;
	}
		
}
