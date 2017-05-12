package protos;

/**
 * Created by Mariano on 11/5/2017.
 */
public class ClientData extends ConnectionData {
	HTTPMessage content;

	public ClientData(int bufferSize) {
		super(bufferSize);
		connectionType = ConnectionType.CLIENT;
	}
}