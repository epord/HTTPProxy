package protos;

import parsers.StateMachine;

import java.nio.ByteBuffer;

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
    public StateMachine machine;

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
    }
}
