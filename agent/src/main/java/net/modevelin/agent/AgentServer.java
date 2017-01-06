package net.modevelin.agent;

import static net.modevelin.agent.ExceptionSupport.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AgentServer {

	private static final Logger LOGGER = Logger.getLogger(AgentServer.class.getName());

	private final int backendPort;

	private final Object initialCallbackMonitor;

	private final String backendHost;

	private final String agentName;

	private Object receivedObject;

	private ServerSocket receiveSocket;

	private volatile boolean started;

	public AgentServer(final Properties agentProperties) {

		String hostProperty = agentProperties.getProperty("host");
		if (hostProperty == null || hostProperty.trim().isEmpty()) {
			throw new RuntimeException("no [host] property in premain args");
		}

		String portProperty = agentProperties.getProperty("port");
		if (portProperty == null || portProperty.trim().isEmpty()) {
			throw new RuntimeException("no [port] property in premain args");
		}

		String agentName = agentProperties.getProperty("name");
		if (agentName == null || agentName.trim().isEmpty()) {
			throw new RuntimeException("no [name] property in premain args");
		}

		this.agentName = agentName;
		this.backendHost = hostProperty;
		this.backendPort = Integer.parseInt(portProperty);
		try {
			this.receiveSocket = new ServerSocket();
		}
		catch (Exception ex) {
			rethrowAsRuntimeException(ex);
		}

		this.initialCallbackMonitor = new Object();

	}

	@SuppressWarnings("unchecked")
	public synchronized Map<String, byte[]> start() {

		if (started) {
			LOGGER.log(Level.INFO, "Call to SocketManager.start(), but already started");
			return Collections.emptyMap();
		}

		try {
			receiveSocket.bind(null);
		}
		catch (Exception ex) {
			return rethrowAsRuntimeException(ex);
		}

		started = true;
		ServerSocketRunner serverSocketRunner = new ServerSocketRunner();
		Thread socketThread = new Thread(serverSocketRunner);
		socketThread.setDaemon(true);
		socketThread.start();

		try {
			synchronized (initialCallbackMonitor) {
				Properties props = new Properties();
				props.put("COMMAND", "REGISTER_AGENT");
				props.put("NAME", agentName);
				props.put("HOST", InetAddress.getLocalHost().getHostName());
				props.put("PORT", Integer.toString(receiveSocket.getLocalPort()));
				send(props, true);
				initialCallbackMonitor.wait();
				return (Map<String, byte[]>)receivedObject;
			}
		}
		catch (Exception ex) {
			return rethrowAsRuntimeException(ex);
		}

	}

	public synchronized void stop() {
		if (!started) {
			LOGGER.log(Level.INFO, "Call to SocketManager.stop(), but not running");
			return;
		}
		started = false;
	}

	public synchronized void send(final Object sendObj) {
		send(sendObj, false);
	}

	public synchronized void send(final Object sendObj, final boolean throwExceptionOnError) {

		LOGGER.log(Level.INFO, new StringBuilder("send(").append(sendObj).append(")").toString());

		Socket sendSocket = null;
		ObjectOutputStream oos = null;
		try {
			sendSocket = new Socket(backendHost, backendPort);
			oos = new ObjectOutputStream(sendSocket.getOutputStream());
			oos.writeObject(sendObj);
		}
		catch (Exception ex) {
			LOGGER.log(Level.SEVERE, "Unable to send response", ex);
			if (throwExceptionOnError) {
				rethrowAsRuntimeException(ex);
			}
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
					//@SuppressWarnings("unchecked")
					//RECEIVE_TYPE osiObjCast = (RECEIVE_TYPE)oisObj;
//					callback.handle(osiObjCast);
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

}
