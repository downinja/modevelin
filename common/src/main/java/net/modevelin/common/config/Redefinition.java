package net.modevelin.common.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="redefinition")
public class Redefinition {
		
	@XmlElement(name="agents")
	private Agents agents;
	
	@XmlElement(name="providers")
	private Providers providers;

	public List<String> getAgents() {
		List<String> agentNames = new ArrayList<>();
		for (Agent agent : agents.getAgents()) {
			agentNames.add(agent.getName());
		}
		return agentNames;
	}
	
	public List<Provider> getProviders() {
		return providers.getProviders();
	}
		
}
