package net.modevelin.demo.tibrvj;

import com.tibco.tibrv.Tibrv;
import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgCallback;
import com.tibco.tibrv.TibrvRvdTransport;

import java.util.Date;

public class ClientMain {

	private static TibrvRvdTransport TRANSPORT;
	
	public static void main (final String ... args) throws Exception {
		
		String service = "7500";
		String network = "190.231.54.20";
		String daemon = "tcp:9025";
		String subject = "SOME.SUBJECT";

		Tibrv.open(Tibrv.IMPL_NATIVE);
		TRANSPORT = new TibrvRvdTransport(service, network, daemon);

		new TibrvListener(Tibrv.defaultQueue(), new TibcoListener(), TRANSPORT, subject, null);

		Thread.sleep(10000);
	}
	
	private static class TibcoListener implements TibrvMsgCallback {
		
		public void onMsg(TibrvListener listener, TibrvMsg msg)	{
			System.out.println((new Date()).toString() + ": subject=" + msg.getSendSubject() + ", reply=" + msg.getReplySubject() + ", message=" + msg.toString());
			
			// TODO, send a response and pick up in server
			try {
				TibrvMsg response = new TibrvMsg();
				response.add("BODY", "BAR");
				response.setSendSubject("SENDSUBJ");
				response.setReplySubject("RECEIVESUBJ");
				TRANSPORT.send(response);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
}
