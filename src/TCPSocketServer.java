import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.security.Key;
import java.util.Iterator;

/**
 * Created by epord on 20/04/17.
 */
public class TCPSocketServer {

    private static final int TIMEOUT = 2000;
    private static final int BUFSIZE = 8 * 1024; // 8KB
    private class KeyData {
        State state;
        ByteBuffer buffer;
        RequestContent content;
        SocketChannel userChannel;
        SocketChannel serverChannel;
        SelectionKey key;
    }
    private enum State {
        CONECTING {
            public void attend(KeyData data) {
                try {
                    SocketChannel serverSocket = ((ServerSocketChannel) data.key.channel()).accept();
                    serverSocket.configureBlocking(false);
                    data.state = LISTENINGREQUEST;
                    serverSocket.register(data.key.selector(), SelectionKey.OP_READ, data);
                } catch (CancelledKeyException e) {
                    System.out.println("Key was closed");
                    e.printStackTrace();
                }catch (IOException e) {
                    System.out.println("Error accepting connection");
                    e.printStackTrace();
                }
            }
        },
        LISTENINGREQUEST {
            public void attend(KeyData data) {
                try {
                    if(!data.key.isReadable()) {
                        throw new IllegalStateException("WAS LISTENING A NON READABLE KEY");
                    }

                    data.userChannel = (SocketChannel) data.key.channel();
                    data.buffer =  ByteBuffer.allocate(BUFSIZE);

                    data.content = ServerHandler.handleRead(
                            data.userChannel,
                            data.state.connectionState(),
                            data.buffer);

                    if (data.content.getType() == MethodType.CONNECT) {
                        System.out.println("HANDLE PERSISTENCE CONNECTION");
                        return;
                    }

                    data.key.cancel();
                    data.state = SENDINGREQUEST;
                    data.serverChannel = SocketChannel.open();
                    data.serverChannel.connect(new InetSocketAddress(data.content.getHost(), data.content.getPort()));
                    data.serverChannel.configureBlocking(false);
                    data.serverChannel.register(data.key.selector(), SelectionKey.OP_WRITE, data);
                } catch (CancelledKeyException e) {
                    System.out.println("Key was closed");
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("Error reading connection");
                    e.printStackTrace();
                }
            }
        },
        SENDINGREQUEST {
            public void attend(KeyData data) {
                try {
                    if(!data.key.isWritable()) {
                        throw new IllegalStateException("WAS LISTENING A NON READABLE KEY");
                    }

                    ServerHandler.handleWrite(
                            data.content,
                            data.serverChannel,
                            data.state.connectionState(),
                            data.buffer);

                    data.key.cancel();
                    data.state = LISTENINGRESPONSE;
                    data.userChannel.register(data.key.selector(), SelectionKey.OP_READ, data);
                } catch (CancelledKeyException e) {
                    System.out.println("Key was closed");
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("Error reading connection");
                    e.printStackTrace();
                }
            }
        },
        LISTENINGRESPONSE {
            public void attend(KeyData data) {
                try {
                    if(!data.key.isReadable()) {
                        throw new IllegalStateException("WAS LISTENING A NON READABLE KEY");
                    }

                    data.content = ServerHandler.handleRead(
                            data.serverChannel,
                            data.state.connectionState(),
                            data.buffer);

                    data.key.cancel();
                    data.state = SENDINGRESPONSE;
                    data.userChannel.register(data.key.selector(), SelectionKey.OP_WRITE, data);
                } catch (CancelledKeyException e) {
                    System.out.println("Key was closed");
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("Error reading connection");
                    e.printStackTrace();
                }
            }
        },
        SENDINGRESPONSE {
            public void attend(KeyData data) {
                try {
                    if(!data.key.isWritable()) {
                        throw new IllegalStateException("WAS LISTENING A NON READABLE KEY");
                    }

                    ServerHandler.handleWrite(
                            data.content,
                            data.userChannel,
                            data.state.connectionState(),
                            data.buffer);

                    data.key.cancel();
                    data.userChannel.close();
                    data.serverChannel.close();
                    data.state = CLOSING;
                } catch (CancelledKeyException e) {
                    System.out.println("Key was closed");
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("Error reading connection");
                    e.printStackTrace();
                }
            }
        },
        CLOSING;
        public void attend(KeyData data) {}

        ConnectionState connectionState() {
            switch (this) {
                case LISTENINGREQUEST:
                case SENDINGREQUEST:
                case CONECTING:
                    return ConnectionState.REQUEST;
                case LISTENINGRESPONSE:
                case SENDINGRESPONSE:
                    return ConnectionState.RESPONSE;
                default:
                    return null;
            }
        }
    }

    public void start() {

        Selector selector = null;
        //region INITIALIZE
        try {
            selector = Selector.open();

            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress(5050));
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            System.out.println("Error opening listener socket.");
            e.printStackTrace();
            return;
        }
        //endregion

        ServerHandler serverHandler = new ServerHandler(BUFSIZE);
        while (true) {
            //region SELECTING
            try {
                if (selector.select(TIMEOUT) == 0) {
                    continue;
                }
            } catch (IOException e) {
                System.out.println("Error in selector.");
            }
            //endregion

            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();

                if(key.isValid()) {
                    if (key.isAcceptable()) {
                        KeyData data = new KeyData();
                        data.state = State.CONECTING;
                        key.attach(data);
                    }

                    KeyData data = (KeyData) key.attachment();
                    data.key = key;
                    data.state.attend(data);
                }

                it.remove();
            }
        }
    }

}
