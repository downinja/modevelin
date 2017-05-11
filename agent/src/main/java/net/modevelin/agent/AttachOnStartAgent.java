package net.modevelin.agent;

import static net.modevelin.agent.ExceptionSupport.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Logger;

public class AttachOnStartAgent {

	private static final Logger LOGGER = Logger.getLogger(AttachOnStartAgent.class.getName());

	/**
	 * agentServer handles our communications with the back-end.
	 */
	private static AgentServer agentServer; 

	/**
	 * agentName identifies us to the back-end.
	 */
	private static String agentName; 

	/**
	 * tmpDirectory optionally specifies where we should write any 
	 * temporary files to - else we'll just default to java.io.tmpdir.
	 * 
	 */
	private static File tmpDirectory; 

	/**
	 * This method is invoked via the JVM by using e.g.
	 * -javaagent:/path/to/modevelin-agent.jar=property1=foo&property2=bar.
	 *
	 * This method will be called before any application classes are loaded; its first
	 * real action is to call back to the Modevelin Server - to get a list of class
	 * redefinitions which are required on startup.
	 *
	 * @param agentArgs
	 * @param instrumentation
	 */
	@SuppressWarnings("unchecked")
	public static void premain(final String agentArgs, final Instrumentation instrumentation) {

		LOGGER.info("premain("+ agentArgs + "," + instrumentation + ")");
		if (agentArgs == null || agentArgs.trim().isEmpty()) {
			throw new RuntimeException("no premain args");
		}

		// To read agentArgs, we replace separator chars with new lines, and treat
		// the result as a properties file.
		Properties agentProperties = new Properties();
		try {

			agentProperties.load(new StringReader(agentArgs.replace("&", "\r\n")));

			AttachOnStartAgent.agentName = agentProperties.getProperty("name");
			if (agentName == null || agentName.trim().isEmpty()) {
				throw new RuntimeException("no [name] property in premain args");
			}
			
			String tmpDirectoryName = agentProperties.getProperty("tmp");
			if (tmpDirectoryName != null && !tmpDirectoryName.trim().isEmpty()) {
				AttachOnStartAgent.tmpDirectory = 
					processTmpDirectoryName(tmpDirectoryName);
			}

			// Configure a connection to the back-end Server.
			agentServer = AgentServer.getInstance(agentProperties);

			// Start the connection to the server; this will immediately return us a list
			// of class redefinitions to use on application startup.
			Properties initialProperties = agentServer.start();
			if (initialProperties != null && initialProperties.containsKey("REDEFINITIONS")) {
				
				Map<String, byte[]> definitions = (Map<String, byte[]>)initialProperties.get("REDEFINITIONS");
				if (definitions != null) {
					// Not all bytes received from the server may be redefinitions - 
					// some may be supporting classes. These will need to be visible
					// to redefinitions which reference them, and the easiest way to
					// do this seems to be to stick them on the system classloader.
					JarFile jarFile = createDefinitionsJarFile(definitions); 
					instrumentation.appendToSystemClassLoaderSearch(jarFile);
					
					// Now we can add ClassFileTransformer to the VM so that our redefinitions
					// are used in place of any classes provided by/with the application.
					instrumentation.addTransformer(new AgentClassFileTransformer(definitions));
				}
			}
		}
		catch (Exception ex) {
			rethrowAsRuntimeException(ex);
		}
	}
	
	private static class AgentClassFileTransformer implements ClassFileTransformer {
		
		private final Map<String, byte[]> definitions;
		
		private AgentClassFileTransformer(final Map<String, byte[]> definitions) {
			this.definitions = definitions;
		}
		
		@Override
		public byte[] transform (
			final ClassLoader loader, 
			final String className, 
			final Class<?> classBeingRedefined,
			final ProtectionDomain protectionDomain, 
			final byte[] classfileBuffer) 
		throws 
			IllegalClassFormatException {
			
			for (String definitionClassName : definitions.keySet()) {
				if (definitionClassName.equals(className)) {
					LOGGER.info("Redefining " + className);
					return definitions.remove(definitionClassName);
				}
			}
			return null;
		}
	}

	private static JarFile createDefinitionsJarFile(final Map<String, byte[]> definitions) throws Exception {
		
		long now = System.currentTimeMillis();
		
		File jarFile = tmpDirectory == null ? 
			File.createTempFile("Modevelin-" + agentName, ".jar") :
				new File(tmpDirectory, "Modevelin-" + agentName + now + ".jar");
		// mark for deletion on exit.
		jarFile.deleteOnExit();
		
      	// create the jar file
 		FileOutputStream stream = new FileOutputStream(jarFile);
 		JarOutputStream out = new JarOutputStream(stream, new Manifest());

      	// write our classes to it
      	for (String className : definitions.keySet()) {
      		byte[] classBytes = definitions.get(className);
      		if (classBytes == null) {
      			throw new Exception("No bytes provided by server for class [" + className + "]");
      		}
	        JarEntry jarAdd = new JarEntry(className + ".class");
	        jarAdd.setTime(now);
	        out.putNextEntry(jarAdd);
	        out.write(classBytes); 
      	}
        out.close();
        stream.close();
        return new JarFile(jarFile);
 	}
 	
	private static File processTmpDirectoryName(final String tmpDirectoryName) {
		File tmpDirectory = new File(tmpDirectoryName);
		if (!tmpDirectory.exists()) {
			boolean created = tmpDirectory.mkdirs();
			if (!created) {
				throw new RuntimeException("Unable to create directory " + tmpDirectoryName);
			}
		} 
		return tmpDirectory;
	}
}

