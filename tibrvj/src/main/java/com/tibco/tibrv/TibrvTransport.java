package com.tibco.tibrv;

import java.util.Properties;

import net.modevelin.agent.AgentServer;

public class TibrvTransport {

	public void send(final com.tibco.tibrv.TibrvMsg message) throws com.tibco.tibrv.TibrvException {
		Properties props = new Properties();
		props.put("COMMAND", "TIBRESPONSE");
		int numFields = message.getNumFields();
		for (int i=0; i < numFields; i++) {
			TibrvMsgField field = message.getFieldByIndex(i);
				props.put(field.name, field.data);
		}
		AgentServer.getInstance().send(props);
	}

}
