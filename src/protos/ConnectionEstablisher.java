package protos;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by Mariano on 9/5/2017.
 */
public class ConnectionEstablisher implements Runnable {

	private HostConnection hostConnection;
	private ConnectionHandler connectionHandler;

	public ConnectionEstablisher(HostConnection hostConnection, ConnectionHandler connectionHandler) { //TODO: No me gusta tener que psar el ConnectionHandler
		this.hostConnection = hostConnection;
		this.connectionHandler = connectionHandler;
	}

	@Override
	public void run() {
		try {
			SocketChannel hostChannel = hostConnection.connect();
			connectionHandler.setConnectionType(hostChannel, ConnectionType.HOST); //TODO: No me gusta que esto esté aca.
			hostConnection.getClientKey().interestOps(SelectionKey.OP_READ);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Unable to connect to host.");
		}
	}
}
