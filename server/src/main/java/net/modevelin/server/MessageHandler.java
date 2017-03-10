package net.modevelin.server;

import java.util.Map;
import java.util.Properties;

public class MessageHandler {

	private ClassRedefinitionFactory redefinitionFactory;
	
	private Server server;

	void setClassRedefinitionFactory(final ClassRedefinitionFactory redefinitionFactory) {
		this.redefinitionFactory = redefinitionFactory;
	}
	
	void setServer(final Server server) {
		this.server = server;
	}

	void handle(final Properties message) {

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
			String agentServer = response.getProperty("HOST");
			String agentPort = response.getProperty("PORT");
			server.send(agentServer, Integer.parseInt(agentPort), response);
		}
		else if ("READY".equals(command)) {
			Properties response = new Properties();
			response.putAll(message);
			response.put("COMMAND", "TIBMSG");
			response.put("BODY", "FOO");
			response.put("SENDSUBJ","FOO2YOU");
			response.put("REPLYSUB", "FROMFOO");
			String agentServer = response.getProperty("HOST");
			String agentPort = response.getProperty("PORT");
			server.send(agentServer, Integer.parseInt(agentPort), response);
			// Now wait for response from app and verify against expected result
		}
		else {
			throw new RuntimeException("Unrecognised command: " + command);
		}
	}

}
