package protos;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * Created by epord on 20/04/17.
 */
public class TCPSocketServer {

	private static final int TIMEOUT = 2000;
	private static final int BUFSIZE = 8 * 1024; // 8KB
	private static final int NONE = 0;

	public enum State {
		LISTENINGREQUEST {
			public void attend(ConnectionData data) {

				ClientData clientData = (ClientData) data;
				try {
					if (!clientData.clientKey.isReadable()) {
						throw new IllegalStateException("WAS LISTENING A NON READABLE KEY");
					}

					if (!clientData.buffer.hasRemaining()) {
						clientData.clientKey.interestOps(NONE);
					}

					clientData.content = ServerHandler.handleRead(
							clientData.clientChannel,
							MessageType.REQUEST,
							clientData.buffer);


					if (clientData.content == null) {
						return; //This means it couldnt get the host YET.
					}


					Integer port = clientData.content.port;
					String host = clientData.content.host;

					SocketChannel hostChannel = SocketChannel.open();
					hostChannel.configureBlocking(false);
					hostChannel.connect(new InetSocketAddress(host, port));
					clientData.hostChannel = hostChannel;

					HostData hostData = new HostData(BUFSIZE); //TODO: Queda medio feo esto asi aca.

					hostData.clientKey = clientData.clientKey;
					hostData.clientChannel = clientData.clientChannel;
					hostData.hostChannel = clientData.hostChannel;
					hostData.state = CONNECTINGTOHOST;
					hostData.hostChannel.register(clientData.clientKey.selector(), SelectionKey.OP_CONNECT, clientData);
					hostData.hostKey = hostData.hostChannel.keyFor(clientData.clientKey.selector());

					clientData.hostKey = hostData.hostKey;

					//TODO: JUANFRA
//					if (messageHasEnded) {
//						clientData.state = SENDINGTOCLIENT;
//					}

				} catch (CancelledKeyException e) {
					System.out.println("Key was closed");
					e.printStackTrace();
				} catch (IOException e) {
					System.out.println("Error reading connection");
					e.printStackTrace();
				}
			}
		},
		CONNECTINGTOHOST {
			public void attend(ConnectionData data) {

				HostData hostData = (HostData) data;
				try {
					if (!hostData.hostKey.isConnectable()) {
						throw new IllegalStateException("WAS TRYING TO CONNECT A NON CONNECTABLE KEY");
					}

					if (hostData.hostChannel.finishConnect()) {
						hostData.state = SENDINGTOHOST;
						hostData.hostKey.interestOps(SelectionKey.OP_WRITE);
//						hostData.hostChannel.register(hostData.clientKey.selector(), SelectionKey.OP_WRITE, hostData); //TODO: ES NECESARIO?
					}

				} catch (CancelledKeyException e) {
					System.out.println("Key was closed");
					e.printStackTrace();
				} catch (IOException e) {
					System.out.println("Error reading connection");
					e.printStackTrace();
				}
			}
		},
		SENDINGTOHOST {
			public void attend(ConnectionData data) {

				HostData hostData = (HostData) data;
				try {
					if (!hostData.hostKey.isWritable()) {
						throw new IllegalStateException("WAS TRYING TO WRITE A NON WRITABLE KEY");
					}

					ServerHandler.handleWrite(
//							hostData.content, TODO: Cambiar, el handleWrite no necesita el content, solo el buffer.
							null,
							hostData.hostChannel,
							MessageType.REQUEST,
							((ClientData) hostData.clientKey.attachment()).buffer);

					//TODO: Debería quedarse en este estado hasta que termine de escribir.

//					if (messageEnded) {
//						hostData.state = LISTENINGHOST;
//						hostData.hostKey.interestOps(SelectionKey.OP_READ);
//					} else {
						hostData.clientKey.interestOps(SelectionKey.OP_READ);
//					}

//					hostData.hostChannel.register(hostData.hostKey.selector(), SelectionKey.OP_READ, hostData); //TODO: Esto es necesario?

				} catch (CancelledKeyException e) {
					System.out.println("Key was closed");
					e.printStackTrace();
				} catch (IOException e) {
					System.out.println("Error reading connection");
					e.printStackTrace();
				}
			}
		},
		LISTENINGHOST {
			public void attend(ConnectionData data) {

				HostData hostData = (HostData) data;
				try {
					if (!hostData.hostKey.isReadable()) {
						throw new IllegalStateException("WAS LISTENING A NON READABLE KEY");
					}

					ServerHandler.handleRead(
							hostData.hostChannel,
							MessageType.RESPONSE,
							hostData.buffer);

					//TODO: Aca debería checkear si ya termine de escuchar al host. En ese caso el cliente debe dejar el host.
//					if (hostData.content == null) {
//						hostData.userChannel.close();
//						hostData.serverChannel.close();
//						return;
//					}

					hostData.hostKey.interestOps(NONE);
					hostData.hostChannel.register(hostData.hostKey.selector(), SelectionKey.OP_WRITE, hostData);
				} catch (CancelledKeyException e) {
					System.out.println("Key was closed");
					e.printStackTrace();
				} catch (IOException e) {
					System.out.println("Error reading connection");
					e.printStackTrace();
				}
			}
		},
		SENDINGTOCLIENT {
			public void attend(ConnectionData data) {
				ClientData clientData = (ClientData) data;
				try {
					if (!clientData.clientKey.isWritable()) {
						throw new IllegalStateException("WAS TRYING TO WRITE A NON WRITABLE KEY");
					}

					ServerHandler.handleWrite(
							clientData.content,
							clientData.clientChannel,
							MessageType.RESPONSE,
							((HostData) clientData.hostKey.attachment()).buffer);


//					clientData.clientKey.cancel();
//					clientData.clientChannel.close();
//					clientData.hostChannel.close();
//					clientData.state = CLOSING;
				} catch (CancelledKeyException e) {
					System.out.println("Key was closed");
					e.printStackTrace();
				} catch (IOException e) {
					System.out.println("Error reading connection");
					e.printStackTrace();
				}
			}
		},;

