package protos;

import parsers.MainError;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by epord on 20/04/17.
 */
public class TCPSocketServer2 {

    private static final int TIMEOUT = 2000;
    private static final int BUFSIZE = 8 * 1024; // 8KB;
    private static final int NONE = 0;

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
                boolean logging = true;
                //region Logging
                    if(logging) {
                        System.err.println("--------------------------------------------------");
                        System.err.println(selector.keys().size() + " keys in the selector");
                        selector.keys().forEach(
                                (k) -> {
                                    KeyData data = (KeyData) k.attachment();
                                    if (data != null) {
                                        System.err.println("Key id:" + data.Id);
                                        System.err.println("Key Userstate:" + data.user.state);
                                        System.err.println("Key Serverstate:" + data.server.state);
                                        if (data.content != null)
                                            System.err.println("Key request:" + data.content.getHost());

                                    } else {
                                        System.err.println("Key not identified yet");
                                    }

                                    if (k.interestOps() == SelectionKey.OP_READ) {
                                        System.err.println("---- Read");
                                    }

                                    if (k.interestOps() == SelectionKey.OP_ACCEPT) {
                                        System.err.println("---- Accept");
                                    }

                                    if (k.interestOps() == SelectionKey.OP_WRITE) {
                                        System.err.println("---- Write");
                                    }

                                    if (k.interestOps() == SelectionKey.OP_CONNECT) {
                                        System.err.println("---- Connect");
                                    }

                                    System.err.println("");
                                }
                        );
                    }

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
                        data = KeyData.userKeyData(BUFSIZE);
                    } else {
                        data = (KeyData) key.attachment();
                        key.interestOps(NONE);
                    }

                    String from = data.user.state.toString() + " , " + data.server.state;
                    data.key = key;
                    if(data.isUser) {
                        data.user.state.userAttend(data);
                    } else {
                        data.server.state.serverAttend(data);
                    }

                    if(data.user.state == ChannelState.done && data.server.state == ChannelState.done) {
                        data.key.cancel();
                        try {
                            data.user.channel.close();
                            data.server.channel.close();
                        }catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    System.out.println(from + " -> " + data.user.state + " , " + data.server.state);
                }
            }
        }
    }

    private static void printBuffer(byte [] buffer){
        StringBuffer str = new StringBuffer();
        for (byte c: buffer ) {
            str.append((char)c);
        }
        System.out.println(str.toString());
    }


}
