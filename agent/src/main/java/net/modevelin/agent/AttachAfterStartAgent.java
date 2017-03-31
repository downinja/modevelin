package net.modevelin.agent;

import com.sun.tools.attach.VirtualMachine;

import static net.modevelin.agent.ExceptionSupport.*;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AttachAfterStartAgent {

	private static final Logger LOGGER = Logger.getLogger(AttachAfterStartAgent.class.getName());

	/**
	 * REDFINITIONS is used when the agent is invoked some time after application startup,
	 * rather than via the -javaagent VM parameter route. The intention is that "unit"
	 * test fixtures will pass a list of ClassDefinitions to our redefine method, which
	 * will store these for the agent to pick up via agentmain, when invoked. 
	 */
	private static final List<ClassDefinition> REDFINITIONS = new ArrayList<>();
	
	/**
	 * Called when an Agent is attached after the VM has been initialised. This method is
	 * not intended to be used by applications; it acts as a hook which is invoked by the
	 * redefine(List<ClassDefinition> method, below, after that method populates the
	 * REDEFINITIONS list and calls vm.loadAgent.
	 *
	 * @param agentArgs
	 * @param instrumentation
	 */
	public static void agentmain(final String agentArgs, final Instrumentation instrumentation) {

		LOGGER.info("agentmain(" + agentArgs + "," + instrumentation + ")");

		try {
			instrumentation.redefineClasses(
				AttachAfterStartAgent.REDFINITIONS.toArray(
					new ClassDefinition[AttachAfterStartAgent.REDFINITIONS.size()]
				)
			);
		}
		catch (Exception ex) {
			rethrowAsRuntimeException(ex);
		}
	}

	public static synchronized void redefine(final List<ClassDefinition> redefinitions) {

		LOGGER.info("redefine("+ redefinitions + ")");

	    String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
	    int p = nameOfRunningVM.indexOf('@');
	    String pid = nameOfRunningVM.substring(0, p);

	    ProtectionDomain protectionDomain = AttachAfterStartAgent.class.getProtectionDomain();
	    CodeSource codeSource = protectionDomain.getCodeSource();
	    URL location = codeSource.getLocation();
	    String jarFilePath = location.getPath();
	    if (jarFilePath.startsWith("/")) {
	    	jarFilePath = jarFilePath.substring(1);
	    }

	    try {
	    	AttachAfterStartAgent.REDFINITIONS.clear();
	    	AttachAfterStartAgent.REDFINITIONS.addAll(redefinitions);
	        VirtualMachine vm = VirtualMachine.attach(pid);
	        vm.loadAgent(jarFilePath, null);
	        vm.detach();
	    }
	    catch (Exception ex) {
			rethrowAsRuntimeException(ex);
	    }
	}

}

