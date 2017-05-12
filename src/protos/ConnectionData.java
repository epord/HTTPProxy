package protos;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Random;

/**
 * Created by Mariano on 11/5/2017.
 */

public abstract class ConnectionData {
	ConnectionType connectionType;
	TCPSocketServer.State state;
	ByteBuffer buffer;
	SocketChannel clientChannel;
	SocketChannel hostChannel;
	SelectionKey clientKey;
	SelectionKey hostKey;
	int id;

	public ConnectionData(int bufferSize) {
		this.buffer = ByteBuffer.allocate(bufferSize);
		this.id = new Random().nextInt();
	}
}