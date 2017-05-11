package net.modevelin.server;

import java.util.Properties;

public interface MessageHandler {

	void handle(final Properties message);
}
