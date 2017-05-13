package protos;

import parsers.StateMachine;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by epord on 20/04/17.
 */
public class RequestContent {

    public MethodType method;
    public String uri;
    public HTTPVersion version;

    public Map<String,String> headers;

    public ByteBuffer body;

    public StateMachine machine;
    public boolean isComplete;

    public enum HTTPVersion {
        zero,
        one;

        public static HTTPVersion version(int v){
            if(v==1) return one;
            if(v==0) return zero;
            return null;
        }
    }

    public RequestContent(){
        this.headers = new HashMap<>();
    }

    public String getHost() {
        return headers.get("host");
    }

    public Integer getPort() {
        return 80;
    }

    public String printBuffer(){
        StringBuffer str = new StringBuffer();
        for (byte c: machine.bytes.array() ) {
            str.append((char)c);
        }
        return str.toString();
    }
}
