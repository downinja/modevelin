package net.modevelin.common.config;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="redefinitions")
public class Redefinitions {
	
	@XmlElement(name="redefinition")
	List<Redefinition> redefinitions;
	
	public List<Redefinition> getRedefinitions() {
		return Collections.unmodifiableList(this.redefinitions);
	}
}

