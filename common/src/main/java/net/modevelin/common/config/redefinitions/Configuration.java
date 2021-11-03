package net.modevelin.common.config.redefinitions;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;

public class Configuration {

	@XmlElement(name="classes")
	private Classes classes;
	
	public List<String> getClasses() {
		List<String> classNames = new ArrayList<>();
		for (Class clazz : classes.getClasses()) {
			classNames.add(clazz.getName());
		}
		return classNames;
	}
	
	@XmlElement(name="properties")
	private Properties properties;
	
	public String getProperty(final String propertyName) {
		for (Property property : properties.getPropertiers()) {
			if (property.getName().equals(propertyName)) {
				return property.getValue();
			}
		}
		return null;
	}
}
