package protos;

/**
 * Created by Mariano on 11/5/2017.
 */
public class HostData extends ConnectionData {

	public HostData(int bufferSize) {
		super(bufferSize);
		connectionType = ConnectionType.HOST;
	}
}