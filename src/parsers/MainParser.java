package parsers;

import protos.ConnectionState;
import protos.RequestContent;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


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

    public void parseRequest(ByteBuffer buffer, RequestContent content) {
        if (content.machine == null) {
            content.machine = new StateMachine(buffer);
        }
        StateMachine machine = content.machine;

        while (machine.state != MainState.body && machine.error == null) {
            if (machine.bytes.hasRemaining()) {
                machine.state.transition(content);
                machine.read++;
            } else if (machine.state != MainState.body) {
                machine.error = MainError.IncompleteData;
            }
        }

        if (machine.error != null) {
            System.out.println("\nERROR ------------- at character" + machine.read + "ErrorType: " + machine.error);
            printBuffer(machine.bytes.array());
            System.out.println("END OF ERROR -------------\n");
        } else {
            content.isComplete = true;
        }

    }

    private void printBuffer(byte [] buffer){
        StringBuffer str = new StringBuffer();
        for (byte c: buffer ) {
            str.append((char)c);
        }
        System.out.println(str.toString());
    }

    public void parseResponse(ByteBuffer buffer, RequestContent content) {
        buffer.position(buffer.limit());
        if(content.machine==null) {
            content.machine = new StateMachine(buffer);
        }
        content.machine.error = MainError.IncompleteData;
        content.isComplete = false;
        printBuffer(content.machine.bytes.array());
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
//        return new RequestContent(null,null,80,buffer);

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
