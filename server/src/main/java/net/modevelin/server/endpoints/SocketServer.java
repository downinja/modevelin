package net.modevelin.server.endpoints;

import static net.modevelin.common.ExceptionSupport.rethrowAsRuntimeException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.modevelin.server.MessageHandler;

public class SocketServer {

	private static final Logger LOGGER = Logger.getLogger(SocketServer.class.getName()); // Or slf4j?

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

		while(started) {
			synchronized (initialCallbackMonitor) {
				initialCallbackMonitor.wait();
				if (receivedObject != null) {
					Properties messageIn = (Properties)receivedObject;
					handler.handle(messageIn);
				}
			}
		}
	}

	public synchronized void stop() {
		if (!started) {
			LOGGER.log(Level.INFO, "Call to SocketManager.stop(), but not running");
			return;
		}
		started = false;
		synchronized (initialCallbackMonitor) {
			initialCallbackMonitor.notifyAll();
		}
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

}
