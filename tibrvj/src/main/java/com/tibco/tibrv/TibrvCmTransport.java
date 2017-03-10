package com.tibco.tibrv;

import net.modevelin.agent.AgentServer;

public class TibrvCmTransport extends TibrvTransport {

	public TibrvCmTransport(final com.tibco.tibrv.TibrvRvdTransport rvdTransport) throws TibrvException {
	}
	
	public TibrvCmTransport(final com.tibco.tibrv.TibrvRvdTransport rvdTransport, final String str1, final boolean bool1, final String str2, final boolean bool2) throws TibrvException {
	}

	public void send(final com.tibco.tibrv.TibrvMsg message) throws com.tibco.tibrv.TibrvException {
		//AgentServer.getInstance().send(message.toString());
	}
	
	public void addListener(final String arg1, final String arg2) {
		
	}
	
	

}
