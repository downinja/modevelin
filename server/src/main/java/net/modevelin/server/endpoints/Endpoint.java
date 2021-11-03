package net.modevelin.server.endpoints;

import net.modevelin.server.ModevelinServer;

public interface Endpoint {

    void send(String agentName, Object sendObj) throws Exception ; // or is this always a Properties?
    void setServer(ModevelinServer server);
    void start() throws Exception;
    void stop() throws Exception;

}
