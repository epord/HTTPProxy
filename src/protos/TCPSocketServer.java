package protos;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by epord on 20/04/17.
 */
public class TCPSocketServer {

    private static final int TIMEOUT = 2000;
    private static final int BUFSIZE = 8 * 1024; // 8KB
    private static final int NONE = 0;
    private class KeyData {
        State state;
        ByteBuffer buffer;
        RequestContent content;
        SocketChannel userChannel;
        SocketChannel serverChannel;
        SelectionKey key;
        int Id;
    }
    private enum State {
        CONNECTING {
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

                    if(data.content == null ){
                        System.out.println("CLOSING ERROR");
                        data.userChannel.close();
                        return;
                    }

                    data.state = SENDINGREQUEST;
                    data.serverChannel = SocketChannel.open();
                    data.serverChannel.configureBlocking(false);

                    Integer port = data.content.getPort();
                    String host = data.content.getHost();
                    data.key.interestOps(NONE);

                    //TODO delete this
                    if(host==null) host="www.google.com.ar";

                    if ( data.serverChannel.connect(new InetSocketAddress(host,port)) ) {
                        data.state = LISTENINGRESPONSE;
                        data.serverChannel.register(data.key.selector(), SelectionKey.OP_READ, data);
                    } else {
                        data.state = CONNECTINGORIGIN;
                        data.serverChannel.register(data.key.selector(),SelectionKey.OP_CONNECT,data);
                    }

                } catch (CancelledKeyException e) {
                    System.out.println("Key was closed");
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("Error reading connection");
                    e.printStackTrace();
                }
            }
        },
        CONNECTINGORIGIN {
            public void attend(KeyData data) {
                try {
                    if (!data.key.isConnectable()) {
                        throw new IllegalStateException("WAS TRYING TO CONNECT A NON CONNECTABLE KEY");
                    }

                    if ( data.serverChannel.finishConnect() ) {
                        data.state = SENDINGREQUEST;
                        data.key.interestOps(NONE);
                        data.serverChannel.register(data.key.selector(), SelectionKey.OP_WRITE, data);
                    }

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
                        throw new IllegalStateException("WAS TRYING TO WRITE A NON WRITABLE KEY");
                    }

                    ServerHandler.handleWrite(
                            data.content,
                            data.serverChannel,
                            data.state.connectionState(),
                            data.buffer);

//                    System.out.println("SENDING REQUEST:\n" + new String(data.buffer.array()));

                    data.state = LISTENINGRESPONSE;
                    data.key.interestOps(NONE);
                    data.serverChannel.register(data.key.selector(), SelectionKey.OP_READ, data);
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

                    if(data.content == null ){
                        data.userChannel.close();
                        data.serverChannel.close();
                        return;
                    }

                    data.state = SENDINGRESPONSE;
                    data.key.interestOps(NONE);
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
                        throw new IllegalStateException("WAS TRYING TO WRITE A NON WRITABLE KEY");
                    }

                    ServerHandler.handleWrite(
                            data.content,
                            data.userChannel,
                            data.state.connectionState(),
                            data.buffer);

//                    System.out.println("SENDING RESPONSE:\n" + new String(data.buffer.array()));

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
                case CONNECTING:
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
                    System.err.println("Timeout");
                }

                //region Logging
                    System.err.println("--------------------------------------------------");
                    System.err.println(selector.keys().size() + " keys in the selector");
                    selector.keys().forEach(
                            (k)-> {
                                KeyData data = (KeyData) k.attachment();
                                if(data != null) {
                                    System.err.println("Key id:" + data.Id);
                                    System.err.println("Key state:" + data.state.name());
                                    if(data.content!=null)
                                    System.err.println("Key request:" + data.content.host);

                                } else {
                                    System.err.println("Key not identified yet");
                                }

                                if(k.interestOps() == SelectionKey.OP_READ) {
                                    System.err.println("---- Read");
                                }

                                if(k.interestOps() == SelectionKey.OP_ACCEPT) {
                                    System.err.println("---- Accept");
                                }

                                if(k.interestOps() == SelectionKey.OP_WRITE) {
                                    System.err.println("---- Write");
                                }

                                if(k.interestOps() == SelectionKey.OP_CONNECT) {
                                    System.err.println("---- Connect");
                                }

                                System.err.println("");
                            }
                    );

                //endregion

            } catch (IOException e) {
                System.out.println("Error in selector.");
            }

            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();

                KeyData data;
                if(key.isValid()) {
                    if (key.isAcceptable()) {
                        data = new KeyData();
                        data.key = key;
                        data.state = State.CONNECTING;
                        data.Id = new Random().nextInt();
                    } else {
                        data = (KeyData) key.attachment();
                    }

                    System.out.println(data.state);
                    data.key = key;
                    data.state.attend(data);

                }

            }
        }
    }

}
