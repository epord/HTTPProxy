package protos;

import parsers.HTTPParser;
import parsers.SimpleParser;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Created by Mariano on 9/5/2017.
 */
public class ConnectionHandler {

	private Map<SocketChannel, HostConnection> hosts;
	private Map<SocketChannel, HostConnection> clientHosts;
	private Map<SocketChannel, HTTPMessage2> currentMessages;
	private Map<SocketChannel, ConnectionType> connectionTypes;
	private Map<String, Queue<HostConnection>> unused;
	private HTTPParser parser;

	public ConnectionHandler() {
		this.hosts = new HashMap<>();
		this.clientHosts = new HashMap<>();
		this.currentMessages = new HashMap<>();
		this.connectionTypes = new HashMap<>();
		this.unused = new HashMap<>();
		this.parser = new SimpleParser();
	}

	public boolean hasCurrentMessage(SocketChannel socketChannel) {
		return currentMessages.containsKey(socketChannel);
	}

	public HTTPMessage2 getMessage(SocketChannel socketChannel) {
		return currentMessages.get(socketChannel);
	}

	public ConnectionType getConnectionType(SocketChannel socketChannel) {
		return connectionTypes.get(socketChannel);
	}

	public HostConnection getHost(SocketChannel hostChannel) {
		return hosts.get(hostChannel);
	}

	public HostConnection getHostFromClient(SocketChannel clientChannel) {
		return clientHosts.get(clientChannel);
	}

	public void connect(SelectionKey clientKey) {
		HTTPMessage2 message = parser.parse(clientKey);
		SocketChannel clientChannel = (SocketChannel) clientKey.channel();
		HostConnection hostConnection = getUnusedConnection(message.getHost(), message.getPort());
		if (hostConnection == null) {
			hostConnection = new HostConnection(message.getHost(), message.getPort(), clientKey);
			Thread establisher = new Thread(new ConnectionEstablisher(hostConnection, this));
			establisher.run();
		}

		currentMessages.put(clientChannel, message);
	}

	private HostConnection getUnusedConnection(String host, Integer port) {
		Queue<HostConnection> queue = unused.get(host);
		if (queue.isEmpty()) {
			return null;
		}
		return queue.poll();
	}

	public void setConnectionType(SocketChannel socketChannel, ConnectionType connectionType) {
		connectionTypes.put(socketChannel, connectionType);
	}
}
