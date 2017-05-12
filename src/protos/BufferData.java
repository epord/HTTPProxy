package protos;

import java.nio.ByteBuffer;

/**
 * Created by juanfra on 11/05/17.
 */
public class BufferData {

    ByteBuffer buff;
    BufferState state;


    public BufferData(int buffsize) {
        buff = ByteBuffer.allocate(buffsize);
        buff.mark();
        state = BufferState.reading;
    }
}
