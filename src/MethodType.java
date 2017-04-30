import java.util.Arrays;
import java.util.Optional;

/**
 * Created by epord on 20/04/17.
 */
public enum MethodType {
    GET("GET"), POST("POST"), HEAD("HEAD"), OTHER(null);

    public boolean isValid(int index, byte c) {
        if(nameArray.length <= index) return false;
        return nameArray[index] == Character.toUpperCase((char)c);
    }

    public boolean isFinished(int index) {
        return nameArray.length == index;
    }

    String s;
    private byte[] nameArray;
    MethodType(String s) {
        this.s = s;
        if(s!=null) {
            this.nameArray = s.getBytes();
        }
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
