package protos;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

/**
 * Created by Mariano on 7/5/2017.
 */
public class Server2 implements Runnable {

	private final int SELECTOR_TIMEOUT = 2000;
	private int serverPort;
	private Selector selector;
	private ServerHandler2 handler;

	public Server2(int serverPort) {
		this.serverPort = serverPort;
		this.handler = new ServerHandler2();
	}

	@Override
	public void run() {
		start();
	}

	private void start() {

		try {
			selector = Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("ERROR: Couldn't open Selector");
			return;
		}

		try {
			listen(serverPort);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("ERROR: Couldn't open ServerSocketChannel");
		}

		try {
			processKeys();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("ERROR: Exception processing keys");
		}

	}

	private void listen(int port) throws IOException {
		ServerSocketChannel listenChannel = ServerSocketChannel.open();
		listenChannel.socket().bind(new InetSocketAddress(serverPort));
		listenChannel.configureBlocking(false);
		listenChannel.register(selector, SelectionKey.OP_ACCEPT);
	}

	private void processKeys() throws IOException {
		while (true) {
			if (selector.select(SELECTOR_TIMEOUT) == 0) {
				continue;
			}

			Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();

			while (keyIter.hasNext()) {
				SelectionKey key = keyIter.next();

				if (key.isAcceptable()) {
					handler.handleAccept(key);
				}

				if (key.isReadable()) {
					handler.handleRead(key);
				}

				if (key.isValid() && key.isWritable()) {
					handler.handleWrite(key);
				}
				keyIter.remove(); //TODO: Hay que removerlas?
			}
		}
	}

}
