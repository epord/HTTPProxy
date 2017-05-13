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

    private static MainParser mainParser = new MainParser();

    public ServerHandler(int bufSize) {
        this.bufSize = bufSize;
    }


    public static int handleRead(KeyData data) throws IOException {
        SocketChannel socket = data.isUser ? data.user.channel: data.server.channel;
        socket.configureBlocking(false);
        ByteBuffer buff = data.buffer;

        int bytesRead = socket.read(buff);
        if (bytesRead <=0 ) { // Did the other end close?
            return bytesRead;
        } else  {
            // start parsing from parsedIndex
            buff.flip();
            processRequest(data);

            // Keep position at the end and ready to keep writing
            buff.limit(buff.capacity());

            //TODO better way than using an error
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
        ByteBuffer buff = data.buffer;
        
        if (socket.isOpen()) {
            buff.flip();
            int writtenBytes = socket.write(buff);

            if(buff.hasRemaining()) {
                // Delete already written Bytes, sets the buffer ready to write in it
                buff.compact();

                //Set mark for parsed data
                data.content.machine.parsedIndex = buff.position();
                buff.limit(buff.capacity());

            } else {

                //buffer is now empty
                buff.clear();
                data.content.machine.parsedIndex = 0;
            }

            return writtenBytes;
        } else {
            return -1;
        }
    }

    private static void processRequest(KeyData data) {
        if (data.isUser)
                mainParser.parseRequest(data.buffer,data.content);
        else
                mainParser.parseResponse(data.buffer,data.content);
    }
}
