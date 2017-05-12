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
                if(!data.key.isAcceptable()) throw new IllegalStateException("Key is not acceptable");

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
                if(!data.key.isConnectable()) throw new IllegalStateException("Key is not connectable");

                if ( data.server.channel.finishConnect() ) {
                    data.server.state = writing;
                    data.server.channel.register(data.key.selector(), SelectionKey.OP_WRITE, data);

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

                if (port != null || host != null) {
                    host = "lanacion.com";
                    port = 80;
                }


                    if(data.server.state == uninitialized) {
                    if (port != null && host != null) {
                        data.server.channel = SocketChannel.open();
                        data.server.channel.configureBlocking(false);

                        if (data.server.channel.connect(new InetSocketAddress(host, port))) {
                            data.server.state = writing;
                            data.server.channel.register(data.key.selector(), SelectionKey.OP_WRITE, data.getPair());

                        } else {
                            data.server.state = connecting;
                            data.server.channel.register(data.key.selector(), SelectionKey.OP_CONNECT, data.getPair());

                        }
                    }
                }

                if(data.user.state == done) {
                    data.user.state = waiting;
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
                if(data.user.state == waiting) {
                    data.user.state = writing;
                    data.user.channel.register(data.key.selector(),SelectionKey.OP_WRITE,data.getPair());
                }
            }catch (IOException e){
                System.err.println(" --- Error when reading user request \n");
                e.printStackTrace();
            }
        }

        private int read(KeyData data,ChannelData from,ChannelData to) throws IOException {
            if(!data.key.isReadable()) throw new IllegalStateException("Key is not readable");

            int originalPos = data.bufferData.buff.position();

            data.content = ServerHandler.handleRead(data);

            int readBytes = data.bufferData.buff.position() - originalPos;

            if(readBytes<=0) throw new IllegalStateException("Read 0 bytes");
            if(data.content == null) throw new IllegalStateException("Content is null");

            MainError error = data.content.machine.error;
            boolean isComplete = data.content.isComplete;

            if (error != null) {
                //TODO
                return -1;
            }

            if(isComplete) {
                from.state = done;
            } else if(!data.bufferData.buff.hasRemaining()) {
                from.state = waiting;
            } else {
                from.state = reading;
                from.channel.register(data.key.selector(),SelectionKey.OP_READ,data);
            }

            if(to.state == waiting || to.state == writing) {
                to.state = writing;
                to.channel.register(data.key.selector(),SelectionKey.OP_WRITE,data.getPair());
            }

            return readBytes;
        }
    },
    writing {
        @Override
        public void userAttend(KeyData data) {
            try {
                write(data, data.server, data.user);
                if(data.server.state == done) {
                    data.user.state = done;
                } else if(data.server.state == waiting || data.server.state == reading){
                    data.server.state = reading;
                    data.server.channel.register(data.key.selector(),SelectionKey.OP_READ,data.getPair());
                } else {
                    throw new RuntimeException("WHAT?");
                }
            }catch (IOException e){
                System.err.println(" --- Error when reading user request \n");
                e.printStackTrace();
            }
        }

        @Override
        public void serverAttend(KeyData data) {
            try {
                write(data, data.user, data.server);
                if(data.user.state == done) {
                    data.server.state = reading;
                    data.server.channel.register(data.key.selector(),SelectionKey.OP_READ,data);
                } else {
                    data.server.state = waiting;
                    data.user.state = reading;
                    data.user.channel.register(data.key.selector(),SelectionKey.OP_READ,data.getPair());
                }
            }catch (IOException e){
                System.err.println(" --- Error when reading user request \n");
                e.printStackTrace();
            }
        }

        private int write(KeyData data,ChannelData from,ChannelData to) throws IOException {
            if(!data.key.isWritable()) throw new IllegalStateException("Key is not writable");

            int writtenBytes = ServerHandler.handleWrite(data);

            if(writtenBytes <= 0) throw new IllegalStateException("COUlD NOT WRITE");

            MainError error = data.content.machine.error;

            if (error != null) {
                //TODO
                return -1;
            }

            if(data.bufferData.buff.position() != 0) {
                to.state = writing;
                to.channel.register(data.key.selector(),SelectionKey.OP_WRITE,data);
            } else {
                to.state = done;
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
