package net.modevelin.common.config.redefinitions;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="classes")
public class Classes {

	@XmlElement(name="class")
	private List<Class> classes;
	
	public List<Class> getClasses() {
		return Collections.unmodifiableList(classes);
	}

}
