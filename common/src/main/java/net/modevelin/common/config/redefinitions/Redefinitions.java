package net.modevelin.common.config.redefinitions;

import java.util.Collections;
import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="redefinitions")
public class Redefinitions {
	
	@XmlElement(name="redefinition")
	List<Redefinition> redefinitions;
	
	public List<Redefinition> getRedefinitions() {
		return Collections.unmodifiableList(this.redefinitions);
	}
}

