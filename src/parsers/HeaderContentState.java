package parsers;

/**
 * Created by juanfra on 04/05/17.
 */
public enum HeaderContentState {
    leadingSpaces,
    inText,
    inNewLine,
    inNextLine,
    nextState,
    errorState;
}
