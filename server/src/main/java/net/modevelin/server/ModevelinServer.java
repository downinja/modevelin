package net.modevelin.server;

import net.modevelin.server.config.redefinitions.Redefinitions;
import net.modevelin.server.config.registrations.Registrations;
import net.modevelin.server.endpoints.Endpoint;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static net.modevelin.common.ExceptionSupport.rethrowAsRuntimeException;
import static org.slf4j.LoggerFactory.getLogger;

public class ModevelinServer {

    private final Logger logger = getLogger(getClass());

    private final ClassRedefinitionFactory redefinitionFactory;

    private final MessageHandler[] messageHandlers;

    private final Endpoint[] endpoints;

    private final Map<String, RegisteredAgent> agentMap = new HashMap<>(); // ConcurrentHashMap? Or just sync on map?

    private volatile boolean started;

    private ModevelinServer(final Builder builder) throws Exception { // TODO remove exception?
        this.endpoints = builder.endpoints;
        this.messageHandlers = builder.messageHandlers;
        for (Endpoint endpoint : endpoints) {
            endpoint.setServer(this);
        }
        this.redefinitionFactory = new ClassRedefinitionFactory(builder.registrations, builder.redefinitions);
    }

    public synchronized void start() {
        if (started) {
            logger.info("Call to ModevelinServer.start(), but already started");
            return;
        }

        started = true;
        for (Endpoint endpoint : endpoints) {
            try {
                endpoint.start();
            }
            catch (Exception ex) {
                logger.error("Unable to start endpoint {}", endpoint, ex);
                stop();
                rethrowAsRuntimeException(ex);
            }
        }
    }

    public synchronized void stop() {
        if (!started) {
            logger.warn("Call to ModevelinServer.stop(), but not running");
            return;
        }
        for (Endpoint endpoint : endpoints) {
            try {
                endpoint.stop();
            }
            catch (Exception ex) {
                logger.warn("Unable to stop endpoint {}", endpoint, ex);
            }
        }
        started = false;
    }

    public void handle(final Endpoint endpoint, final Properties message) {

        // Any "framework" messages (agent register, agent ready, etc) we handle here,
        // else we just delegate any fixture messages to our list of handlers

        String agentName = getAgentName(message);
        String command = message.getProperty("COMMAND");
        if ("REGISTER_AGENT".equals(command)) {
            registerAgent(endpoint, message);
            Map<String, byte[]> initialRedefinitions = redefinitionFactory.getRedefinitions(agentName);
            Properties response = new Properties();
            response.putAll(message);
            response.put("REDEFINITIONS", initialRedefinitions);
            try {
                send(agentName, response);
            }
            catch (Exception ex) {
                rethrowAsRuntimeException(ex);
            }
        }
        else if ("READY".equals(command)) {
            RegisteredAgent registeredAgent = agentMap.get(agentName);
            registeredAgent.setReady(true);
        }
        else {
            for (MessageHandler handler : messageHandlers) {
                handler.handle(message);
            }
        }
    }

    public boolean isReady(final String agentName) {
        RegisteredAgent registeredAgent = agentMap.get(agentName);
        return registeredAgent != null && registeredAgent.isReady();
    }

    // TODO, move to common utility class?
    private String getAgentName(final Properties message) {
        return message.getProperty("NAME");
    }

    private void registerAgent(final Endpoint endpoint, final Properties message) {
        String agentName = getAgentName(message);
        RegisteredAgent agentRegisteredAgent = new RegisteredAgent(endpoint);
        this.agentMap.put(agentName, agentRegisteredAgent);
    }

    public void send(final String agentName, final Properties response) throws Exception {
        RegisteredAgent registeredAgent = this.agentMap.get(agentName);
        registeredAgent.endpoint.send(agentName, response);
    }

    private class RegisteredAgent {
        private final Endpoint endpoint;
        private volatile boolean ready;
        private RegisteredAgent(final Endpoint endpoint) {
            this.endpoint = endpoint;
        }
        public void setReady(final boolean ready) {
            this.ready = ready;
        }
        public boolean isReady() {
            return ready;
        }
    }

    public static class Builder {

        private Endpoint[] endpoints = new Endpoint[0];
        private MessageHandler[] messageHandlers = new MessageHandler[0];
        private Registrations registrations = null;
        private Redefinitions redefinitions = null;

        public Builder withRedefinitions(final Redefinitions redefinitions) {
            this.redefinitions = redefinitions;
            return this;
        }

        public Builder withRegistrations(final Registrations registrations) {
            this.registrations = registrations;
            return this;
        }

        public Builder withEndpoints(final Endpoint... endpoints) {
            this.endpoints = endpoints;
            return this;
        }

        public Builder withMessageHandlers(final MessageHandler... messageHandlers) {
            this.messageHandlers = messageHandlers;
            return this;
        }

        public ModevelinServer build() throws Exception {
            return new ModevelinServer(this);
        }

    }

}
