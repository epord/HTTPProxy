package protos;

import java.nio.ByteBuffer;

/**
 * Created by Mariano on 8/5/2017.
 */
public class HTTPMessage2 {

	private MessageType type;
	private MethodType method;
	private String host;
	private Integer port;
	private ByteBuffer buffer;
	private String uri;
	private int contentLength;
	private int bytes;
	private int version;

	private HTTPMessage2(MessageType type, MethodType method, String host, Integer port, ByteBuffer buffer, int contentLength, int version) {
		this.type = type;
		this.method = method;
		this.host = host;
		this.port = port;
		this.buffer = buffer;
		this.uri = null;
		this.contentLength = contentLength;
		this.bytes = buffer.position() + 1 + contentLength; //TODO: Verificar. Bytes que ocupa los haders + linea en blanco + content length;
		this.version = version;
	}

	public static HTTPMessage2 Request(MethodType method, String host, Integer port, ByteBuffer buffer, int contentLength, int version) {
		return new HTTPMessage2(MessageType.REQUEST, method, host, port, buffer, contentLength, version);
	}

	public static HTTPMessage2 Response(ByteBuffer buffer, int contentLength, int version) {
		return new HTTPMessage2(MessageType.RESPONSE, null, null, null, buffer, contentLength, version);
	}

	public MessageType getType() {
		return type;
	}

	public MethodType getMethod() {
		if (this.type == MessageType.RESPONSE) {
			throw new IllegalStateException("Responses don't have Method");
		}
		return method;
	}

	public String getHost() {
		if (this.type == MessageType.RESPONSE) {
			throw new IllegalStateException("Responses don't have Host");
		}
		return host;
	}

	public Integer getPort() {
		if (this.type == MessageType.RESPONSE) {
			throw new IllegalStateException("Responses don't have Port");
		}
		return port;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}

	public String getUri() {
		return uri;
	}

	public int getVersion() {
		return version;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
}
