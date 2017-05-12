package parsers;

import protos.MethodType;
import protos.RequestContent;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by juanfra on 03/05/17.
 */
public class StateMachine {

    public MainError error;
    public MainState state;

    public int read;
    public ByteBuffer bytes;

    //EveryState must know what is in here
    public Object stateData;

    public StateMachine(ByteBuffer buffer) {
        state = MainState.firstLine;
        bytes = buffer;
        read = 0;
    }
}
