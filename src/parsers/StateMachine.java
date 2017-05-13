package parsers;

import java.nio.ByteBuffer;

/**
 * Created by juanfra on 03/05/17.
 */
public class StateMachine {

    public MainError error;
    public MainState state;

    public int read;
    public ByteBuffer bytes;
    public int parsedIndex;

    //EveryState must know what is in here
    public Object stateData;

    public StateMachine(ByteBuffer buffer) {
        state = MainState.firstLine;
        bytes = buffer;
        read = 0;
    }
}
