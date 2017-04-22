import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * Created by epord on 20/04/17.
 */
public class TCPSocketServer {

    private final int TIMEOUT = 2000;
    private final int BUFSIZE = 8 * 1024; // 8KB

    public void start() {
        Selector selector = null;
        try {
            selector = Selector.open();

            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress(5050));
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            System.out.println("Error opening listener socket.");
            e.printStackTrace();
        }

        ServerHandler serverHandler = new ServerHandler(BUFSIZE);
        while (true) {
            try {
                if (selector.select(TIMEOUT) == 0) {
                    continue;
                }
            } catch (IOException e) {
                System.out.println("Error in selector.");
            }

            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                if (key.isAcceptable()) {
                    try {
                        serverHandler.handleAccept(key);
                    } catch (IOException e) {
                        System.out.println("Error accepting connection");
                        e.printStackTrace();
                    }
                }

                if(key.isConnectable()) {
                    // Connectable stuff
                }

                if (key.isValid() && key.isReadable()) {
                    try {
                        serverHandler.handleRead(key);
                    } catch (IOException e) {
                        System.out.println("Error reading connection");
                        e.printStackTrace();
                    }
                }

                if (key.isValid() && key.isWritable()) {
                    try {
                        serverHandler.handleWrite(key);
                    } catch (IOException e) {
                        System.out.println("Error writing connection");
                        e.printStackTrace();
                    }
                }

                it.remove();
            }
        }
    }

}
