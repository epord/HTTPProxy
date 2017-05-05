package protos;

import java.util.Arrays;
import java.util.Optional;

/**
 * Created by epord on 20/04/17.
 */
public enum MethodType {
    GET("GET"), POST("POST"), HEAD("HEAD"), OTHER(null);

    String name;
    private byte[] nameArray;

    MethodType(String s) {
        this.name = s;
        if(s!=null) {
            this.nameArray = s.getBytes();
        }
    }

    static MethodType fromString(String string) {
        if(string==null) return OTHER;
        Optional<MethodType> maybeMethod = Arrays.stream(MethodType.values()).
                filter((m) -> m.name.equals(string)).findAny();

        return maybeMethod.orElse(null);
    }

    public boolean isValid(int index, byte c) {
        return nameArray.length > index && nameArray[index] == Character.toUpperCase((char)c);
    }
    public boolean isFinished(int index) {
        return nameArray.length == index;
    }

}
