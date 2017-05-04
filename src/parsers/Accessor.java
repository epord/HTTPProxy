package parsers;

/**
 * Created by juanfra on 04/05/17.
 */
public interface Accessor<T> {
    void update(T t);
    T access();
}
