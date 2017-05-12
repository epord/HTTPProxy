package parsers;

import protos.RequestContent;

/**
 * Created by juanfra on 03/05/17.
 */
public enum MainState implements Transitioner<MainState> {
    firstLine {
        public MainState transition(RequestContent requestContent) {
            return setState(requestContent.machine,FirstLineParser.instance.transition(requestContent));
        }
    },
    headers {
        public MainState transition(RequestContent requestContent) {
            return setState(requestContent.machine,HeaderParser.instance.transition(requestContent));
        }
    },
    body {
        public MainState transition(RequestContent requestContent) {
            return setState(requestContent.machine,done);
        }
    },
    errorState,
    done;

    static MainState setError(StateMachine machine, MainError error){
        machine.error = error;
        return setState(machine,errorState);
    }

    static MainState setState(StateMachine machine, MainState state){
        machine.state = state;
        return state;
    }

    @Override
    public MainState transition(RequestContent requestContent) {
        return this;
    }
}
