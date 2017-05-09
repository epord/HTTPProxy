package protos;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by Mariano on 9/5/2017.
 */
public class HostConnection {

	private String host;
	private int port;
	private SelectionKey key;
	private SelectionKey clientKey;
	private HTTPMessage2 current;

	public HostConnection(String host, Integer port, SelectionKey clientKey) {
		this.host = host;
		this.port = port;
		this.clientKey = clientKey;
	}

	public String getHost() {
		return this.host;
	}

	public int getPort() {
		return this.port;
	}

	public SelectionKey getKey() {
		return key;
	}

	public SelectionKey getClientKey() {
		return clientKey;
	}

	public SocketChannel connect() throws IOException {
		SocketChannel hostChannel = SocketChannel.open();
		hostChannel.configureBlocking(false);

		hostChannel.connect(new InetSocketAddress(this.host, this.port));
		hostChannel.finishConnect(); //TODO: This should block?

		hostChannel.register(clientKey.selector(), SelectionKey.OP_WRITE, ByteBuffer.allocate(ServerHandler2.BUFSIZE)); //TODO: No me gusta que el buffsize este ahí.

		return hostChannel;
	}
}
