package net.modevelin.tibrv;

import java.util.Properties;

import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgCallback;

import net.modevelin.agent.ExceptionSupport;
import net.modevelin.agent.MessageHandler;
import net.modevelin.agent.MessageHandler.MessageProcessor;

public class AgentMessageHandler implements MessageProcessor {

	private final TibrvListener listener;
	private final TibrvMsgCallback callback;
		
	public AgentMessageHandler(final TibrvListener listener, final TibrvMsgCallback callback) {
		this.callback = callback;
		this.listener = listener;
		MessageHandler.addHandler("foo", this);
	}
	
	@Override
	public void process(final Object obj) {
		// pull relevant fields out of props (e.g. TibrvMsg) and invoke callback
		try {
			Properties props = (Properties)obj;
			String body = props.getProperty("BODY");
			String sendSubj = props.getProperty("SENDSUBJ");
			String receiveSubj = props.getProperty("REPLYSUBJ");
			TibrvMsg msg = new TibrvMsg();
			msg.setSendSubject(sendSubj);
			msg.setReplySubject(receiveSubj);
			callback.onMsg(listener, msg);
		}
		catch(Exception ex) {
			ExceptionSupport.rethrowAsRuntimeException(ex);
		}
	}
	
}
