package parsers;

import protos.HTTPMessage2;

import java.nio.channels.SelectionKey;

/**
 * Created by Mariano on 8/5/2017.
 */
public interface HTTPParser {

	HTTPMessage2 parse(SelectionKey key);
}
