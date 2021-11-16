package net.modevelin.server.config.registrations;

import java.util.Collections;
import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="agents")
public class Agents {

	@XmlElement(name="agent")
	private List<Agent> agents;

	public List<Agent> getAgents() {
		return Collections.unmodifiableList(agents);
	}
		
}
