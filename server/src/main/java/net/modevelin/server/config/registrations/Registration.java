package net.modevelin.server.config.registrations;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="redefinition")
public class Registration {
		
	@XmlElement(name="agents")
	private Agents agents;
	
	@XmlElement(name="redefinitions")
	private Redefinitions redefinitions;
	
	public List<String> getAgents() {
		List<String> agentNames = new ArrayList<>();
		for (Agent agent : agents.getAgents()) {
			agentNames.add(agent.getName());
		}
		return agentNames;
	}
	
	public List<String> getRedefinitions() {
		List<String> redefinitionIds = new ArrayList<>();
		for (Redefinition redefinition : redefinitions.getRedefintions()) {
			redefinitionIds.add(redefinition.getId());
		}
		return redefinitionIds;
	}		
	
}
