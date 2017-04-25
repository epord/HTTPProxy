import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by epord on 20/04/17.
 */
public class SimpleParser {

//    public RequestContent parse(ByteBuffer buffer) {
//        StringBuilder builder = new StringBuilder();
//        buffer.flip();
//        while (buffer.hasRemaining()){
//            builder.append((char) buffer.get());
//        }
//        String request = builder.toString().trim();
////        System.out.println(request);
//
//        String[] auxRequest = request.split(" ");
//        String method = auxRequest[0];
//        if (method.toUpperCase().equals("GET")) {
////            String host = auxRequest[1].split(":")[0];
////            Integer port = Integer.parseInt(auxRequest[1].split(":")[1]);
//            return new RequestContent(MethodType.GET, "www.lanacion.com.ar", 80, buffer.duplicate());
//        }
//        return new RequestContent(MethodType.OTHER, buffer.duplicate());
//    }


        private enum ParserState {
            request,
            url,
            version,
            response,
            header,
            newLine,
            text,
            done;
        }

        public RequestContent parse(ByteBuffer buffer, ConnectionState state) {
            if(state== ConnectionState.REQUEST) {
                return parseRequest(buffer);
            } else {
                return parseResponse(buffer);
            }
        }
//
    private RequestContent parseRequest(ByteBuffer buffer) {
            byte[] reBytes=buffer.array();
            int i = 0;
            ParserState state = ParserState.request;
            Map<String,String> headers=new HashMap<>();
            StringBuffer str = new StringBuffer();
            String lastHeader="";
            while(state!= ParserState.done){
                switch (reBytes[i]) {
                    case '\r':
                        if(reBytes[i+1]=='\n') {
                            if(state== ParserState.newLine){
                                state= ParserState.done;
                            } else {
                                if(state== ParserState.version) {
                                    headers.put("VERSION", str.toString());
                                    str = new StringBuffer();
                                }
                                state = ParserState.newLine;
                                headers.put(lastHeader,str.toString());
                                lastHeader="";
                            }
                            i++;
                        }
                        break;
                    case ':':
                        if(state== ParserState.header){
                            lastHeader = str.toString();
                            str = new StringBuffer();
                            state= ParserState.text;
                            if(reBytes[++i]!=' ') {
                                str.append((char)reBytes[i]);
                            }
                        }
                        break;
                    case ' ':
                        if(state== ParserState.request) {
                            headers.put("METHOD",str.toString());
                            state = ParserState.url;
                            str = new StringBuffer();
                        } else if(state== ParserState.url){
                            headers.put("URL",str.toString());
                            state = ParserState.version;
                            str = new StringBuffer();
                        } else if(state== ParserState.version){
                            headers.put("VERSION",str.toString());
                            state = ParserState.version;
                            str = new StringBuffer();
                        } else if(state== ParserState.text) {
                            str.append((char)reBytes[i]);
                        }
                        break;
                    default:
                        if(state== ParserState.newLine) {
                            state= ParserState.header;
                            str = new StringBuffer();
                        }
                        str.append((char)reBytes[i]);
                }
                i++;
            }

            return new RequestContent(MethodType.fromString(headers.get("METHOD")),headers.get("Host"),80,buffer);
        }

    private RequestContent parseResponse(ByteBuffer buffer) {
        byte[] reBytes=buffer.array();
        int i = 0;
        ParserState state = ParserState.response;
        Map<String,String> headers=new HashMap<>();
        StringBuffer str = new StringBuffer();
        String lastHeader="";
        while(state!= ParserState.done){
            switch (reBytes[i]) {
                case '\r':
                    if(reBytes[i+1]=='\n') {
                        if(state== ParserState.newLine){
                            state= ParserState.done;
                        } else {
                            if(state== ParserState.version) {
                                headers.put("VERSION", str.toString());
                                str = new StringBuffer();
                            }
                            state = ParserState.newLine;
                            headers.put(lastHeader,str.toString());
                            lastHeader="";
                        }
                        i++;
                    }
                    break;
                case ':':
                    if(state== ParserState.header){
                        lastHeader = str.toString();
                        str = new StringBuffer();
                        state= ParserState.text;
                        if(reBytes[++i]!=' ') {
                            str.append((char)reBytes[i]);
                        }
                    }
                    break;
                case ' ':
                    if(state== ParserState.request) {
                        headers.put("METHOD",str.toString());
                        state = ParserState.url;
                        str = new StringBuffer();
                    } else if(state== ParserState.url){
                        headers.put("URL",str.toString());
                        state = ParserState.version;
                        str = new StringBuffer();
                    } else if(state== ParserState.version){
                        headers.put("VERSION",str.toString());
                        state = ParserState.version;
                        str = new StringBuffer();
                    } else if(state== ParserState.text) {
                        str.append((char)reBytes[i]);
                    }
                    break;
                default:
                    if(state== ParserState.newLine) {
                        state= ParserState.header;
                        str = new StringBuffer();
                    }
                    str.append((char)reBytes[i]);
            }
            i++;
        }

        return new RequestContent(MethodType.fromString(headers.get("METHOD")),headers.get("Host"),80,buffer);
    }



}
