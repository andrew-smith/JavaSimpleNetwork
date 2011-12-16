
package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrew
 */
public class ConnectedClient extends Thread
{
    /** The client socket */
    private Socket client;

    //input and output streams
    private DataOutputStream out;
    private DataInputStream in;

    /** All the listeners listening for messages */
    private ArrayList<ClientListener> listeners;

    /**
     * Creates a new connected client with the specified socket.
     * Starts listening for messages automatically (run())
     * @param client
     */
    public ConnectedClient(Socket client) throws IOException
    {
        this.client = client;
        out = new DataOutputStream(client.getOutputStream());
        in = new DataInputStream(client.getInputStream());

        listeners = new ArrayList<ClientListener>();

        //automatically starts listening for incomming connections
        start();
    }


    /**
     * Attaches a new listener to this client to listen for incomming client
     * messages
     * @param listener the listener to attach
     * @return true if listener was added, false if not
     */
    public boolean attachListener(ClientListener listener)
    {
        return listeners.add(listener);
    }

    /**
     * Detaches an attached listener
     * @param listener listener to detach
     * @return true if was detached, false if not found
     */
    public boolean detachListener(ClientListener listener)
    {
        return listeners.remove(listener);
    }

    /**
     * Broadcasts a message to all the listeners
     * @param message the message to broadcast
     */
    private void broadcastMessage(ByteBuffer message)
    {
        for(ClientListener l : listeners) l.recievedMessage(this, message);
    }

    /**
     * Sends a message to all the listeners to say this client has disconnected
     */
    private void disconnectClient()
    {
        for(ClientListener l : listeners) l.clientDisconnected(this);
    }

    /**
     * Sends a message to this client
     * @param message a message to send
     * @throws IOException if an exception occured
     */
    public void sendMessage(ByteBuffer message) throws IOException
    {
        message.rewind();
        out.writeInt(message.array().length); //writes the length of the buffer first
        out.write(message.array(), 0, message.array().length);
        out.flush();
    }


    /**
     * Disconnects the client and stops the thread
     * @throws IOException
     */
    public void disconnect() throws IOException
    {
        client.close();
    }

    /**
     * Runs a listener in the background to listen for incomming messages
     */
    @Override
    public void run()
    {
        while(client.isConnected())
        {
            try 
            {
                if (in.available() != 0)
                {
                    int len = in.readInt(); //length of the message (length of byte buffer)

                    if(len == 0) //then this is an internal message
                    {
                        byte message = in.readByte();
                        if(message == (byte)1) //this means client is disconnecting
                        {
                            disconnectClient();
                        }
                    }
                    else
                    {
                        System.out.println("Creating buffer: " + len );
                        byte[] buf = new byte[len];
                        in.read(buf, 0, len);

                        ByteBuffer message = ByteBuffer.allocate(len);
                        for(int i=0; i<len; i++)
                            message.put(buf[i]);
                        message.rewind();
                        broadcastMessage(message);

                        System.out.print("Message: ");
                        for(int i=0; i<len; i++)
                            System.out.print("" + buf[i]);
                        System.out.println();
                    }
                }

                //sleep to not waste cpu
                try {Thread.sleep(5);}  //TODO is this needed?
                catch (InterruptedException ex) {Logger.getLogger(ConnectedClient.class.getName()).log(Level.SEVERE, null, ex);}
            }
            catch (IOException ex)
            {   Logger.getLogger(ConnectedClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Logger.getLogger(ConnectedClient.class.getName()).log(Level.INFO, null, "Client (" + toString() + ") Disconnected");
    }

    @Override
    public String toString()
    {
        return client.toString();
    }
}
