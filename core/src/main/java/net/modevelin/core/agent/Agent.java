package net.modevelin.core.agent;

import com.sun.tools.attach.VirtualMachine;

import static net.modevelin.core.ExceptionSupport.*;

import java.io.StringReader;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class Agent {

	private static final Logger LOGGER = Logger.getLogger(Agent.class.getName());
	
	private static final List<ClassDefinition> REDFINITIONS = new ArrayList<>();
	
	private static AgentServer agentServer;
	
	private static String agentName;
	
	/**
	 * This method is called if this agent is invoked via the JVM args e.g. by using
	 * -javaagent:/path/to/modevelin-core.jar=property1=foo&property2=bar.
	 * 
	 * This method will be called before any application classes are loaded; its first
	 * real action is to call back to the modevelin Server, to get a list of class
	 * redefinitions which are required on startup. 
	 * 
	 * @param agentArgs
	 * @param instrumentation
	 */
	public static void premain(final String agentArgs, Instrumentation instrumentation) {
		
		LOGGER.info("premain("+ agentArgs + "," + instrumentation + ")");
		if (agentArgs == null || agentArgs.trim().isEmpty()) {
			throw new RuntimeException("no premain args");
		}

		// To read agentArgs, we replace separator chars with new lines, and treat 
		// the result as a properties file.
		Properties agentProperties = new Properties();
		try {

			agentProperties.load(new StringReader(agentArgs.replace("&", "\r\n")));
			
			Agent.agentName = agentProperties.getProperty("name");
			if (agentName == null || agentName.trim().isEmpty()) {
				throw new RuntimeException("no [name] property in premain args");
			}
			
			// Configure a connection to the back-end Server.
			agentServer = new AgentServer(agentProperties);
			
			// Start the connection to the server; this will immediately return us a list 
			// of class redefinitions to use on application startup.
			Map<String, byte[]> definitions = agentServer.start();
			
			if (definitions != null) {
				
				// Add a ClassFileTransformer to the VM so that our redefinitions are used
				// in place of classes provided by the application.	 
				instrumentation.addTransformer(new ClassFileTransformer() {
					@Override
					public byte[] transform(
						ClassLoader loader, 
						String className, 
						Class<?> classBeingRedefined, 
						ProtectionDomain protectionDomain, 
						byte[] classfileBuffer)	
						throws IllegalClassFormatException {
						
						// This gets called for every class that is loaded for the application.
						// Mostly it will return null, except for any classes where a redefinition
						// has been provided by the back-end.
						for (String definitionClassName : definitions.keySet()) {
							if (definitionClassName.equals(className)) {
								LOGGER.info("Redefining " + className);
								return definitions.get(definitionClassName);
							}
						}
						
						return null;
					}
				});
			}
		}
		catch (Exception ex) {
			rethrowAsRuntimeException(ex);
		}
		
		/*
		addRequestHandler(new TestAgentRequestHandler() {
			@Override
			public void handle(final TestAgentRequest request) {
				if (TestAgentRequestType.SHUTDOWN == request.getRequestType()) {
					socketManager.stop();
				}
			}
		});
		*/
	}
	
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
				Agent.REDFINITIONS.toArray(
					new ClassDefinition[Agent.REDFINITIONS.size()]
				)
			);
		}
		catch (Exception ex) {
			rethrowAsRuntimeException(ex);
		}
	}
	
	public static synchronized void redefine(final List<ClassDefinition> redefinitions) {

		System.out.println("redefine("+ redefinitions + ")");
		
	    String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
	    int p = nameOfRunningVM.indexOf('@');
	    String pid = nameOfRunningVM.substring(0, p);
	    
	    ProtectionDomain protectionDomain = Agent.class.getProtectionDomain();
	    CodeSource codeSource = protectionDomain.getCodeSource();
	    URL location = codeSource.getLocation();
	    String jarFilePath = location.getPath();
	    if (jarFilePath.startsWith("/")) {
	    	jarFilePath = jarFilePath.substring(1);
	    }
	    
	    try {
	    	Agent.REDFINITIONS.clear();
	    	Agent.REDFINITIONS.addAll(redefinitions);
	        VirtualMachine vm = VirtualMachine.attach(pid);
	        vm.loadAgent(jarFilePath, null);
	        vm.detach();
	    } 
	    catch (Exception ex) {
			rethrowAsRuntimeException(ex);
	    }
	}
	
	
}
