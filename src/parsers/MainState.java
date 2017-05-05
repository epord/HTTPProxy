package parsers;

/**
 * Created by juanfra on 03/05/17.
 */
public enum MainState implements Transitioner<MainState> {
    firstLine {
        public MainState transition(StateMachine machine) {
            return setState(machine,FirstLineParser.instance.transition(machine));
        }
    },
    headers {
        public MainState transition(StateMachine machine) {
            return setState(machine,HeaderParser.instance.transition(machine));
        }
    },
    body {
        public MainState transition(StateMachine machine) {
            return setState(machine,done);
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
    public MainState transition(StateMachine machine) {
        return this;
    }
}
