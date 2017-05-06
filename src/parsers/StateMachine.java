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
    public MethodType method;
    public String uri;
    public RequestContent.HTTPVersion HTTPversion;

    public MainError error;
    public MainState state;

    public int read;
    public ByteBuffer bytes;
    public Map<String,String> headers;

    //EveryState must know what is in here
    public Object stateData;

    public StateMachine(ByteBuffer buffer) {
        state = MainState.firstLine;
        bytes = buffer;
        headers = new HashMap<>();
        read = 0;
    }
}
