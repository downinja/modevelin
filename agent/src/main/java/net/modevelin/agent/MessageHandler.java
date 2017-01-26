package net.modevelin.agent;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class MessageHandler {

	private static Map<String, MessageProcessor> commandMap = new ConcurrentHashMap<>();
	
	public static interface MessageProcessor {
		void process(Object message); // TODO, use properties or sth else with metadata about obj received etc
	}
	
	public static void addHandler(final String topic, final MessageProcessor command) {
		commandMap.put(topic, command); // TODO allow many subscriptions to one topic
	}
	
	static void foo(final Object obj) {
		if (commandMap.size() > 0) {
			commandMap.values().iterator().next().process(obj);
		}
	}
	
}
