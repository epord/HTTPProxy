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

    public enum HTTPVersion {
        zero,
        one;

        static HTTPVersion version(int v){
            if(v==1) return one;
            if(v==0) return zero;
            return null;
        }
    }

    public RequestContent(MethodType type, String host, int port, ByteBuffer body) {
        this.method = type;
        this.host = host;
        this.port = port;
        this.body = body;
    }

    public  RequestContent(){}

    public RequestContent(MethodType type, ByteBuffer body) {
        this.method = type;
        this.body = body;
    }

    public MethodType getMethod() {
        return method;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public ByteBuffer getBody() {
        return body;
    }
}
