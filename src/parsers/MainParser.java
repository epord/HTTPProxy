package parsers;

import protos.MessageType;
import protos.HTTPMessage;

import java.nio.ByteBuffer;


/**
 * Created by epord on 20/04/17.
 */
public class MainParser {

    static private boolean[] isAlpha = {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false };
    static private boolean[] isSeparator = {false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, true, false, false, false, false, false, true, true, false, false, true, false, false, true, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false };
    static private boolean[] isUri = {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, true, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, true, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false };
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

    public HTTPMessage parse(ByteBuffer buffer, MessageType type) {
        if (type == MessageType.REQUEST) {
            return parseRequest(buffer);
        } else {
            return parseResponse(buffer);
        }
    }

    private HTTPMessage parseRequest(ByteBuffer buffer) {
        StateMachine machine = new StateMachine(buffer);

        while(machine.state!=MainState.body && machine.error == null) {
            if(machine.bytes.hasRemaining()) {
                machine.state.transition(machine);
                machine.read ++;
            } else if (machine.state != MainState.body){
                machine.error = MainError.IncompleteData;
            }
        }

        if(machine.error != null) {
            System.out.println("ERROR ------------- at character" + machine.read);
            printBuffer(machine.bytes.array());
            System.out.println("END OF ERROR -------------");
        }

        HTTPMessage content = new HTTPMessage();
        content.type = MessageType.REQUEST;
        content.host = machine.headers.get("host");
        content.port = 80;
        content.body = buffer;
        content.machine = machine;
        return content;
    }


    private void printBuffer(byte [] buffer){
        StringBuffer str = new StringBuffer();
        for (byte c: buffer ) {
            str.append((char)c);
        }
        System.out.println(str.toString());
    }

    private HTTPMessage parseResponse(ByteBuffer buffer) {
        buffer.position(buffer.limit());
        return new HTTPMessage(MessageType.RESPONSE,null, null, 80, buffer);
    }

//        int i = 0;
//        DeprecatedState state = DeprecatedState.response;
//        Map<String,String> headers=new HashMap<>();
//        StringBuffer str = new StringBuffer();
//        String lastHeader="";
//        while(state!= DeprecatedState.done){
//            switch (reBytes[i]) {
//                case '\r':
//                    if(reBytes[i+1]=='\n') {
//                        if(state== DeprecatedState.newLine){
//                            state= DeprecatedState.done;
//                        } else {
//                            if(state== DeprecatedState.version) {
//                                headers.put("VERSION", str.toString());
//                                str = new StringBuffer();
//                            }
//                            state = DeprecatedState.newLine;
//                            headers.put(lastHeader,str.toString());
//                            lastHeader="";
//                        }
//                        i++;
//                    }
//                    break;
//                case ':':
//                    if(state== DeprecatedState.header){
//                        lastHeader = str.toString();
//                        str = new StringBuffer();
//                        state= DeprecatedState.text;
//                        if(reBytes[++i]!=' ') {
//                            str.append((char)reBytes[i]);
//                        }
//                    }
//                    break;
//                case ' ':
//                    if(state== DeprecatedState.request) {
//                        headers.put("METHOD",str.toString());
//                        state = DeprecatedState.URI;
//                        str = new StringBuffer();
//                    } else if(state== DeprecatedState.URI){
//                        headers.put("URL",str.toString());
//                        state = DeprecatedState.version;
//                        str = new StringBuffer();
//                    } else if(state== DeprecatedState.version){
//                        headers.put("VERSION",str.toString());
//                        state = DeprecatedState.version;
//                        str = new StringBuffer();
//                    } else if(state== DeprecatedState.text) {
//                        str.append((char)reBytes[i]);
//                    }
//                    break;
//                default:
//                    if(state== DeprecatedState.newLine) {
//                        state= DeprecatedState.header;
//                        str = new StringBuffer();
//                    }
//                    str.append((char)reBytes[i]);
//            }
//            i++;
//        }
//        return new HTTPMessage(null,null,80,buffer);

    //        private enum DeprecatedState {
//            response,
//            newLine,
//            header,
//            text,
//            version,
//            request,
//            URI,
//            done
//        }



}
