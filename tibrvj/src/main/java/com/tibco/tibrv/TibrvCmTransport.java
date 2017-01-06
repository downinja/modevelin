package com.tibco.tibrv;
 
public class TibrvCmTransport extends TibrvTransport {

	public TibrvCmTransport(final com.tibco.tibrv.TibrvRvdTransport rvdTransport) throws TibrvException {
	}
	
	public TibrvCmTransport(final com.tibco.tibrv.TibrvRvdTransport rvdTransport, final String str1, final boolean bool1, final String str2, final boolean bool2) throws TibrvException {
	}

	public void send(final com.tibco.tibrv.TibrvMsg message) throws com.tibco.tibrv.TibrvException {
		//TestAgentResponse response = new TestAgentResponseBuilder(TestAgentResponseType.DEAL_MESSAGE).payload(message.toString()).build();
		//TestAgent.getInstance().sendResponse(response);
	}
	
	public void addListener(final String arg1, final String arg2) {
		
	}
	
	@Override
	public void destroy() {
	}

}
