package protos;

import parsers.MainError;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by juanfra on 11/05/17.
 */
public enum ChannelState {
    uninitialized {
        @Override
        public void userAttend(KeyData data) {
            try {
                assert data.key.isAcceptable() : "Key is not acceptable";

                SocketChannel userChannel = ((ServerSocketChannel) data.key.channel()).accept();
                userChannel.configureBlocking(false);
                userChannel.register(data.key.selector(), SelectionKey.OP_READ, data);

                data.user.channel = userChannel;
                data.user.state = reading;

            } catch (IOException e) {
                System.err.println(" --- Error when accepting user request \n");
                e.printStackTrace();
            }
        }

        @Override
        public void serverAttend(KeyData data) {
            throw new RuntimeException("SERVER UNINITIALIZED?!");
        }
    },
    connecting{

        @Override
        public void userAttend(KeyData data) {
            throw new RuntimeException("USER CONECTING?!");
        }

        @Override
        public void serverAttend(KeyData data) {
            try {
                assert data.key.isConnectable() : "Key not connectable";

                if ( data.server.channel.finishConnect() ) {
                    data.server.channel.register(data.key.selector(), SelectionKey.OP_WRITE, data);
                    data.server.state = writing;

                } else {
                    data.key.cancel();
                    data.server.channel.close();
                    data.server.state = done;
                    data.user.channel.close();
                    data.user.state = done;

                }

            } catch (IOException e) {
                System.err.println(" --- Error when connecting to the server \n");
                e.printStackTrace();
            }
        }
    },
    reading {
        @Override
        public void userAttend(KeyData data) {

            try {
                int readBytes = read(data, data.user, data.server);

                Integer port = data.content.port;
                String host = data.content.host;

                if(data.server.state == uninitialized) {
                    if (port != null && host != null) {
                        data.server.channel = SocketChannel.open();
                        data.server.channel.configureBlocking(false);

                        KeyData serverKeyData = data.getPair();

                        if (data.server.channel.connect(new InetSocketAddress(host, port))) {
                            data.server.state = connecting;
                            data.server.channel.register(data.key.selector(), SelectionKey.OP_READ, serverKeyData);

                        } else {
                            data.server.state = writing;
                            data.server.channel.register(data.key.selector(), SelectionKey.OP_WRITE, serverKeyData);
                        }
                    }
                }

            }catch (IOException e){
                System.err.println(" --- Error when reading user request \n");
                e.printStackTrace();
            }
        }

        @Override
        public void serverAttend(KeyData data) {
            try {
                int readBytes = read(data, data.server, data.user);
            }catch (IOException e){
                System.err.println(" --- Error when reading user request \n");
                e.printStackTrace();
            }
        }

        private int read(KeyData data,ChannelData from,ChannelData to) throws IOException {
            assert data.key.isReadable() : "Key is not readable";

            int originalSize = data.buffer.remaining();

            data.content = ServerHandler.handleRead(
                    data.user.channel,
                    data.isUser? ConnectionState.REQUEST: ConnectionState.RESPONSE,
                    data.buffer);

            int readBytes = originalSize - data.buffer.remaining();

            assert readBytes > 0 : "COUlD NOT READ";
            assert data.content != null : "ERROR READING CONTENT NULL";

            MainError error = data.content.machine.error;
            boolean isComplete = data.content.isComplete;

            if (error != null) {
                //TODO
                return -1;
            }

            if(isComplete) {
                from.state = done;
            } else if(data.buffer.hasRemaining()) {
                from.state = waiting;
            } else {
                from.state = reading;
                from.channel.register(data.key.selector(),SelectionKey.OP_READ,data);
            }

            if(to.state == waiting || to.state == writing) {
                to.channel.register(data.key.selector(),SelectionKey.OP_WRITE,data);
            }

            return readBytes;
        }
    },
    writing {
        @Override
        public void userAttend(KeyData data) {
            try {
                write(data, data.user, data.server);
                if(data.server.state == done) {
                    data.user.state = waiting;
                    data.server.state = reading;
                    data.server.channel.register(data.key.selector(),SelectionKey.OP_READ,data);
                }
            }catch (IOException e){
                System.err.println(" --- Error when reading user request \n");
                e.printStackTrace();
            }
        }

        @Override
        public void serverAttend(KeyData data) {
            try {
                write(data, data.server, data.user);
            }catch (IOException e){
                System.err.println(" --- Error when reading user request \n");
                e.printStackTrace();
            }
        }

        private int write(KeyData data,ChannelData from,ChannelData to) throws IOException {
            assert data.key.isWritable() : "Key is not writable";

            int originalSize = data.buffer.capacity() - data.buffer.remaining();

            int writtenBytes = ServerHandler.handleWrite(
                    data.content,
                    data.user.channel,
                    data.isUser? ConnectionState.REQUEST: ConnectionState.RESPONSE,
                    data.buffer);

            assert writtenBytes > 0 : "COUlD NOT WRITE";

            MainError error = data.content.machine.error;

            if (error != null) {
                //TODO
                return -1;
            }

            if(data.buffer.hasRemaining()) {
                to.state = writing;
                to.channel.register(data.key.selector(),SelectionKey.OP_WRITE,data);
            } else if(from.state == done) {
                to.state = done;
            } else {
                to.state = waiting;
            }

            if( from.state == waiting || from.state == reading ) {
                from.channel.register(data.key.selector(),SelectionKey.OP_READ,data);
            }


            return writtenBytes;
        }

    },
    waiting,
    done;

    public void userAttend(KeyData data) {
        throw new UnsupportedOperationException();
    }
    public void serverAttend(KeyData data) {}

    private static final int TIMEOUT = 2000;
    private static final int BUFSIZE = 8 * 1024; // 8KB
    private static final int NONE = 0;
}
