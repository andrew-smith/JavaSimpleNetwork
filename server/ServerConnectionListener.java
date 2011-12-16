

package server;

/**
 * Listens out for new clients connecting to the server
 * @author Andrew
 */
public interface ServerConnectionListener
{
    /**
     * Called when a new client has connected to this server
     * @param client the new client that just connected
     */
    public void clientConnected(ConnectedClient client);
}
