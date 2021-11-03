package net.modevelin.server.endpoints.socket;

import static net.modevelin.common.ExceptionSupport.rethrowAsRuntimeException;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.modevelin.server.ModevelinServer;
import net.modevelin.server.endpoints.Endpoint;
import org.slf4j.Logger;

public class DefaultSocketEndpoint implements Endpoint {

	private final Logger logger = getLogger(getClass());

	private volatile boolean started;

	private final int receivePort;

	private ModevelinServer modevelinServer;

	private ServerSocket receiveSocket;

	private final Map<String, RegisteredAgent> agentMap = new HashMap<>(); // ConcurrentHashMap? Or just sync on map?

	public DefaultSocketEndpoint(final int receivePort) {
		this.receivePort = receivePort;
	}

	public void setServer(final ModevelinServer modevelinServer) {
		this.modevelinServer = modevelinServer;
	}

	public synchronized void start() throws Exception {

		if (started) {
			logger.info("call to start, but already started");
			return;
		}

		started = true;
		this.receiveSocket = new ServerSocket(receivePort);

		MessageListener messageListener = new MessageListener();
		Thread messageListenerThread = new Thread(messageListener);
		messageListenerThread.setDaemon(true);
		messageListenerThread.start();
	}

	public synchronized void stop() {
		if (!started) {
			logger.warn("call to stop(), but not running");
			return;
		}
		started = false;
	}

	// TODO, move to common utility class?
	private String getAgentName(final Properties message) {
		return message.getProperty("NAME");
	}

	private void registerAgent(final Properties message) {
		String agentName = getAgentName(message);
		String agentServer = message.getProperty("HOST");
		int agentPort = Integer.parseInt(message.getProperty("PORT"));
		RegisteredAgent registeredAgent = new RegisteredAgent(agentServer, agentPort);
		this.agentMap.put(agentName, registeredAgent);
	}

	private class RegisteredAgent {
		private final String host;
		private final int port;
		private RegisteredAgent(final String host, final int port) {
			this.host = host;
			this.port = port;
		}
	}

	public synchronized void send(final String agentName, final Object sendObj) {
		RegisteredAgent agent = agentMap.get(agentName);
		send(agent.host, agent.port, sendObj);
	}

	private synchronized void send(final String sendHost, final int sendPort, final Object sendObj) {

		logger.info("send({}, {}, {})", sendHost, sendObj, sendObj);

		Socket sendSocket = null;
		ObjectOutputStream oos = null;
		try {
			sendSocket = new Socket(sendHost, sendPort);
			oos = new ObjectOutputStream(sendSocket.getOutputStream());
			oos.writeObject(sendObj);
		}
		catch (Exception ex) {
			logger.error("unable to send response", ex);
			rethrowAsRuntimeException(ex);
		}
		finally {
			if (oos != null) {
				try {
					oos.close();
				}
				catch (Exception ex) {
					logger.warn("unable to close ObjectOutputStream", ex);
				}
			}
			if (sendSocket != null) {
				try {
					sendSocket.close();
				}
				catch (Exception ex) {
					logger.warn("unable to close sendSocket", ex);
				}
			}
		}
	}

	private class MessageListener implements Runnable {

		private final Logger logger = getLogger(getClass());

		@Override
		public void run() {

			while (started) {

				ObjectInputStream ois;
				Socket clientSocket;

				try {
					logger.info("listening on port {}", receiveSocket.getLocalPort());
					clientSocket = receiveSocket.accept();
					ois = new ObjectInputStream(clientSocket.getInputStream());
					Object oisObj = ois.readObject();
					logger.info("received {}", oisObj);
					if (oisObj != null) {
						Properties messageIn = (Properties)oisObj;
						String command = messageIn.getProperty("COMMAND");
						if ("REGISTER_AGENT".equals(command)) {
							registerAgent(messageIn);
						}
						modevelinServer.handle(DefaultSocketEndpoint.this, messageIn);
					}
				}
				catch (Exception ex) {
					logger.error("unable to process request", ex);
				}
			}

		}
	}

}
