package net.modevelin.demo.tibrvj.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.JAXB;

import net.modevelin.common.bytes.BytesProvider;
import net.modevelin.common.config.fixtures.Fixture;
import net.modevelin.common.config.fixtures.Fixtures;
import net.modevelin.common.config.redefinitions.Provider;
import net.modevelin.common.config.redefinitions.Configuration;

public class ClassRedefinitionFactory {

	private final Map<Key, Map<String, byte[]>> agentFixtureRedefinitionsMap;
	
	public ClassRedefinitionFactory(final String fixturesFile, final String redefinitionsFile) throws Exception {
		Map<String, Map<String, byte[]>> redefinitionsMap = getRedefinitionsMap(redefinitionsFile);
		this.agentFixtureRedefinitionsMap = parseFixtures(fixturesFile, redefinitionsMap);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Map<String, byte[]>> getRedefinitionsMap(final String redefinitionsFile) throws Exception {
		
		Map<String, Map<String, byte[]>> redefinitionsMap = new TreeMap<>();
		net.modevelin.common.config.redefinitions.Redefinitions redefinitions = unmarshallRedefinitions(redefinitionsFile);
		
		for (net.modevelin.common.config.redefinitions.Redefinition redefinition : redefinitions.getRedefinitions()) {
			for (Provider provider : redefinition.getProviders()) {
				String providerClass = provider.getProviderClass();
				net.modevelin.common.config.redefinitions.Configuration configuration = provider.getConfiguration();
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
	
	private Map<Key, Map<String, byte[]>> parseFixtures(final String fixturesFile, final Map<String, Map<String, byte[]>> redefinitionsMap) throws Exception {
		Map<Key, Map<String, byte[]>> map = new HashMap<>();
		Fixtures fixtures = unmarshallFixtures(fixturesFile);
		for (Fixture fixture : fixtures.getFixtures()) {
			for (String agent : fixture.getAgents()) {
				for (String messageType : fixture.getMessageTypes()) {
					Key key = new Key(agent, messageType);
					Map<String, byte[]> existingRefinitionsBytes = map.get(key);
					if (existingRefinitionsBytes == null) {
						existingRefinitionsBytes = new TreeMap<>();
						map.put(key, existingRefinitionsBytes);
					}
					for (String redefinitionId : fixture.getRedefinitions()) {
						Map<String, byte[]> redefinitionsBytes = redefinitionsMap.get(redefinitionId);
						existingRefinitionsBytes.putAll(redefinitionsBytes);
					}
				}
			}
		}
		return map;
	}

	private net.modevelin.common.config.redefinitions.Redefinitions unmarshallRedefinitions(final String redefinitionsFile) throws IOException {
		try (InputStream is = getClass().getResourceAsStream(redefinitionsFile)) {
			return JAXB.unmarshal(is, net.modevelin.common.config.redefinitions.Redefinitions.class);
		}
	}
	
	private Fixtures unmarshallFixtures(final String fixturesFile) throws IOException {
		try (InputStream is = getClass().getResourceAsStream(fixturesFile)) {
			return JAXB.unmarshal(is, Fixtures.class);
		}
	}

	public Map<String, byte[]> getRedefinitions(final String fixture, final String agentName) {
		final Key key = new Key(agentName, fixture);
		return agentFixtureRedefinitionsMap.get(key);
	}
	
	private final class Key {
		private String agent;
		private String messageType;
		private Key(final String agent, final String messageType) {
			this.agent = agent;
			this.messageType = messageType;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((agent == null) ? 0 : agent.hashCode());
			result = prime * result + ((messageType == null) ? 0 : messageType.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (agent == null) {
				if (other.agent != null)
					return false;
			} else if (!agent.equals(other.agent))
				return false;
			if (messageType == null) {
				if (other.messageType != null)
					return false;
			} else if (!messageType.equals(other.messageType))
				return false;
			return true;
		}
		private ClassRedefinitionFactory getOuterType() {
			return ClassRedefinitionFactory.this;
		}
		
	}

}
