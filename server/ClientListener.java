

package server;

import java.nio.ByteBuffer;

/**
 * Defines a listener listening out for client messages
 * @author Andrew
 */
public interface ClientListener
{

    /**
     * Called when a message has been recieved
     * @param client the client that sent this message
     * @param message the content of the message
     */
    public void recievedMessage(ConnectedClient client, ByteBuffer message);

    /**
     * Called when a client disconnects from the server
     * @param client the client that disconnected (not able to send any more messages)
     */
    public void clientDisconnected(ConnectedClient client);
}
