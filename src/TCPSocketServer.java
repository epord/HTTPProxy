import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * Created by epord on 20/04/17.
 */
public class TCPSocketServer {

    private final int TIMEOUT = 2000;
    private final int BUFSIZE = 8 * 1024; // 8KB

    private enum State {
        CONECTING,
        LISTENINGREQUEST,
        SENDINGREQUEST,
        LISTENINGRESPONSE,
        SENDINGRESPONSE,
        CLOSING;

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

                if(!key.isValid()) {
                    continue;
                }

                if (key.isAcceptable()) {
                    key.attach(State.CONECTING);
                }

                State keyState = (State) key.attachment();

                switch (keyState) {
                    case CONECTING: break;

                    //region Listening
                    case LISTENINGREQUEST:
                    case LISTENINGRESPONSE:
                        if(!key.isReadable()) {
                            throw new IllegalStateException("WAS LISTENING A NON READABLE KEY");
                        }

                        try {
                            SocketChannel keySocket = (SocketChannel) key.channel();
                            RequestContent content = serverHandler.handleRead(keySocket, keyState.connectionState());
                            if(content.getType() == MethodType.CONNECT) {
                                System.out.println("HANDLE PERSISTENCE CONNECTION");
                                continue;
                            }
                            SocketChannel hostSocket;
                            switch (keyState) {
                                case LISTENINGREQUEST:
                                    System.out.println("Read:\n" + new String(content.getBody().array()));
                                    hostSocket = SocketChannel.open();
                                    hostSocket.connect(new InetSocketAddress(content.getHost(), content.getPort()));
                                    hostSocket.configureBlocking(false);
                                    hostSocket.register(key.selector(), SelectionKey.OP_WRITE, new SocketContainer(keySocket, content.getBody()));
                                    key.attach(State.SENDINGREQUEST);
                                    break;

                                case LISTENINGRESPONSE:
                                    //TODO get the channel
                                    break;
                            }
                        } catch (IOException e) {
                            System.out.println("Error reading connection");
                            e.printStackTrace();
                        }

                        break;
                    //endregion
                }

                        break;


                }

                if (key.isAcceptable()) {
                    try {
                        serverHandler.handleAccept(key);
                    } catch (IOException e) {
                        System.out.println("Error accepting connection");
                        e.printStackTrace();
                    }
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
