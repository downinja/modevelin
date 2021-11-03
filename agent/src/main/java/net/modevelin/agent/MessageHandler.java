package net.modevelin.agent;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class MessageHandler {

	private static final Map<String, MessageProcessor> COMMAND_MAP = new ConcurrentHashMap<>();
	
	public interface MessageProcessor {
		void process(Properties message); 
	}
	
	public static void addHandler(final String topic, final MessageProcessor command) {
		COMMAND_MAP.put(topic, command); // TODO allow many subscriptions to one topic
	}
	
	public static void removeHandler(final String topic, final MessageProcessor command) {
		COMMAND_MAP.remove(topic); // TODO allow many subscriptions to one topic
	}
	
	static void handle(final Properties message) {
		String responseType = message.getProperty("COMMAND");
		for (String topic : COMMAND_MAP.keySet()) {
			if (topic.equals(responseType)) {
				MessageProcessor processor = COMMAND_MAP.get(topic);
				processor.process(message);
			}
		}
	}
	
}
