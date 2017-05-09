package parsers;

import protos.HTTPMessage;
import protos.MethodType;

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

    public MainState transition(StateMachine machine) {
        if(machine.stateData == null) {
            machine.stateData = new Data();
        }

        Data stateData = ((Data) machine.stateData);

        FirstLineState prevState = stateData.firstLineState;
        FirstLineState nextState = stateData.firstLineState.transition(machine);

        if( prevState!=nextState ) {
           stateData.firstLineData = null;
        }
        stateData.firstLineState = nextState;

        switch (nextState) {
            case errorState:
                return MainState.errorState;
            case nextState:
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

            public FirstLineState transition(StateMachine machine) {
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
                            machine.method = MethodType.GET;
                            break;
                        case 'P':
                        case 'p':
                            machine.method = MethodType.POST;
                            break;
                        case 'H':
                        case 'h':
                            machine.method = MethodType.HEAD;
                            break;
                        default:
                            return setError(machine, MainError.UnsupportedMethod);
                    }
                    return method;
                } else {
                    if (c == ' ') {
                        if (machine.method.isFinished(data.index)) {
                            return URI;
                        } else {
                            return setError(machine, MainError.UnsupportedMethod);
                        }
                    }

                    if (machine.method.isValid(data.index, c)) {
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

            public FirstLineState transition(StateMachine machine) {
                Data parentData = (Data) machine.stateData;
                if (parentData.firstLineData == null) {
                    parentData.firstLineData = new FirstLineData();
                }
                FirstLineData data = (FirstLineData) parentData.firstLineData;

                byte c = machine.bytes.get();
                if (c == ' ') {
                    machine.uri = data.buffer.toString();
                    return version;
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

            public FirstLineState transition(StateMachine machine) {
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
                        machine.HTTPversion = HTTPMessage.HTTPVersion.version(v);
                        return RN;
                    }
                }
            }

        },
        RN {
            class FirstLineData {
                boolean recent_R = false;
            }

            public FirstLineState transition(StateMachine machine) {
                Data parentData = (Data) machine.stateData;
                if (parentData.firstLineData == null) {
                    parentData.firstLineData = new FirstLineData();
                }
                FirstLineData data = (FirstLineData) parentData.firstLineData;

                byte c = machine.bytes.get();

                if (data.recent_R) {
                    if (c == '\n') {
                        return nextState;
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
        nextState,
        errorState;

        public FirstLineState transition(StateMachine machine) {
            return this;
        }

        static FirstLineState setError(StateMachine machine, MainError error) {
            machine.error = error;
            return errorState;
        }
    }

}
