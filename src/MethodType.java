import java.util.Arrays;
import java.util.Optional;

/**
 * Created by epord on 20/04/17.
 */
public enum MethodType {
    GET("GET"), POST("POST"), CONNECT("CONNECT"), OTHER(null);

    String s;
    MethodType(String s) {
        this.s = s;
    }
    static MethodType fromString(String string) {
        if(string==null) return OTHER;
        Optional<MethodType> maybeMethod = Arrays.stream(MethodType.values()).
                filter((m) -> m.s.equals(string)).findAny();

        if(maybeMethod.isPresent()) {
            return maybeMethod.get();
        } else {
            return null;
        }
    }
}
