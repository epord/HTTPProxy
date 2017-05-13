package protos;

import java.io.IOException;

/**
 * Created by epord on 21/04/17.
 */
public class ProxyMain {

    public static void main(String[] args) throws IOException{
        TCPSocketServer server = new TCPSocketServer();
        server.start();
    }
}
