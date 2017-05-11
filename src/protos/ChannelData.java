package protos;

import java.nio.channels.SocketChannel;

/**
 * Created by juanfra on 11/05/17.
 */
public class ChannelData {
    SocketChannel channel;
    ChannelState state;

    public ChannelData(SocketChannel channel, ChannelState state) {
        this.channel = channel;
        this.state = state;
    }
}
