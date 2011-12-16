
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple server application
 * @author Andrew
 */
public class Server extends Thread
{

    /** Server Socket */
    private ServerSocket server;

    /* Is Server Running */
    private boolean serverRunning;

    /** List of connected clients */
    private ArrayList<ConnectedClient> clients;

    private ArrayList<ServerConnectionListener> listeners;

    /**
     * Creates a new server with the specified port number
     * @param portNum the port number for the server to listen on
     * @throws IOException
     */
    public Server(int portNum) throws IOException
    {
        server = new ServerSocket(portNum);
        serverRunning = false;
        clients = new ArrayList<ConnectedClient>();
        listeners = new ArrayList<ServerConnectionListener>();
    }


    /**
     * Attaches a new listener to this server to listen for incomming clients
     * connecting
     * @param listener the listener to attach
     * @return true if listener was added, false if not
     */
    public boolean attachListener(ServerConnectionListener listener)
    {
        return listeners.add(listener);
    }

    /**
     * Detaches an attached listener 
     * @param listener listener to detach
     * @return true if was detached, false if not found
     */
    public boolean detachListener(ServerConnectionListener listener)
    {
        return listeners.remove(listener);
    }

    /**
     * @return the port number of this server
     */
    public int getPort()
    {
        return server.getLocalPort();
    }

    /**
     * Adds a new client to this server and broadcasts it
     * @param client the new client to add
     */
    private void addNewClient(ConnectedClient client)
    {
        Logger.getLogger(Server.class.getName()).log(Level.INFO, null, "Client Connected: " + client);
        clients.add(client);
        //broadcast this new client
        for(ServerConnectionListener c : listeners) c.clientConnected(client);
    }


    /**
     * Starts and runs the server
     */
    @Override
    public void run()
    {
        serverRunning = true;

        while(serverRunning)
        {
            try
            {
                System.out.println("Server Running");
                ConnectedClient nClient = new ConnectedClient(server.accept());
                addNewClient(nClient);
            } 
            catch (IOException ex)
            {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, "Server could not connect to client");
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
