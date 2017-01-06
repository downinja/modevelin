package net.modevelin.core.server;

import java.util.Map;
import java.util.Properties;

public class MessageHandler {

	private ClassRedefinitionFactory redefinitionFactory;
	
	public void setClassRedefinitionFactory(final ClassRedefinitionFactory redefinitionFactory) {
		this.redefinitionFactory = redefinitionFactory;
	}
	
	// presume can have e.g. multiple outgoing messages etc,
	// but only one sync one e.g. request/response. any others can
	// be sent internally. In fact, can they all be sent internally
	// e.g. we pass the "live" socket from the Server class to some co-ordinator class?
	Properties handle(final Properties message) {
		
		// What might we want to do, here? 
		// We might want to supply new class definitions (either on startup, or in response
		// to some event or other - and potentially to multiple agents). We might also want
		// to fire a message (e.g. data) at one or more agents. What will be the config 
		// mechanisms for handling this workflow, and for specifying which classes to return
		// (and where to get the bytecode from), and for which data to return (and where to get
		// it from) ??
		
		String command = message.getProperty("COMMAND");
		if ("REGISTER_AGENT".equals(command)) {
			String agentName = message.getProperty("NAME");
			Map<String, byte[]> initialRedefinitions = redefinitionFactory.getRedefinitions(agentName, command);
			Properties response = new Properties();
			response.putAll(message);
			response.put("PAYLOAD", initialRedefinitions);
			return response;
		}
		throw new RuntimeException("Unrecognised command: " + command);
	}
	
}
