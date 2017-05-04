package protos;

import parsers.MainParser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by epord on 20/04/17.
 */
public class ServerHandler {

    private int bufSize;

    public ServerHandler(int bufSize) {
        this.bufSize = bufSize;
    }


    public static RequestContent handleRead(SocketChannel keySocket, ConnectionState state, ByteBuffer buf) throws IOException {
        buf.clear();
        long bytesRead = keySocket.read(buf);
        if (bytesRead <=0 ) { // Did the other end close?
            return null;
        } else  {
           return processRequest(buf,state);
        }
    }

    public static void handleWrite(RequestContent content, SocketChannel socket, ConnectionState state, ByteBuffer buf) throws IOException {
        socket.configureBlocking(false);
        if (socket.isOpen()) {
            buf.flip();
            socket.write(buf);
        }
    }

    private static RequestContent processRequest(ByteBuffer buffer,ConnectionState state) {
        MainParser simpleParser = new MainParser();
        return simpleParser.parse(buffer,state);
    }
}
