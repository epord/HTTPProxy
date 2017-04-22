import java.nio.ByteBuffer;

/**
 * Created by epord on 20/04/17.
 */
public class SimpleParser {

    public RequestContent parse(ByteBuffer buffer) {
        StringBuilder builder = new StringBuilder();
        buffer.flip();
        while (buffer.hasRemaining()){
            builder.append((char) buffer.get());
        }
        String request = builder.toString().trim();
//        System.out.println(request);

        String[] auxRequest = request.split(" ");
        String method = auxRequest[0];
        if (method.toUpperCase().equals("GET")) {
//            String host = auxRequest[1].split(":")[0];
//            Integer port = Integer.parseInt(auxRequest[1].split(":")[1]);
            return new RequestContent(MethodType.GET, "www.lanacion.com.ar", 80, buffer.duplicate());
        }
        return new RequestContent(MethodType.OTHER, buffer.duplicate());
    }
}
