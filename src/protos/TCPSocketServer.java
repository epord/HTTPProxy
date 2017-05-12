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
			public void attend(ClientData clientData) {
				try {
					if (!clientData.clientKey.isReadable()) {
						throw new IllegalStateException("WAS LISTENING A NON READABLE KEY");
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


					clientData.state = SENDINGTOCLIENT;

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
			public void attend(HostData hostData) {
				try {
					if (!hostData.hostKey.isConnectable()) {
						throw new IllegalStateException("WAS TRYING TO CONNECT A NON CONNECTABLE KEY");
					}

					if (hostData.hostChannel.finishConnect()) {
						hostData.state = SENDINGTOHOST;
						hostData.hostKey.interestOps(SelectionKey.OP_WRITE);
						hostData.hostChannel.register(hostData.clientKey.selector(), SelectionKey.OP_WRITE, hostData);
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
			public void attend(HostData hostData) {
				try {
					if (!hostData.hostKey.isWritable()) {
						throw new IllegalStateException("WAS TRYING TO WRITE A NON WRITABLE KEY");
					}

					ServerHandler.handleWrite(
//							hostData.content, TODO: Cambiar, el handleWrite no necesita el content, solo el buffer.
							null,
							hostData.hostChannel,
							MessageType.REQUEST,
							hostData.buffer);

					//TODO: Debería quedarse en este estado hasta que termine de escribir.

					hostData.state = LISTENINGHOST;
					hostData.hostKey.interestOps(SelectionKey.OP_READ);
					hostData.hostChannel.register(hostData.hostKey.selector(), SelectionKey.OP_READ, hostData); //TODO: Esto es necesario?
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
			public void attend(HostData hostData) {
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
					hostData.userChannel.register(hostData.key.selector(), SelectionKey.OP_WRITE, hostData);
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
			public void attend(ClientData clientData) {

			}
		},
		SENDINGRESPONSE {
			public void attend(KeyData data) {
				try {
					if (!data.key.isWritable()) {
						throw new IllegalStateException("WAS TRYING TO WRITE A NON WRITABLE KEY");
					}

					ServerHandler.handleWrite(
							data.content,
							data.userChannel,
							MessageType.RESPONSE,
							data.buffer);

//                    System.out.println("SENDING RESPONSE:\n" + new String(data.buffer.array()));

					data.key.cancel();
					data.userChannel.close();
					data.serverChannel.close();
					data.state = CLOSING;
				} catch (CancelledKeyException e) {
					System.out.println("Key was closed");
					e.printStackTrace();
				} catch (IOException e) {
					System.out.println("Error reading connection");
					e.printStackTrace();
				}
			}
		},
		CLOSING;

		public void attend(ConnectionData data) {
		}

		MessageType connectionState() {
			switch (this) {
				case LISTENINGREQUEST:
				case SENDINGTOHOST:
				case CONNECTING:
					return MessageType.REQUEST;
				case SENDINGTOCLIENT:
				case SENDINGRESPONSE:
					return MessageType.RESPONSE;
				default:
					return null;
			}
		}
	}

	public void start() {

		Selector selector = null;
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
								System.err.println("Key id:" + data.Id);
								System.err.println("Key state:" + data.state.name());
								if (((ClientData) data.content) != null)
									System.err.println("Key request:" + data.content.host);

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
