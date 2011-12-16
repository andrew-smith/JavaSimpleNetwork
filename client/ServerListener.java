

package client;

import java.nio.ByteBuffer;

/**
 * An interface that listens for server messages
 * @author Andrew
 */
public interface ServerListener {

    public void recieveMessage(ByteBuffer message);
}
