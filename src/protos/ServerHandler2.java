package protos;

import parsers.HTTPParser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by Mariano on 8/5/2017.
 */
public class ServerHandler2 {

	public static final int BUFSIZE = 8 * 1024; // 8KB
	private ConnectionHandler connectionHandler;

	public ServerHandler2() {
		connectionHandler = new ConnectionHandler();
	}

	public void handleAccept(SelectionKey key) throws IOException {
		SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();
		clientChannel.configureBlocking(false);
		clientChannel.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(BUFSIZE));
		connectionHandler.setConnectionType(clientChannel, ConnectionType.CLIENT);
	}

	public void handleRead(SelectionKey key) throws IOException {
		// Client socket channel has pending data
		SocketChannel socketChannel = (SocketChannel) key.channel();
		if (connectionHandler.getConnectionType(socketChannel) == ConnectionType.CLIENT) {
			if (connectionHandler.hasCurrentMessage(socketChannel)) { //Se supone que si tiene un currentMessage en el hashmap debería estar ya conectado.
				HTTPMessage2 message = connectionHandler.getMessage(socketChannel);
				HostConnection hostConnection = connectionHandler.getHostFromClient(socketChannel);
				SelectionKey hostKey = hostConnection.getKey();
				ByteBuffer hostBuffer = (ByteBuffer) hostKey.attachment();
				if (message.getBuffer().hasRemaining()) { //Aca copia lo que habia leido anteriormente el parser al crear el mensaje HTTP con sus headers. Solo falta el content.
					ByteBuffer messageBuffer = message.getBuffer();
					int size = Math.min(hostBuffer.remaining(), messageBuffer.remaining());
					byte[] aux = new byte[size];
					message.getBuffer().get(aux);
					hostBuffer.put(aux);
					if (messageBuffer.hasRemaining()) {
						key.interestOps(0);
						return;
					}
				}
				if (hostBuffer.hasRemaining()) { //Aca copia el content, si es que hay.
					long bytesRead = socketChannel.read(hostBuffer);
					if (bytesRead == -1) { //TODO: El cliente se desconecto?
						socketChannel.close();
						return;
					} else if (bytesRead == 0) {
						//TODO: Borrar mensaje y otras cosas.
					}
				}//TODO: OJO!!!!!!!!!!!!!!!! Que pasa si ya leyo todo el socketChannel y salió, debería haber borrado el mensaje. Tiene que fijarse si el socketChannel tiene mas para leer.
				hostKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
				//TODO: Hacer que solo agregue la OP_READ cuando queda en bytesRead == 0?
				key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
			} else {
				connectionHandler.connect(key);
				return; //TODO: Hacer que cree la conexion y el mensaje. O que reuse la conexion.
			}
		} else {
			SelectionKey clientKey = connectionHandler.getHost(socketChannel).getClientKey();
			ByteBuffer clientBuffer = (ByteBuffer) clientKey.attachment();

			if (clientBuffer.hasRemaining()) {
				long bytesRead = socketChannel.read(clientBuffer);
				if (bytesRead == -1) { //TODO: reConnect with host?
					socketChannel.close();
				} else if (bytesRead == 0) {
					//TODO: Remove client from this connection, to allow new clients join it? QUE PASA SI FALTABA LEER ALGO PARA ESTE CLIENT? Cuando se que el host deja de mandar cosas al client?
				} else {
					clientKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
				}
			}
		}
	}

	public void handleWrite(SelectionKey key) throws IOException {
	}
}
