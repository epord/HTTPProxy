import java.nio.ByteBuffer;

/**
 * Created by epord on 20/04/17.
 */
public class RequestContent {
    private MethodType type;
    private String host;
    private Integer port;
    private ByteBuffer body;

    public RequestContent(MethodType type, String host, int port, ByteBuffer body) {
        this.type = type;
        this.host = host;
        this.port = port;
        this.body = body;
    }

    public RequestContent(MethodType type, ByteBuffer body) {
        this.type = type;
        this.body = body;
    }

    public MethodType getType() {
        return type;
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
