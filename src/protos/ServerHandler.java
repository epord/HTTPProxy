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


    public static HTTPMessage handleRead(SocketChannel keySocket, MessageType type, ByteBuffer buf) throws IOException {
        int bytesRead = keySocket.read(buf);
        if (bytesRead <=0 ) { // Did the other end close?
            return null;
        } else  {
            buf.position(buf.position() - bytesRead);
           return processRequest(buf,type);
        }
    }

    public static void handleWrite(HTTPMessage content, SocketChannel socket, MessageType type, ByteBuffer buf) throws IOException {
        socket.configureBlocking(false);
        if (socket.isOpen()) {
            buf.flip();
            socket.write(buf);
            //TODO check if eveything has been written
            buf.clear();
        }
    }

    private static HTTPMessage processRequest(ByteBuffer buffer, MessageType type) {
        MainParser simpleParser = new MainParser();
        return simpleParser.parse(buffer,type);
    }
}