		public void attend(ConnectionData data) {
		}
	}

	public void start() {

		System.out.println("STARTING");

		Selector selector;
		//region INITIALIZE
		try {
			selector = Selector.open();

			ServerSocketChannel serverSocket = ServerSocketChannel.open();
			serverSocket.bind(new InetSocketAddress(5050));
			serverSocket.configureBlocking(false);
			serverSocket.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			System.out.println("Error opening listener socket.");
			e.printStackTrace();
			return;
		}
		//endregion

		ServerHandler serverHandler = new ServerHandler(BUFSIZE);
		while (true) {
			//region SELECTING
			try {
				if (selector.select(TIMEOUT) == 0) {
					System.err.println("Timeout");
				}

				//region Logging
				System.err.println("--------------------------------------------------");
				System.err.println(selector.keys().size() + " keys in the selector");
				selector.keys().forEach(
						(k) -> {
							ConnectionData data = (ConnectionData) k.attachment();
							if (data != null) {
								System.err.println("Key id:" + data.id);
								System.err.println("Key state:" + data.state.name());
								if (((ClientData) data).content != null)
									System.err.println("Key request:" + ((ClientData) data).content.host);

							} else {
								System.err.println("Key not identified yet");
							}

							if (k.interestOps() == SelectionKey.OP_READ) {
								System.err.println("---- Read");
							}

							if (k.interestOps() == SelectionKey.OP_ACCEPT) {
								System.err.println("---- Accept");
							}

							if (k.interestOps() == SelectionKey.OP_WRITE) {
								System.err.println("---- Write");
							}

							if (k.interestOps() == SelectionKey.OP_CONNECT) {
								System.err.println("---- Connect");
							}

							System.err.println("");
						}
				);

				//endregion

			} catch (IOException e) {
				System.out.println("Error in selector.");
			}

			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			while (it.hasNext()) {
				SelectionKey key = it.next();
				it.remove();

				ConnectionData data;
				if (key.isValid()) {
					if (key.isAcceptable()) {
						data = handleAccept(key);
						continue;
					} else {
						data = (ConnectionData) key.attachment();
					}

					System.out.println(data.state);
					data.state.attend(data);

				}

			}
		}
	}

	private ClientData handleAccept(SelectionKey serverKey) {
		ClientData data = new ClientData(BUFSIZE);
		try {
			data.clientChannel = ((ServerSocketChannel) serverKey.channel()).accept();
			data.clientChannel.configureBlocking(false);
			data.state = State.LISTENINGREQUEST;
			data.clientChannel.register(serverKey.selector(), SelectionKey.OP_READ, data);
			data.clientKey = data.clientChannel.keyFor(serverKey.selector());
		} catch (CancelledKeyException e) {
			System.err.println("Key was closed");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Error accepting connection");
			e.printStackTrace();
		}

		return data;
	}
}
