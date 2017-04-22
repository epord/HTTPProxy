import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by epord on 21/04/17.
 */
public class SocketContainer {
    private SocketChannel socket;
    private ByteBuffer buffer;

    public SocketContainer(SocketChannel socket, ByteBuffer buffer) {
        this.socket = socket;
        this.buffer = buffer;
    }

    public SocketChannel getSocket() {
        return socket;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }
}
