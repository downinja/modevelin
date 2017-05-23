package net.modevelin.demo.tibrvj;

import com.tibco.tibrv.Tibrv;
import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgCallback;
import com.tibco.tibrv.TibrvRvdTransport;

import java.util.Date;

/**
 * Demo of an application which sends and receives messages via tibrv.
 * 
 * Config notwithstanding, this code should happily run against a real tibrv network. However having 
 * a dependency on this infrastructure is obviously not ideal for testing; if you try and run this 
 * code without tibrv in place, and without the modevelin agent attached, then you'll see something 
 * along the lines of:
 * 
 * Exception in thread "main" TibrvException[error=901,message=Library not found: tibrvj]
 *	 at com.tibco.tibrv.Tibrv.loadLib(Tibrv.java:476)
 *	 at com.tibco.tibrv.Tibrv.open(Tibrv.java:275)
 *	 at net.modevelin.demo.tibrvj.ClientMain.main(ClientMain.java:22)
 * Internal exception:
 *	 java.lang.UnsatisfiedLinkError: no tibrvj in java.library.path 
 *     
 * You could try and work around this, abstracting all but a few lines of connectivity code into some 
 * class which you then mock out, but that would (a) leave you with a second version of your artefact, 
 * specifically for testing, and would (b) still leave some of your application code (however minimal) 
 * untested. Much better to leave your code "as is", and sever links to the outside word elsewhere. 
 * 
 */
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
			
			// send a response and pick up in server
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
