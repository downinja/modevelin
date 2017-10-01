package net.modevelin.common.config.fixtures;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="redefinitions")
public class Redefinitions {

	@XmlElement(name="redefinition")
	private List<Redefinition> redefintions;

	public List<Redefinition> getRedefintions() {
		return Collections.unmodifiableList(redefintions);
	}
		
}
