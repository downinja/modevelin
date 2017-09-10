package net.modevelin.demo.tibrvj.server;

import java.util.logging.Logger;

import net.modevelin.server.endpoints.SocketServer;

public class ServerMain {

	private static final Logger LOGGER = Logger.getLogger(ServerMain.class.getName()); // Or slf4j?
	
	public static void main (String ... args) throws Exception {
		if (args == null || args.length == 0) {
			LOGGER.severe("Usage: net.modevelin.demo.tibrvj.server [listenPort]");
			System.exit(1);
		}
		
		int receivePort = Integer.parseInt(args[0]);
		SocketServer server = new SocketServer();
		server.setReceivePort(receivePort);
		TibrvjDemoMessageHandler messageHandler = new TibrvjDemoMessageHandler();
		ClassRedefinitionFactory redefinitionFactory = new ClassRedefinitionFactory("/redefinitions.xml");
		messageHandler.setClassRedefinitionFactory(redefinitionFactory);
		messageHandler.setServer(server);
		server.setMessageHandler(messageHandler);
		server.start();
	}

}
