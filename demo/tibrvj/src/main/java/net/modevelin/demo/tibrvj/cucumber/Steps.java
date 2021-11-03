package net.modevelin.demo.tibrvj.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import net.modevelin.server.DefaultMessageHandler;
import net.modevelin.server.ModevelinServer;
import net.modevelin.server.endpoints.socket.DefaultSocketEndpoint;

import java.util.Properties;
import java.util.logging.Logger;

import static net.modevelin.server.config.jaxb.JaxbConfigUtil.unmarshallRedefinitions;
import static net.modevelin.server.config.jaxb.JaxbConfigUtil.unmarshallRegistrations;

public class Steps {

    private static final Logger LOGGER = Logger.getLogger(Steps.class.getName());

    private DefaultMessageHandler messageHandler;
    private ModevelinServer modevelinServer;

    @Given("a modevelin server running on port {int}")
    public void setUpModevelinServer(final int receivePort) throws Exception {
        messageHandler = new DefaultMessageHandler();
        modevelinServer = new ModevelinServer.Builder()
                .withMessageHandlers(messageHandler)
                .withRedefinitions(unmarshallRedefinitions("/redefinitions.xml"))
                .withRegistrations(unmarshallRegistrations("/registrations.xml"))
                .withEndpoints(new DefaultSocketEndpoint(receivePort)).build();
        modevelinServer.start();
    }

    @When("agent {string} is ready")
    public void agentIsReady(final String agentName) throws Exception {
        for (int attempt=0; attempt < 100; attempt++) {
            //LOGGER.info("waiting for " + agentName + " to be ready [attempt" + attempt);

            // Probably we want to move "isReady" into a Server API?
            // I mean, strictly speaking it *is* in response to a message, but is more the state of the server/agent environment?
            // Or is a Server API just OTT to begin with? We could provide a DefaultMessageHandler, for this kind of thing,
            // for convenience - without trying to make it part of the furniture? Even then, do we want to try and prescribe
            // this behaviour, or is it simple enough to just let people roll their own
            if (modevelinServer.isReady(agentName)) {
                return;
            }
            Thread.sleep(500);
        }
        throw new RuntimeException(agentName  + " hasn't registered");
    }

    @When("send tibrvj message to agent {string} with messageId {string} body {string} SENDSUBJ {string} and REPLYSUB {string}")
    public void sendTibrvjMessageToAgent(final String agentName,
                                         final String modevelinMessageId,
                                         final String body,
                                         final String sendSubj,
                                         final String replySub) throws Exception {

        Properties response = new Properties();
        response.put("modevelin.message.id", modevelinMessageId);
        response.put("COMMAND", "TIBMSG");
        response.put("BODY", body);
        response.put("SENDSUBJ", sendSubj);
        response.put("REPLYSUB", replySub);
        modevelinServer.send(agentName, response);
    }

    @When("receive tibrvjMessage from agent {string} with messageId {string} body {string}")
    public void receiveTibrvjMessageFromAgent(final String agentName,
                                              final String modevelinMessageId,
                                              final String body) {
        boolean received = messageHandler.isReceived(modevelinMessageId);
        if (received) {
            Properties message = messageHandler.get(modevelinMessageId);
            if (message == null) {
                throw new IllegalStateException("Message " + modevelinMessageId + " is null");
            }
            String messageBody = message.getProperty("BODY");
            if (!body.equals(messageBody)) {
                throw new IllegalStateException("Expected BODY= " + body + " but got " + messageBody + " for messageId " + modevelinMessageId);
            }
            if (!message.getProperty("NAME").equals(agentName)) {
                throw new IllegalStateException("Expected agentName= " + agentName + " but got " + message.getProperty("NAME") + " for messageId " + modevelinMessageId);
            }
        }
        else {
            throw new IllegalStateException("Message " + modevelinMessageId + " not received");
        }
    }


}
