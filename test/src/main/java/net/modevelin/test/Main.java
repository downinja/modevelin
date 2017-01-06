package net.modevelin.test;

import com.tibco.tibrv.Tibrv;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgCallback;
import com.tibco.tibrv.TibrvRvdTransport;

import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Main {

	public static Runnable process(final Runnable runnable) {
		Runnable wrapper = new Runnable() {
			@Override
			public void run() {
				System.out.println("FOO3");
				runnable.run();
			}
		};
		return wrapper;		
	}
	
	public static void main (final String ... args) throws Exception {

		Executor executor = Executors.newSingleThreadExecutor();
		executor.execute(new Runnable() { 
			@Override
			public void run() {
				System.out.println("RUNNING");
			}
		});
		
		String service = "7500";
		String network = "190.231.54.20";
		String daemon = "tcp:9025";
		String subject = "SOME.SUBJECT";

		Tibrv.open(Tibrv.IMPL_NATIVE);
		TibrvRvdTransport transport = new TibrvRvdTransport(service, network, daemon);

		new TibrvListener(Tibrv.defaultQueue(), new TibcoListener(), transport, subject, null);

		Thread.sleep(10000);
	}
	
	private static class TibcoListener implements TibrvMsgCallback {
		
		public void onMsg(TibrvListener listener, TibrvMsg msg)	{
			
			System.out.println((new Date()).toString() + ": subject=" + msg.getSendSubject() + ", reply=" + msg.getReplySubject() + ", message=" + msg.toString());

			System.out.flush();

		}
	}
	
}
