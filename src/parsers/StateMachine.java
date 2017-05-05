package parsers;

import protos.RequestContent;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by juanfra on 03/05/17.
 */
public class StateMachine {
    RequestContent content;

    MainError error;
    MainState state;

    int read;
    ByteBuffer bytes;
    Map<String,String> headers;

    //EveryState must know what is in here
    Object stateData;

    public StateMachine(ByteBuffer buffer) {
        state = MainState.firstLine;
        bytes = buffer;
        content = new RequestContent();
        content.machine = this;
        headers = new HashMap<>();
        read = 0;
    }
}
