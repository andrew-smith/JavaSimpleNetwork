
package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A connection to a server
 * @author Andrew
 */
public abstract class ServerConnection extends Thread
{
    /** The socket to connect to */
    private Socket server;

    /** Listeners that are listening for messages */
    private ArrayList<ServerListener> listeners = new ArrayList<ServerListener>();

    /** Server loop */
    private boolean isRunning = false;

    private DataInputStream in;
    private DataOutputStream out;

    /**
     * @return the port of the remote server
     */
    public int getServerPort()
    {
        return server.getPort();
    }

    /**
     * Attaches a server listener to this server connection. Any messages that
     * are recieved will be broadcast to this listener
     * @param listener the listener to attach to this server connection
     * @return true if attached, false if error occurs
     */
    public boolean attachListener(ServerListener listener)
    {
        return listeners.add(listener);
    }

    /**
     * Detaches a listener from this server connection
     * @param listener the listener to detach
     * @return true if listener was removed, false if it was not found
     */
    public boolean detachListener(ServerListener listener)
    {
        return listeners.remove(listener);
    }


    /**
     * Sends a message to the server
     * @param message the message to send to the server
     * @throws IOException if an error occurs
     */
    public synchronized void sendMessage(ByteBuffer message) throws IOException
    {
        message.rewind();
        out.writeInt(message.array().length); //send length always first UNLESS it is an internal message
        out.write(message.array(), 0, message.array().length);
        out.flush();
    }

    /**
     * Sends a message to all the listners 
     * @param message the message to send
     */
    private void broadcastMessage(final ByteBuffer message)
    {
        for(ServerListener l : listeners) l.recieveMessage(message.duplicate());
    }

    /**
     * Starts the server connection
     */
    @Override
    public void run()
    {
        isRunning = true;

        while(isRunning)
        {
            try
            {
                if (in.available() > 0)
                {
                    //read the data
                    int len = in.readInt(); //first is always the length of the byte buffer
                    byte[] buf = new byte[len];
                    in.read(buf, 0, len);
                    broadcastMessage(ByteBuffer.wrap(buf));
                }

                //sleep to not waste cpu
                try {Thread.sleep(5);}
                catch (InterruptedException ex) {Logger.getLogger(ServerConnection.class.getName()).log(Level.SEVERE, null, ex);}
            }
            catch (IOException ex)
            {
                Logger.getLogger(ServerConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }


    /**
     * Detaches from server
     */
    public final void detach() throws IOException
    {
        //sends a signal to the server to say we are detaching
        ByteBuffer goodbyeMessage = ByteBuffer.allocate(5);
        goodbyeMessage.putInt(0);
        goodbyeMessage.put((byte)1);

        goodbyeMessage.rewind();
        out.write(goodbyeMessage.array(), 0, goodbyeMessage.array().length);
        out.flush();

        isRunning = false;

        //close the socket
        server.close();
    }

    /**
     * Gets a new server connection to a server
     * @param ip
     * @param port
     * @return
     * @throws UnknownHostException
     * @throws IOException
     */
    public static final ServerConnection getServerConnection(String ip, int port) throws UnknownHostException, IOException
    {
        //creates a new final server connection
        ServerConnection serverConnection = new ServerConnection() { };
        serverConnection.server = new Socket(ip, port);
        serverConnection.in = new DataInputStream(serverConnection.server.getInputStream());
        serverConnection.out = new DataOutputStream(serverConnection.server.getOutputStream());

        //return
        return serverConnection;
    }

}
