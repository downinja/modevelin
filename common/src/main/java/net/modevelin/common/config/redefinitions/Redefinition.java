package net.modevelin.common.config.redefinitions;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="redefinition")
public class Redefinition {
		
	@XmlAttribute(name="id")
	private String id;

	public String getId() {
		return id;
	}
	
	@XmlElement(name="providers")
	private Providers providers;

	public List<Provider> getProviders() {
		return providers.getProviders();
	}
}
