package net.modevelin.server.config.redefinitions;

import java.util.Collections;
import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="properties")
public class Properties {

	@XmlElement(name="property")
	private List<Property> properties;
	
	public List<Property> getPropertiers() {
		return Collections.unmodifiableList(properties);
	}

}
