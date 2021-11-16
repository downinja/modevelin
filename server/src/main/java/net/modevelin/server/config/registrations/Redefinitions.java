package net.modevelin.server.config.registrations;

import java.util.Collections;
import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="redefinitions")
public class Redefinitions {

	@XmlElement(name="redefinition")
	private List<Redefinition> redefintions;

	public List<Redefinition> getRedefintions() {
		return Collections.unmodifiableList(redefintions);
	}
		
}
