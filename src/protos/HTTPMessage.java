package protos;

import parsers.StateMachine;

import java.nio.ByteBuffer;

/**
 * Created by epord on 20/04/17.
 */
public class HTTPMessage {
	public MessageType type;
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

    public HTTPMessage(){}

    public HTTPMessage(MessageType type, MethodType method, String host, Integer port, ByteBuffer body) {
    	this.type = type;
        this.method = method;
        this.host = host;
        this.port = port;
        this.body = body;
    }
}
