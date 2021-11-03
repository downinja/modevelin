package net.modevelin.redefinitions.tibrv_8_4_5;

import java.util.Properties;

import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgCallback;

import net.modevelin.agent.ExceptionSupport;
import net.modevelin.agent.MessageHandler.MessageProcessor;

public class AgentMessageProcessor implements MessageProcessor {

	private final TibrvListener listener;
	private final TibrvMsgCallback callback;
		
	public AgentMessageProcessor(final TibrvListener listener, final TibrvMsgCallback callback) {
		this.callback = callback;
		this.listener = listener;
	}
	
	@Override
	public void process(final Properties props) {
		// TibrvMsg isn't directly Serializable, so we have to reconstruct one
		// from the message properties.
		try {
			String body = props.getProperty("BODY");
			String sendSubj = props.getProperty("SENDSUBJ");
			String receiveSubj = props.getProperty("REPLYSUBJ");
			TibrvMsg msg = new TibrvMsg();
			msg.add("BODY", body);
			msg.setSendSubject(sendSubj);
			msg.setReplySubject(receiveSubj);
			callback.onMsg(listener, msg);
		}
		catch(Exception ex) {
			ExceptionSupport.rethrowAsRuntimeException(ex);
		}
	}
	
}
