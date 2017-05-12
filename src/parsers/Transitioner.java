package parsers;

import protos.RequestContent;

/**
 * Created by juanfra on 03/05/17.
 */
public interface Transitioner<T extends Enum> {
    T transition(RequestContent requestContent);
}
