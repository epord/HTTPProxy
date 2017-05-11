package protos;

import parsers.MainError;
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


    public static RequestContent handleRead(SocketChannel socket, ConnectionState state, ByteBuffer buf) throws IOException {
        socket.configureBlocking(false);

        int bytesRead = socket.read(buf);
        if (bytesRead <=0 ) { // Did the other end close?
            return null;
        } else  {
            buf.position(buf.position() - bytesRead);
           RequestContent ans = processRequest(buf,state);
            if(ans.machine.error == MainError.IncompleteData) {
                ans.machine.error = null;
                ans.isComplete = false;
            } else {
                ans.isComplete = true;
            }
            return ans;
        }
    }

    public static int handleWrite(RequestContent content, SocketChannel socket, ConnectionState state, ByteBuffer buf) throws IOException {
        socket.configureBlocking(false);
        int bytesWritten = buf.remaining();
        if (socket.isOpen()) {
            socket.write(buf);
            //TODO check if everything has been written
            if(buf.remaining() == 0) {
                buf.clear();
            }

            return bytesWritten - buf.remaining();
        } else {
            return -1;
        }
    }

    private static RequestContent processRequest(ByteBuffer buffer,ConnectionState state) {
        MainParser simpleParser = new MainParser();
        return simpleParser.parse(buffer,state);
    }
}
