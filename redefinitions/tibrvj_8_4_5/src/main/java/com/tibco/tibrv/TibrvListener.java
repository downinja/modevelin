package com.tibco.tibrv;

import java.util.Properties;

import net.modevelin.agent.AgentServer;
import net.modevelin.agent.MessageHandler;
import net.modevelin.redefinitions.tibrv_8_4_5.AgentMessageProcessor;

/**
 * Redefinition of com.tibco.tibrv.TibrvListener. 
 * 
 * Note that if we are attaching the modevelin agent on startup, such that the real 
 * version of this class has not already been loaded, then we don't need to implement 
 * any methods which are not invoked as part of our use case, since the JVM will not 
 * come looking for them. 
 * 
 * The methods below are the ones needed to get the demo working, most likely these 
 * would need to be expanded to provide a more general purpose drop-in replacement for 
 * this class. Ideally, we would use the entire source file and just surgically remove
 * the calls we need to - so that potentially we could attach this redefinition after
 * the original class has been loaded. (In such cases, the JVM enforces that the 
 * redefined class is structurally identical to the one that it's replacing.) However,
 * this would probably bump into licensing issues if the code is not open source.
 *
 */
public class TibrvListener {

	public TibrvListener(
		final com.tibco.tibrv.TibrvQueue queue, 
		final com.tibco.tibrv.TibrvMsgCallback callback, 
		final com.tibco.tibrv.TibrvTransport transport, 
		final java.lang.String subject, 
		final java.lang.Object foo) throws com.tibco.tibrv.TibrvException {
		 
		// TODO, we probably need to make use of queue/subject params to mimic
		// tibrv behaviour in sending the right messages to the right listeners.
		MessageHandler.addHandler("TIBMSG", new AgentMessageProcessor(this, callback));
		Properties props = new Properties();
		// TODO - this needs to be more specific than just "READY",
		// it needs to be that this specific function (TibrvListener) 
		// is ready. It's up to the config of the test fixture as to
		// what, ultimately, constitutes "Go!" on the back-end. 
		// (E.g., it's likely to be more like a countdown latch of
		// various components signalling that they have been put in
		// place on the agent side.)
		props.put("COMMAND", "READY"); 
		AgentServer.getInstance().send(props);
		 
	 }
	 
	
}
