package net.modevelin.agent;

import static net.modevelin.agent.ExceptionSupport.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.modevelin.agent.MessageHandler.MessageProcessor;

public class AgentServer {

	private static final Logger LOGGER = Logger.getLogger(AgentServer.class.getName());

	private int backendPort;
	
	private ExecutorService executorService;

	private InitialMessageProcessor initialMessageProcessor;

	private String localHost;
	
	private String backendHost;

	private String agentName;

	private ServerSocket receiveSocket;

	private volatile boolean started;
	
	private static AgentServer INSTANCE;

	public static synchronized AgentServer getInstance(final Properties properties) {
		if (INSTANCE == null) {
			INSTANCE = new AgentServer(properties);
		}
		else {
			// TODO, check properties the same as the ones we already have
		}
		return INSTANCE;
	}
	
	public static synchronized AgentServer getInstance() {
		if (INSTANCE == null) {
			throw new RuntimeException("AgentServer has not been initialised");
		}
		return INSTANCE;
	}
	
	private AgentServer(final Properties agentProperties) {

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
			this.localHost = InetAddress.getLocalHost().getHostName();
		}
		catch (Exception ex) {
			rethrowAsRuntimeException(ex);
		}

		this.executorService = Executors.newSingleThreadExecutor(); // TODO, give it a name
		this.initialMessageProcessor = new InitialMessageProcessor();

	}

	public synchronized Properties start() {

		if (started) {
			LOGGER.log(Level.INFO, "Call to SocketManager.start(), but already started");
			return initialMessageProcessor.receivedObject;
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
			
			MessageHandler.addHandler("REGISTER_AGENT", initialMessageProcessor);
			
			synchronized (initialMessageProcessor) {
				Properties props = new Properties();
				props.put("COMMAND", "REGISTER_AGENT");
				send(props, true);
				initialMessageProcessor.wait();
				MessageHandler.removeHandler("REGISTER_AGENT", initialMessageProcessor);
				return initialMessageProcessor.receivedObject;
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

	public synchronized void send(final Properties sendObj) {
		send(sendObj, false);
	}

	public synchronized void send(final Properties sendObj, final boolean throwExceptionOnError) {

		LOGGER.log(Level.INFO, new StringBuilder("send(").append(sendObj).append(")").toString());

		sendObj.put("NAME", agentName);
		sendObj.put("HOST", localHost);
		sendObj.put("PORT", Integer.toString(receiveSocket.getLocalPort()));
		
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
					executorService.submit(new Runnable() {
						@Override
						public void run() {	
							MessageHandler.handle((Properties)oisObj);
						}
					});
				}
				catch (Exception ex) {
					LOGGER.log(Level.SEVERE, "ServerSocketRunner unable to process request", ex);
				}
			}

		}
	}

	private static class InitialMessageProcessor implements MessageProcessor {

		private Properties receivedObject;
		
		@Override
		public void process(Properties message) {
			receivedObject = message;
			synchronized(this) {
				this.notifyAll();
			}
		}
		
	}
	
}
