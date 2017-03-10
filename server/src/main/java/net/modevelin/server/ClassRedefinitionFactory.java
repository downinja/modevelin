package net.modevelin.server;

import static net.modevelin.common.bytes.support.JarBytesSupport.*;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import net.modevelin.common.bytes.BytesProvider;

public class ClassRedefinitionFactory {

	private final Map<String, BytesProvider> customProviders = new HashMap<>(); // so that an enclosing app can add additional providers like ExecutorsProvider
	
	private final Map<String, byte[]> initialDefinitions;

	public void setCustomProviders(final Map<String, BytesProvider> customProviders) {
		this.customProviders.putAll(customProviders);
	}
	
	public ClassRedefinitionFactory() throws Exception {

		this.initialDefinitions = new TreeMap<>();

		// obvs these would be driven by config not hard coded
		
		BytesProvider executorsProvider = (BytesProvider)Class.forName("net.modevelin.executors.ThreadPoolExecutorAsmProvider").newInstance();
		Map<String, byte[]> bytesMap = executorsProvider.getBytes(null);
		for (String className : bytesMap.keySet()) {
			initialDefinitions.put(className, bytesMap.get(className));
		}

		String tibrvJarPath = "C:\\Users\\john\\dev\\github\\modevelin\\tibrvj\\target\\modevelin-tibrvj-1.0.0-SNAPSHOT.jar";
		byte[] bytes = getJarBytes(tibrvJarPath, "com.tibco.tibrv.TibrvTransport");
		initialDefinitions.put("com/tibco/tibrv/TibrvTransport", bytes);

		bytes = getJarBytes(tibrvJarPath, "com.tibco.tibrv.TibrvListener");
		initialDefinitions.put("com/tibco/tibrv/TibrvListener", bytes);
		
		bytes = getJarBytes(tibrvJarPath, "net.modevelin.tibrv.AgentMessageHandler");
		initialDefinitions.put("net/modevelin/tibrv/AgentMessageHandler", bytes);

		bytes = getJarBytes(tibrvJarPath, "com.tibco.tibrv.TibrvRvdTransport");
		initialDefinitions.put("com/tibco/tibrv/TibrvRvdTransport", bytes);

		bytes = getJarBytes(tibrvJarPath, "com.tibco.tibrv.Tibrv");
		initialDefinitions.put("com/tibco/tibrv/Tibrv", bytes);

	}

	public Map<String, byte[]> getRedefinitions(final String agentName, final String command) {
		return initialDefinitions;
	}

}
