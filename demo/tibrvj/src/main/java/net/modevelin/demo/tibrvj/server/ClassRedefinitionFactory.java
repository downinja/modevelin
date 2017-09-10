package net.modevelin.demo.tibrvj.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.JAXB;

import net.modevelin.common.bytes.BytesProvider;
import net.modevelin.common.config.Configuration;
import net.modevelin.common.config.Provider;
import net.modevelin.common.config.Redefinition;
import net.modevelin.common.config.Redefinitions;

public class ClassRedefinitionFactory {
	
	private final Map<String, Map<String, byte[]>> agentRedefinitions;
	
	@SuppressWarnings("unchecked")
	public ClassRedefinitionFactory(final String redefinitionsFile) throws Exception {

		this.agentRedefinitions = new TreeMap<>();
		Redefinitions redefinitions = unmarshallRedefinitions(redefinitionsFile);
		
		for (Redefinition redefinition : redefinitions.getRedefinitions()) {
			for (Provider provider : redefinition.getProviders()) {
				String providerClass = provider.getProviderClass();
				Configuration configuration = provider.getConfiguration();
				List<String> classNames = configuration.getClasses();
				Class<BytesProvider> bytesProviderClass = (Class<BytesProvider>)Class.forName(providerClass);
				BytesProvider bytesProvider = bytesProviderClass.getConstructor(Configuration.class).newInstance(configuration);
				Map<String, byte[]> redefinitionsBytes = bytesProvider.getBytes(classNames);
				List<String> agents = redefinition.getAgents();
				for (String agent : agents) {
					Map<String, byte[]> existingAgentRedefinitions = agentRedefinitions.get(agent);
					if (existingAgentRedefinitions == null) {
						existingAgentRedefinitions = new HashMap<>();
						agentRedefinitions.put(agent, existingAgentRedefinitions);
					}
					existingAgentRedefinitions.putAll(redefinitionsBytes);
				}
			}
		}
	}
	
	private Redefinitions unmarshallRedefinitions(final String redefinitionsFile) throws IOException {
		try (InputStream is = ServerMain.class.getResourceAsStream(redefinitionsFile)) {
			return JAXB.unmarshal(is, Redefinitions.class);
		}
	}

	public Map<String, byte[]> getRedefinitions(final String agentName, final String command) {
		return agentRedefinitions.get(agentName);
	}

}
