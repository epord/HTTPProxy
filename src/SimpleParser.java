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

        private enum ParserState {
            OK,
            ValidatingMethod {
                int index = 0;
                public ParserState transition(StateMachine machine) {
                    byte c = machine.bytes.get();
                    if(index == 0) {
                        switch (c) {
                            case 'G':
                            case 'g':
                                machine.content.method = MethodType.GET;
                                break;
                            case 'P':
                            case 'p':
                                machine.content.method = MethodType.POST;
                                break;
                            case 'H':
                            case 'h':
                                machine.content.method = MethodType.HEAD;
                                break;
                            default:
                                return setError(machine,ParserError.UnsupportedMethod);
                        }
                        index++;
                        return setState(machine, ValidatingMethod);
                    } else {
                        if(c == ' ') {
                            if (machine.content.method.isFinished(index)) {
                                return setState(machine,URI);
                            } else {
                                return setError(machine,ParserError.UnsupportedMethod);
                            }
                        }
                        if(machine.content.method.isValid(index,c)) {
                            index++;
                            return setState(machine, ValidatingMethod);
                        } else {
                            return setError(machine,ParserError.UnsupportedMethod);
                        }

                    }
                }
            },
            request,
            URI {
                public ParserState transition(StateMachine machine) {
                    byte c = machine.bytes.get();
                    if ( c == ' ') {
                        machine.content.uri = machine.str.toString();
                        machine.str = new StringBuilder();
                        return setState(machine,version);
                    } else if(isUri(c)){
                        machine.str.append(Character.toLowerCase((char) c));
                        return setState(machine,URI);
                    } else {
                        return setError(machine,(ParserError.UnknownExpression));
                    }
                }
            },
            version {
                byte[] http={'H','T','T','P','/','1','.'};

                public ParserState transition(StateMachine machine) {
                    for (int i = 0; i < http.length; i++) {
                        if (!machine.bytes.hasRemaining()) {
                            return setError(machine, ParserError.IncompleteData);
                        }

                        if (http[i] != machine.bytes.get()) {
                            return setError(machine, ParserError.InvalidVersion);
                        }
                    }

                    if (!machine.bytes.hasRemaining()) {
                        return setError(machine, ParserError.IncompleteData);
                    }

                    byte v = (byte) (machine.bytes.get() - '0');

                    if (v != 0 && v != 1) {
                        return setError(machine, ParserError.InvalidVersion);

                    } else {

                        machine.content.version = RequestContent.HTTPVersion.version(v);
                        if (validateRN(machine, true) == error) {
                            return setError(machine, ParserError.InvalidRequestLine);
                        } else {
                            return setState(machine, header);
                        }
                    }
                }

                },
            header {
                public ParserState transition(StateMachine machine){
                    if(validateRN(machine,false) == OK) {
                        return setState(machine,body);
                    }
                    if(machine.state==error) { return error; }

                    byte c = machine.bytes.get();
                    if(c == ':') {
                        machine.lastHeader = machine.str.toString();

                        if(machine.lastHeader.isEmpty()) {
                            return setError(machine,ParserError.MissingHeader);
                        }

                        machine.str = new StringBuilder();
                        return setState(machine,headerContent);

                    } else if (isToken(c)) {
                        machine.str.append(Character.toLowerCase((char) c));
                        return setState(machine,header);
                    } else {
                        return setError(machine,ParserError.UnknownExpression);
                    }
                }

            },
            headerContent {

                public ParserState transition(StateMachine machine) {

                    byte c = machine.bytes.get();
                    while(c==' ') { c=machine.bytes.get(); }

                    if(!isCLT(c)) {
                        machine.str.append(Character.toLowerCase((char)c));
                        return setState(machine,headerContent);
                    } else if(c=='\r') {
                        if(machine.bytes.get() == '\n') {
                            //Space after enter
                            if(machine.bytes.get(machine.bytes.position()) == ' ') {
                                machine.str.append(' ');
                                return setState(machine,headerContent);
                            } else {
                                //Enter new header
                                machine.headers.put(machine.lastHeader,machine.str.toString());
                                machine.str = new StringBuilder();
                                return setState(machine,header);
                            }
                        } else {
                            //Invalid /r/n
                            return setError(machine,ParserError.UnknownExpression);
                        }
                    } else {
                        return setError(machine,ParserError.InvalidHeaderContent);
                    }
                }
            },
            body{

                public  ParserState transition(StateMachine machine) {
                    return setState(machine,done);
                }
            },
            done,
            noNewLine,
            error;

            private static ParserState validateRN(StateMachine machine, boolean required){
                byte c = machine.bytes.get(machine.bytes.position());
                if(c =='\r') {
                    machine.bytes.get();
                    if(!machine.bytes.hasRemaining()) {
                        return setError(machine, ParserError.IncompleteData);
                    }

                    c = machine.bytes.get();
                    if(c=='\n') {
                        return setState(machine,OK);
                    } else {
                        return ParserState.setError(machine,ParserError.UnknownExpression);
                    }
                } else if(required) {
                    return ParserState.setError(machine, ParserError.UnknownExpression);
                } else {
                    return setState(machine,noNewLine);
                }
            }
            static private ParserState setError(StateMachine machine, ParserError error){
                machine.error = error;
                machine.state = ParserState.error;
                return ParserState.error;
            }
            static private ParserState setState(StateMachine machine, ParserState state){
                machine.state = state;
                return state;
            }

            public ParserState transition(StateMachine machine) {
                return setError(machine,ParserError.InvalidState);
            }
        }

        public enum ParserError {
            UnsupportedMethod,
            InvalidVersion,
            IncompleteData,
            UnknownExpression,
            MissingHeader,
            InvalidHeaderContent,
            InvalidState,
            InvalidRequestLine
        }

        private class StateMachine {
            int index;
            RequestContent content;
            ParserError error;
            ParserState state;
            ByteBuffer bytes;
            Map<String,String> headers;
            int read;
            StringBuilder str;
            String lastHeader;
        }

        public RequestContent parse(ByteBuffer buffer, ConnectionState state) {
            if(state == ConnectionState.REQUEST) {
                return parseRequest(buffer);
            } else {
                return parseResponse(buffer);
            }
        }
//
    private RequestContent parseRequest(ByteBuffer buffer) {
        buffer.flip();
        StateMachine machine = new StateMachine();
        machine.state = ParserState.ValidatingMethod;
        machine.bytes = buffer;
        machine.index = buffer.position();
        machine.str = new StringBuilder();
        machine.content = new RequestContent();
        machine.headers = new HashMap<>();
        machine.read=0;
        while(machine.state!=ParserState.body && machine.state != ParserState.error) {
            if(machine.bytes.hasRemaining()) {
                machine.state.transition(machine);
                machine.read ++;
            } else if (machine.state != ParserState.body){
                machine.state = ParserState.error;
                machine.error = ParserError.IncompleteData;
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
        return new RequestContent(MethodType.fromString(headers.get("METHOD")),headers.get("Host"),80,buffer);
    }



}
