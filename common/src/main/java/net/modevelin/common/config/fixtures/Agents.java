package net.modevelin.common.config.fixtures;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="agents")
public class Agents {

	@XmlElement(name="agent")
	private List<Agent> agents;

	public List<Agent> getAgents() {
		return Collections.unmodifiableList(agents);
	}
		
}
