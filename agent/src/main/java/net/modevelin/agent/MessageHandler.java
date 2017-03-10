package net.modevelin.agent;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class MessageHandler {

	private static Map<String, MessageProcessor> commandMap = new ConcurrentHashMap<>();
	
	public static interface MessageProcessor {
		void process(Properties message); 
	}
	
	public static void addHandler(final String topic, final MessageProcessor command) {
		commandMap.put(topic, command); // TODO allow many subscriptions to one topic
	}
	
	public static void removeHandler(final String topic, final MessageProcessor command) {
		commandMap.remove(topic); // TODO allow many subscriptions to one topic
	}
	
	static void handle(final Properties message) {
		String responseType = message.getProperty("COMMAND");
		for (String topic : commandMap.keySet()) {
			if (topic.equals(responseType)) {
				MessageProcessor processor = commandMap.get(topic);
				processor.process(message);
			}
		}
	}
	
}
