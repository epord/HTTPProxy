package parsers;

import protos.HTTPMessage2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by Mariano on 8/5/2017.
 */
public class SimpleParser implements HTTPParser {

	@Override
	public HTTPMessage2 parse(SelectionKey key) {


		SocketChannel clientChannel = (SocketChannel) key.channel();

		ByteBuffer aux = ByteBuffer.allocate(1);
		String message = "";
		byte[] bytes = new byte[1];

		int mark = 0;
		while (mark < 4) {
			int read = 0;
			try {
				read = clientChannel.read(aux);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("PARSER: Error reading Channel");
			}
			if (read > 0) {
				aux.flip();
				bytes[0] = aux.get();
				String c = new String(bytes);
				message += c;
				aux.clear();
//				System.out.println(message);
				char ch = c.toCharArray()[0];
				if ((ch == '\n') || (ch == '\r')) {
					mark++;
				} else {
					mark = 0;
				}
			}
		}

		String rn = "";
		rn += new Character('\r');
		rn += new Character('\n');
		rn += rn;

		String[] sections = message.split(rn);

		String host;
		int port;

		if (hasSection(sections, "Host:")) {
			String hostSection = findSection(sections, "Host");
			hostSection = hostSection.split(" ")[1];
			if (hostSection.contains(":")) {
				host = hostSection.split(":")[0];
				port = new Integer(hostSection.split(":")[1]);
			} else {
				host = hostSection;
				port = 80;
			}
			System.out.println(host);
			System.out.println(port);
		}

		for (String s : sections) {
//			System.out.println(s);
		}





//		HTTPMessage2 ans = HTTPMessage2.Request()

		//TODO: ACA QUEDE.
		HTTPMessage2 messages = HTTPMessage2.Response(null, 0, 0);


		return messages;
	}

	private boolean hasSection(String[] sections, String startWith) {
		for (String section : sections) {
			if (section.startsWith(startWith)) {
				return true;
			}
		}
		return false;
	}

	private String findSection(String[] sections, String startWith) {
		for (String section : sections) {
			if (section.startsWith(startWith)) {
				return section;
			}
		}
		return null;
	}

}
