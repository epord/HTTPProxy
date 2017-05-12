package protos;

import java.nio.channels.SelectionKey;
import java.util.Random;

/**
 * Created by juanfra on 11/05/17.
 */
public class KeyData {

    RequestContent content;

    ChannelData user;
    ChannelData server;

    BufferData bufferData;

    SelectionKey key;
    int Id;
    boolean isUser;

    private KeyData pair;

    public static KeyData userKeyData(int bufferSize){
        KeyData userKeyData = new KeyData();
        userKeyData.user = new ChannelData(null,ChannelState.uninitialized);
        userKeyData.server = new ChannelData(null,ChannelState.uninitialized);

        userKeyData.bufferData = new BufferData(bufferSize);

        userKeyData.Id = Math.abs(new Random().nextInt());
        userKeyData.isUser = true;
        userKeyData.pair = generatePair(userKeyData);

        return userKeyData;
    }

    public KeyData getPair(){
        return pair;
    }

    private static KeyData generatePair(KeyData k1) {

        KeyData serverKeyData = new KeyData();
        serverKeyData.content = k1.content;
        serverKeyData.server = k1.server;
        serverKeyData.user = k1.user;
        serverKeyData.bufferData = k1.bufferData;

        serverKeyData.isUser = !k1.isUser;
        serverKeyData.Id = -k1.Id;

        return serverKeyData;
    }
}
