package net.modevelin.server;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class DefaultMessageHandler implements MessageHandler {

    private static final Logger LOGGER = Logger.getLogger(DefaultMessageHandler.class.getName());
    // Do we need to Map by agentName, first, and then by messageId? Or can we assume that messageIds are globally unique?
    // (Can we even assume that messageIds are practical in all situations? How else would we look them up, except by
    // matching field by field according to some WHERE clause?)
    private final Map<String, Properties> messages = new ConcurrentHashMap<>();

    @Override
    public void handle(final Properties message) {
        messages.put(message.getProperty("modevelin.message.id"), message); // TODO, what should the name of the key be?
    }

    public boolean isReceived(final String messageId) {
        return isReceived(messageId, 10, 500);
    }

    public boolean isReceived(final String messageId, final int maxAttempts, final long sleepInterval) {
        boolean received = messages.containsKey(messageId);
        if (received) {
            return true;
        }
        if (maxAttempts > 0) {
            int attempt = 0;
            while (attempt++ < maxAttempts) {
                if (messages.containsKey(messageId)) {
                    return true;
                }
                try {
                    Thread.sleep(sleepInterval);
                }
                catch (Exception ex) {
                    LOGGER.warning("Unable to sleep: " + ex);
                }
            }
        }
        return messages.containsKey(messageId);
    }

    public Properties get(final String messageId) {
        return messages.get(messageId);
    }

    public Properties getAndRemove(final String messageId) {
        return messages.remove(messageId);
    }
}
