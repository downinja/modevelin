package com.tibco.tibrv;

import java.util.Properties;

import net.modevelin.agent.AgentServer;
import net.modevelin.agent.MessageHandler;
import net.modevelin.tibrv.AgentMessageHandler;

public class TibrvListener {

	public TibrvListener(
		final com.tibco.tibrv.TibrvQueue queue, 
		final com.tibco.tibrv.TibrvMsgCallback callback, 
		final com.tibco.tibrv.TibrvTransport transport, 
		final java.lang.String subject, 
		final java.lang.Object foo) throws com.tibco.tibrv.TibrvException {
		 
		MessageHandler.addHandler("SOMETIBRVFLAG", new AgentMessageHandler(this, callback));
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
