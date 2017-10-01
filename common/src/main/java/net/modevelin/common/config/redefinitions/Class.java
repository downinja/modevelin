package net.modevelin.common.config.redefinitions;

import javax.xml.bind.annotation.XmlValue;

public class Class {

	@XmlValue
	private String name;

	public String getName() {
		return name;
	}
		
}
