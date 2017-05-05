package parsers;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * Created by juanfra on 03/05/17.
 */

public class HeaderParser {

    public static HeaderParser instance = new HeaderParser();
    private HeaderParser() {}

    private class Data {
        HeaderState headerState = HeaderState.header;

        String lastHeader;
        Byte forHeaderTitle = null;

        Object headerData;
    }

    public MainState transition(StateMachine machine) {
        if(machine.stateData == null) {
            machine.stateData = new Data();
        }
        Data stateData = ((Data) machine.stateData);

        HeaderState prevState = stateData.headerState;
        HeaderState nextState = stateData.headerState.transition(machine);

        if( prevState!=nextState ) {
            stateData.headerData = null;
        }
        stateData.headerState = nextState;

        switch (nextState) {
            case nextState:
                machine.stateData = null;
                return MainState.body;
            case errorState:
                return MainState.errorState;
            default:
                return MainState.headers;
        }
    }

    public enum HeaderState implements Transitioner<HeaderState> {
        header {
            class HeaderData {
                StringBuffer buffer = new StringBuffer();
                boolean inNewLine = false;
            }

            public HeaderState transition(StateMachine machine) {
                Data parentData = (Data) machine.stateData;
                if (parentData.headerData == null) {

                    HeaderData headerData = new HeaderData();
                    if(parentData.forHeaderTitle!=null){
                        byte c = parentData.forHeaderTitle;
                        if(c=='\r') {
                            headerData.inNewLine = true;
                        } else if(!MainParser.isToken(parentData.forHeaderTitle)) {
                            return setError(machine, MainError.UnknownExpression);
                        } else {
                            headerData.buffer.append(Character.toLowerCase((char) parentData.forHeaderTitle.byteValue()));
                        }

                        parentData.forHeaderTitle = null;
                    }
                    parentData.headerData = headerData;

                }
                HeaderData data = (HeaderData) parentData.headerData;

                byte c = machine.bytes.get();

                if (data.inNewLine) {
                    if (c == '\n') {
                        if (data.buffer.length() == 0) {
                            return nextState;
                        } else {
                            data.inNewLine = false;
                            return header;
                        }
                    } else {
                        return setError(machine, MainError.UnknownExpression);
                    }
                } else {
                    if (c == '\r') {
                        data.inNewLine = true;
                        return header;
                    }else {
                        if (c == ':') {
                            parentData.lastHeader = data.buffer.toString();
                            if (parentData.lastHeader.isEmpty()) {
                                return setError(machine, MainError.MissingHeader);
                            } else {
                                return headerContent;
                            }

                        } else if (MainParser.isToken(c)) {
                            data.buffer.append(Character.toLowerCase((char) c));
                            return header;
                        } else {
                            return setError(machine, MainError.UnknownExpression);
                        }
                    }
                }
            }
        },
        headerContent {
            class HeaderData {
                StringBuffer buffer = new StringBuffer();
                HeaderContentState currentState = HeaderContentState.leadingSpaces;
            }

            public HeaderState transition(StateMachine machine) {
                Data parentData = (Data) machine.stateData;
                if (parentData.headerData == null) {
                    parentData.headerData = new HeaderData();
                }
                HeaderData data = (HeaderData) parentData.headerData;
                byte c = machine.bytes.get();

                switch (data.currentState) {
                    case leadingSpaces:
                        if (c == ' ') {
                            return headerContent;
                        } else {
                            data.currentState = HeaderContentState.inText;
                        }

                        //Deliberate left no break
                    case inText:
                        if (MainParser.isToken(c) || MainParser.isSeparator(c)) {
                            data.buffer.append(Character.toLowerCase((char) c));;
                            return headerContent;
                        } else if (c == '\r') {
                            data.currentState = HeaderContentState.inNewLine;
                            return headerContent;
                        }
                        break;

                    case inNewLine:
                        if (c == '\n') {
                            data.currentState = HeaderContentState.inNextLine;
                            return headerContent;
                        } else {
                            return setError(machine,MainError.UnknownExpression);
                        }

                    case inNextLine:
                        if (c == ' ') {
                            data.currentState = HeaderContentState.leadingSpaces;
                            return headerContent;
                        } else {
                            parentData.forHeaderTitle = c;
                            machine.headers.put(parentData.lastHeader,data.buffer.toString());
                            return header;
                        }
                }

                return setError(machine, MainError.InvalidHeaderContent);
            }
        },
        nextState,
        errorState;

        public HeaderState transition(StateMachine machine) {
            return this;
        }

        static HeaderState setError(StateMachine machine, MainError error) {
            machine.error = error;
            return errorState;
        }
    }
}