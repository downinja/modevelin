package net.modevelin.server;

import static net.modevelin.server.ExceptionSupport.rethrowAsRuntimeException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

// Probably want to rename this to sth like Transport; or at least, have the Server
// class as the single entry point to the "app", but just be a very thin layer over
// one or more transports and workflow? Maybe don't even need that, if Spring would
// typically start up the endpoints. Or maybe AbstactTransport?

// Although, would this work over e.g. JMS? Needs synchronous request/response, at
// least for initial class redefinitions.
public class Server {

	private static final Logger LOGGER = Logger.getLogger(Server.class.getName()); // Or slf4j?

	private final Object initialCallbackMonitor = new Object();

	private volatile boolean started;

	private int receivePort;

	private MessageHandler handler;

	private Object receivedObject;

	private ServerSocket receiveSocket;

	public void setMessageHandler(final MessageHandler handler) {
		this.handler = handler;
	}

	public void setReceivePort(final int receivePort) {
		this.receivePort = receivePort;
	}

	public synchronized void start() throws Exception {

		if (started) {
			LOGGER.info("Call to Server.start(), but already started");
			return;
		}

		started = true;
		this.receiveSocket = new ServerSocket(receivePort);

		ServerSocketRunner serverSocketRunner = new ServerSocketRunner();
		Thread socketThread = new Thread(serverSocketRunner);
		socketThread.setDaemon(true);
		socketThread.start();

		synchronized (initialCallbackMonitor) {
			initialCallbackMonitor.wait();
			if (receivedObject != null) {
				Properties messageIn = (Properties)receivedObject;
				Properties messageOut =  handler.handle(messageIn);
				String agentServer = messageOut.getProperty("HOST");
				String agentPort = messageOut.getProperty("PORT");
				Object payload = messageOut.get("PAYLOAD");
				if (payload != null) {
					send(agentServer, Integer.parseInt(agentPort), payload);
				}
				Properties tibmsg = new Properties();
				tibmsg.put("BODY", "FOO");
				tibmsg.put("SENDSUBJ","FOO2YOU");
				tibmsg.put("REPLYSUB", "FROMFOO");
				Thread.sleep(2000);
				send(agentServer, Integer.parseInt(agentPort), tibmsg);
			}
		}

	}

	public synchronized void stop() {
		if (!started) {
			LOGGER.log(Level.INFO, "Call to SocketManager.stop(), but not running");
			return;
		}
		started = false;
	}

	public synchronized void send(final String sendHost, final int sendPort, final Object sendObj) {

		LOGGER.log(Level.INFO, new StringBuilder("send(").append(sendObj).append(")").toString());

		Socket sendSocket = null;
		ObjectOutputStream oos = null;
		try {
			sendSocket = new Socket(sendHost, sendPort);
			oos = new ObjectOutputStream(sendSocket.getOutputStream());
			oos.writeObject(sendObj);
		}
		catch (Exception ex) {
			LOGGER.log(Level.SEVERE, "Unable to send response", ex);
			rethrowAsRuntimeException(ex);
		}
		finally {
			if (oos != null) {
				try {
					oos.close();
				}
				catch (Exception ex) {
					LOGGER.log(Level.SEVERE, "Unable to close ObjectOutputStream", ex);
				}
			}
			if (sendSocket != null) {
				try {
					sendSocket.close();
				}
				catch (Exception ex) {
					LOGGER.log(Level.WARNING, "Unable to close sendSocket", ex);
				}
			}
		}
	}

	private class ServerSocketRunner implements Runnable {

		@Override
		public void run() {

			while (started) {

				ObjectInputStream ois = null;
				Socket clientSocket = null;

				try {
					LOGGER.log(Level.INFO, "ServerSocketRunner listening on ServerSocket " + receiveSocket.getLocalPort());
					clientSocket = receiveSocket.accept();
					ois = new ObjectInputStream(clientSocket.getInputStream());
					Object oisObj = ois.readObject();
					LOGGER.log(Level.INFO, "received " + oisObj);
					synchronized(initialCallbackMonitor) {
						receivedObject = oisObj;
						initialCallbackMonitor.notifyAll();
					}

				}
				catch (Exception ex) {
					LOGGER.log(Level.SEVERE, "ServerSocketRunner unable to process request", ex);
				}
			}

		}
	}

	public static void main (String ... args) throws Exception {
		if (args == null || args.length == 0) {
			LOGGER.severe("Usage: net.modevelin.core.server.Server [listenPort]");
			System.exit(1);
		}
		int receivePort = Integer.parseInt(args[0]);
		Server server = new Server();
		server.setReceivePort(receivePort);
		MessageHandler messageHandler = new MessageHandler();
		ClassRedefinitionFactory redefinitionFactory = new ClassRedefinitionFactory();
		messageHandler.setClassRedefinitionFactory(redefinitionFactory);
		server.setMessageHandler(messageHandler);
		server.start();
	}

}
