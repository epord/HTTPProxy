package parsers;

import protos.MethodType;
import protos.RequestContent;

/**
 * Created by juanfra on 03/05/17.
 */

public class FirstLineParser {
    static FirstLineParser instance = new FirstLineParser();
    private FirstLineParser(){}

    private class Data {
        FirstLineState firstLineState = FirstLineState.method;
        Object firstLineData;
    }

    public MainState transition(RequestContent content) {
        StateMachine machine = content.machine;
        if(machine.stateData == null) {
            machine.stateData = new Data();
        }

        Data stateData = ((Data) machine.stateData);

        FirstLineState prevState = stateData.firstLineState;
        FirstLineState nextState = stateData.firstLineState.transition(content);

        if( prevState!=nextState ) {
           stateData.firstLineData = null;
        }
        stateData.firstLineState = nextState;

        if(nextState == FirstLineState.nexState) {
            switch (prevState) {
                case method:
                    stateData.firstLineState = FirstLineState.URI; break;
                case URI:
                    stateData.firstLineState = FirstLineState.version; break;
                case version:
                    stateData.firstLineState = FirstLineState.RN; break;
                case RN:
                    stateData.firstLineState = FirstLineState.done; break;
            }
        }

        switch (stateData.firstLineState) {
            case errorState:
                return MainState.errorState;
            case done:
                machine.stateData = null;
                return MainState.headers;
            default:
                return MainState.firstLine;
        }
    }

    public enum FirstLineState implements Transitioner<FirstLineState> {

        method {
            class FirstLineData {
                int index = 0;
            }

            public FirstLineState transition(RequestContent content) {
                StateMachine machine = content.machine;

                Data parentData = (Data) machine.stateData;
                if (parentData.firstLineData == null) {
                    parentData.firstLineData = new FirstLineData();
                }
                FirstLineData data = (FirstLineData) parentData.firstLineData;

                byte c = machine.bytes.get();
                if (data.index == 0) {
                    data.index++;
                    switch (c) {
                        case 'G':
                        case 'g':
                            content.method = MethodType.GET;
                            break;
                        case 'P':
                        case 'p':
                            content.method = MethodType.POST;
                            break;
                        case 'H':
                        case 'h':
                            content.method = MethodType.HEAD;
                            break;
                        default:
                            return setError(machine, MainError.UnsupportedMethod);
                    }
                    return method;
                } else {
                    if (c == ' ') {
                        if (content.method.isFinished(data.index)) {
                            return nexState;
                        } else {
                            return setError(machine, MainError.UnsupportedMethod);
                        }
                    }

                    if (content.method.isValid(data.index, c)) {
                        data.index++;
                        return method;
                    } else {
                        return setError(machine, MainError.UnsupportedMethod);
                    }
                }
            }

        },
        URI {

            class FirstLineData {
                StringBuffer buffer = new StringBuffer();
            }

            public FirstLineState transition(RequestContent content) {
                StateMachine machine = content.machine;
                Data parentData = (Data) machine.stateData;
                if (parentData.firstLineData == null) {
                    parentData.firstLineData = new FirstLineData();
                }
                FirstLineData data = (FirstLineData) parentData.firstLineData;

                byte c = machine.bytes.get();
                if (c == ' ') {
                    content.uri = data.buffer.toString();
                    return nexState;
                } else if (MainParser.isUri(c)) {
                    data.buffer.append(Character.toLowerCase((char) c));
                    return URI;
                } else {
                    return setError(machine, (MainError.UnknownExpression));
                }
            }
        },
        version {
            class FirstLineData {
                int index = 0;
            }

            byte[] http = {'H', 'T', 'T', 'P', '/', '1', '.'};

            public FirstLineState transition(RequestContent content) {
                StateMachine machine = content.machine;

                Data parentData = (Data) machine.stateData;
                if (parentData.firstLineData == null) {
                    parentData.firstLineData = new FirstLineData();
                }
                FirstLineData data = (FirstLineData) parentData.firstLineData;

                if (data.index < http.length) {
                    if (http[data.index++] != machine.bytes.get()) {
                        return setError(machine, MainError.InvalidVersion);
                    } else {
                        return version;
                    }
                } else {
                    byte v = (byte) (machine.bytes.get() - '0');

                    if (v != 0 && v != 1) {
                        return setError(machine, MainError.InvalidVersion);
                    } else {
                        content.version = RequestContent.HTTPVersion.version(v);
                        return nexState;
                    }
                }
            }

        },
        RN {
            class FirstLineData {
                boolean recent_R = false;
            }

            public FirstLineState transition(RequestContent content) {
                StateMachine machine = content.machine;
                Data parentData = (Data) machine.stateData;
                if (parentData.firstLineData == null) {
                    parentData.firstLineData = new FirstLineData();
                }
                FirstLineData data = (FirstLineData) parentData.firstLineData;

                byte c = machine.bytes.get();

                if (data.recent_R) {
                    if (c == '\n') {
                        return done;
                    } else {
                        return setError(machine, MainError.UnknownExpression);
                    }
                } else {
                    if (c == '\r') {
                        data.recent_R = true;
                        return RN;
                    } else {
                        return setError(machine, MainError.UnknownExpression);
                    }
                }

            }
        },
        nexState,
        done,
        errorState;

        public FirstLineState transition(RequestContent requestContent) {
            return this;
        }

        static FirstLineState setError(StateMachine machine, MainError error) {
            machine.error = error;
            return errorState;
        }
    }

}
