package net.modevelin.common.config.registrations;

import java.util.Collections;
import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="registrations")
public class Registrations {
	
	@XmlElement(name="registration")
	List<Registration> registrations;
	
	public List<Registration> getRegistrations() {
		return Collections.unmodifiableList(this.registrations);
	}
}

