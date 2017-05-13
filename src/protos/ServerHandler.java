package protos;

import parsers.MainError;
import parsers.MainParser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.Key;

/**
 * Created by epord on 20/04/17.
 */
public class ServerHandler {

    private int bufSize;

    private static MainParser mainParser = new MainParser();

    public ServerHandler(int bufSize) {
        this.bufSize = bufSize;
    }


    public static int handleRead(KeyData data) throws IOException {
        SocketChannel socket = data.isUser ? data.user.channel: data.server.channel;
        socket.configureBlocking(false);
        ByteBuffer buff = data.bufferData.buff;

        int bytesRead = socket.read(buff);
        if (bytesRead <=0 ) { // Did the other end close?
            return bytesRead;
        } else  {
            // start parsing from the mark
            buff.reset();
            processRequest(data);
            // set mark where the position is now
            buff.mark();

            //TODO

            if(data.content.machine.error == MainError.IncompleteData) {
                data.content.machine.error = null;
                data.content.isComplete = false;
            } else {
                data.content.isComplete = true;
            }
            return bytesRead;
        }
    }

    public static int handleWrite(KeyData data) throws IOException {

        SocketChannel socket = data.isUser ? data.user.channel: data.server.channel;
        socket.configureBlocking(false);
        ByteBuffer buff = data.bufferData.buff;
        
        if (socket.isOpen()) {
            buff.flip();
            int writtenBytes = socket.write(buff);

            if(buff.hasRemaining()) {
                // Delete already written Bytes, sets the buffer ready to write in it
                buff.compact();

                //Set mark for parsed data
                int pos = buff.position();
                buff.position(buff.limit());
                buff.mark();
                buff.position(pos);

            } else {

                //buffer is now empty
                buff.clear();
                buff.mark();
            }

            return writtenBytes;
        } else {
            return -1;
        }
    }

    private static void processRequest(KeyData data) {
        if (data.isUser)
                mainParser.parseRequest(data.bufferData.buff,data.content);
        else
                mainParser.parseResponse(data.bufferData.buff,data.content);
    }
}
