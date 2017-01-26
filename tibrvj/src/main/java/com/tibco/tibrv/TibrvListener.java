package com.tibco.tibrv;

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
		 
	 }
	 
	
}
