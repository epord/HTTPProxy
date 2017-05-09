package protos;

/**
 * Created by Mariano on 9/5/2017.
 */
public class ProxyMain2 {

	public static void main(String[] args) {
		Server2 proxy = new Server2(10444);
		proxy.run();
	}
}
