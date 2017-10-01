package net.modevelin.common.config.fixtures;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="redefinition")
public class Fixture {
		
	@XmlElement(name="agents")
	private Agents agents;
	
	@XmlElement(name="redefinitions")
	private Redefinitions redefinitions;
	
	@XmlElement(name="messageTypes")
	private MessageTypes messageTypes;
	
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
	
	public List<String> getMessageTypes() {
		List<String> messageTypeTypes = new ArrayList<>();
		for (MessageType messageType : messageTypes.getMessageTypes()) {
			messageTypeTypes.add(messageType.getType());
		}
		return messageTypeTypes;
	}		
}
