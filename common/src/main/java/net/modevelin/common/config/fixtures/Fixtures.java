package net.modevelin.common.config.fixtures;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="fixtures")
public class Fixtures {
	
	@XmlElement(name="fixture")
	List<Fixture> fixtures;
	
	public List<Fixture> getFixtures() {
		return Collections.unmodifiableList(this.fixtures);
	}
}

