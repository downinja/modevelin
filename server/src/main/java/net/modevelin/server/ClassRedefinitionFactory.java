package net.modevelin.server;

import net.modevelin.common.BytesProvider;
import net.modevelin.server.config.redefinitions.Configuration;
import net.modevelin.server.config.redefinitions.Provider;
import net.modevelin.server.config.redefinitions.Redefinition;
import net.modevelin.server.config.redefinitions.Redefinitions;
import net.modevelin.server.config.registrations.Registration;
import net.modevelin.server.config.registrations.Registrations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ClassRedefinitionFactory {

	private final Map<String, Map<String, byte[]>> agentRedefinitionsMap;

	public ClassRedefinitionFactory(final Registrations registrations, final Redefinitions redefinitions) throws Exception {
		Map<String, Map<String, byte[]>> redefinitionsMap = getRedefinitionsMap(redefinitions);
		this.agentRedefinitionsMap = parseRegistrations(registrations, redefinitionsMap);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Map<String, byte[]>> getRedefinitionsMap(final Redefinitions redefinitions) throws Exception {
		
		Map<String, Map<String, byte[]>> redefinitionsMap = new TreeMap<>();

		for (Redefinition redefinition : redefinitions.getRedefinitions()) {
			for (Provider provider : redefinition.getProviders()) {
				String providerClass = provider.getProviderClass();
				Configuration configuration = provider.getConfiguration();
				List<String> classNames = configuration.getClasses();
			
				Class<BytesProvider> bytesProviderClass = (Class<BytesProvider>)Class.forName(providerClass);
				BytesProvider bytesProvider = bytesProviderClass.getConstructor(Configuration.class).newInstance(configuration);
				Map<String, byte[]> redefinitionsBytes = bytesProvider.getBytes(classNames);
				String id = redefinition.getId();
				redefinitionsMap.put(id, redefinitionsBytes);
			}
		}
		return redefinitionsMap;
	}
	
	private Map<String, Map<String, byte[]>> parseRegistrations(final Registrations registrations, final Map<String, Map<String, byte[]>> redefinitionsMap) throws Exception {
		Map<String, Map<String, byte[]>> map = new HashMap<>();
		for (Registration registration : registrations.getRegistrations()) {
			for (String agent : registration.getAgents()) {
				Map<String, byte[]> existingRedefinitionsBytes = map.get(agent);
				if (existingRedefinitionsBytes == null) {
					existingRedefinitionsBytes = new TreeMap<>();
					map.put(agent, existingRedefinitionsBytes);
				}
				for (String redefinitionId : registration.getRedefinitions()) {
					Map<String, byte[]> redefinitionsBytes = redefinitionsMap.get(redefinitionId);
					existingRedefinitionsBytes.putAll(redefinitionsBytes);
				}
			}
		}
		return map;
	}

	public Map<String, byte[]> getRedefinitions(final String agentName) {
		return agentRedefinitionsMap.get(agentName);
	}

}
