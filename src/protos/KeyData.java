package protos;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Random;

/**
 * Created by juanfra on 11/05/17.
 */
public class KeyData {

    RequestContent content;

    ChannelData user;
    ChannelData server;

    ByteBuffer buffer;

    SelectionKey key;
    int Id;
    boolean isUser;

    public static KeyData userKeyData(int bufferSize){
        KeyData userKeyData = new KeyData();
        userKeyData.user = new ChannelData(null,ChannelState.uninitialized);
        userKeyData.server = new ChannelData(null,ChannelState.uninitialized);

        userKeyData.buffer = ByteBuffer.allocate(bufferSize);

        userKeyData.Id = Math.abs(new Random().nextInt());
        userKeyData.isUser = true;

        return userKeyData;
    }

    public KeyData getPair() {

        KeyData serverKeyData = new KeyData();
        serverKeyData.server = server;
        serverKeyData.user = user;
        serverKeyData.buffer = buffer;

        serverKeyData.isUser = !isUser;
        serverKeyData.Id = -Id;

        return serverKeyData;
    }
}
