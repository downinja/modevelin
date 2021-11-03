package net.modevelin.common.config.redefinitions;

import jakarta.xml.bind.annotation.XmlElement;

public class Provider {

	@XmlElement(name="provider-class")
	private String providerClass;

	public String getProviderClass() {
		return providerClass;
	}
	
	@XmlElement(name="configuration")
	private Configuration configuration;
	
	public Configuration getConfiguration() {
		return configuration;
	}
		
}
