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
    public String host;
    public Integer port;
    public ByteBuffer body;
    public String uri;
    public HTTPVersion version;
    public Map<String,String> headers;
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

    public RequestContent(){}

    public RequestContent(MethodType method, String host, Integer port, ByteBuffer body) {
        this.method = method;
        this.host = host;
        this.port = port;
        this.body = body;
        this.headers = new HashMap<>();
    }
}
