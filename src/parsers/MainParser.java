package parsers;

import protos.ConnectionState;
import protos.RequestContent;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by epord on 20/04/17.
 */
public class MainParser {

//    public protos.RequestContent parse(ByteBuffer buffer) {
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
//            return new protos.RequestContent(protos.MethodType.GET, "www.lanacion.com.ar", 80, buffer.duplicate());
//        }
//        return new protos.RequestContent(protos.MethodType.OTHER, buffer.duplicate());
//    }

    static private boolean[] isAlpha = {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false };
    static private boolean[] isSeparator = {false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, true, false, false, false, false, false, true, true, false, false, true, false, false, true, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false };
    static private boolean[] isUri = {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, true, true, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, true, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, true, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false };
    static private boolean[] isToken = {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, true, true, true, true, true, false, false, true, true, false, true, true, false, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, true, false, true, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true };
    static private boolean[] isCLT = {true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false };

    static public boolean isAlpha(byte c) {
        return isAlpha[(int)c&0xFF];
    }

    static public boolean isSeparator(byte c) {
        return isSeparator[(int)c&0xFF];
    }

    static public boolean isUri(byte c) {
        return isUri[(int)c&0xFF];
    }

    static public boolean isToken(byte c) {
        return isToken[(int)c&0xFF];
    }

    static public boolean isCLT(byte c) {
        return isCLT[(int)c&0xFF];
    }

    public RequestContent parse(ByteBuffer buffer, ConnectionState state) {
        if (state == ConnectionState.REQUEST) {
            return parseRequest(buffer);
        } else {
            return parseResponse(buffer);
        }
    }

    private RequestContent parseRequest(ByteBuffer buffer) {
        StateMachine machine = new StateMachine(buffer);

        while(machine.state!=MainState.body && machine.state != MainState.errorState) {
            if(machine.bytes.hasRemaining()) {
                machine.state.transition(machine);
                machine.read ++;
            } else if (machine.state != MainState.body){
                machine.state = MainState.errorState;
                machine.error = MainError.IncompleteData;
            }
        }

        return new RequestContent(machine.content.method,machine.content.uri,80,buffer);
    }

        private enum DeprecatedState {
            response,
            newLine,
            header,
            text,
            version,
            request,
            URI,

            done
        }

    private RequestContent parseResponse(ByteBuffer buffer) {
        byte[] reBytes=buffer.array();
        int i = 0;
        DeprecatedState state = DeprecatedState.response;
        Map<String,String> headers=new HashMap<>();
        StringBuffer str = new StringBuffer();
        String lastHeader="";
        while(state!= DeprecatedState.done){
            switch (reBytes[i]) {
                case '\r':
                    if(reBytes[i+1]=='\n') {
                        if(state== DeprecatedState.newLine){
                            state= DeprecatedState.done;
                        } else {
                            if(state== DeprecatedState.version) {
                                headers.put("VERSION", str.toString());
                                str = new StringBuffer();
                            }
                            state = DeprecatedState.newLine;
                            headers.put(lastHeader,str.toString());
                            lastHeader="";
                        }
                        i++;
                    }
                    break;
                case ':':
                    if(state== DeprecatedState.header){
                        lastHeader = str.toString();
                        str = new StringBuffer();
                        state= DeprecatedState.text;
                        if(reBytes[++i]!=' ') {
                            str.append((char)reBytes[i]);
                        }
                    }
                    break;
                case ' ':
                    if(state== DeprecatedState.request) {
                        headers.put("METHOD",str.toString());
                        state = DeprecatedState.URI;
                        str = new StringBuffer();
                    } else if(state== DeprecatedState.URI){
                        headers.put("URL",str.toString());
                        state = DeprecatedState.version;
                        str = new StringBuffer();
                    } else if(state== DeprecatedState.version){
                        headers.put("VERSION",str.toString());
                        state = DeprecatedState.version;
                        str = new StringBuffer();
                    } else if(state== DeprecatedState.text) {
                        str.append((char)reBytes[i]);
                    }
                    break;
                default:
                    if(state== DeprecatedState.newLine) {
                        state= DeprecatedState.header;
                        str = new StringBuffer();
                    }
                    str.append((char)reBytes[i]);
            }
            i++;
        }
        return new RequestContent(null,null,80,buffer);
    }



}
