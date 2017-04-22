import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by epord on 20/04/17.
 */
public class ServerHandler {

    private int bufSize;

    public ServerHandler(int bufSize) {
        this.bufSize = bufSize;
    }

    public void handleAccept(SelectionKey key) throws IOException {
        SocketChannel serverSocket = ((ServerSocketChannel) key.channel()).accept();
        serverSocket.configureBlocking(false);
        serverSocket.register(key.selector(), SelectionKey.OP_READ);
    }

    public void handleRead(SelectionKey key) throws IOException {
        SocketChannel keySocket = (SocketChannel) key.channel();
        ByteBuffer buf = ByteBuffer.allocate(bufSize);

        long bytesRead = keySocket.read(buf);
        if (bytesRead == -1) { // Did the other end close?
            keySocket.close();
        } else if (bytesRead > 0) {
            RequestContent requestContent = processRequest(buf);
            switch (requestContent.getType()) {
                case GET:
                System.out.println("Read:\n" + new String(buf.array()));
                    SocketChannel hostSocket = SocketChannel.open();
                    hostSocket.connect(new InetSocketAddress(requestContent.getHost(), requestContent.getPort()));
                    hostSocket.configureBlocking(false);
                    hostSocket.register(key.selector(), SelectionKey.OP_WRITE, new SocketContainer(keySocket, buf));
                    break;
                case OTHER:
                    // Write response to attachment socket
                    SocketChannel socketToWrite = (SocketChannel) key.attachment();
                    if (socketToWrite != null && socketToWrite.isOpen()) {
                        socketToWrite.configureBlocking(false);
                        socketToWrite.register(key.selector(), SelectionKey.OP_WRITE, new SocketContainer(keySocket, buf));
                    }
                    break;
            }
        }
    }

    public void handleWrite(SelectionKey key) throws IOException {
        SocketContainer socketContainer = (SocketContainer) key.attachment();
        ByteBuffer buf = socketContainer.getBuffer();
        SocketChannel socketToWrite = (SocketChannel) key.channel();
        socketToWrite.configureBlocking(false);

        if (socketToWrite.isOpen()) {
            buf.flip();
            socketToWrite.write(buf);
            socketToWrite.register(key.selector(), SelectionKey.OP_READ, socketContainer.getSocket());
        }
    }

    private static RequestContent processRequest(ByteBuffer buffer) {
        SimpleParser simpleParser = new SimpleParser();
        return simpleParser.parse(buffer);
    }
}
