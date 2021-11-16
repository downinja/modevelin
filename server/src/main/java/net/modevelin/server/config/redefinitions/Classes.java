package net.modevelin.server.config.redefinitions;

import java.util.Collections;
import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="classes")
public class Classes {

	@XmlElement(name="class")
	private List<Class> classes;
	
	public List<Class> getClasses() {
		return Collections.unmodifiableList(classes);
	}

}
